package desktop;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class UnitMain extends JFrame {

    private static final File DB = new File(
            System.getProperty("user.home") + "/DB/db/unit.db");

    // =========================================================
    // THEME
    // =========================================================

    private static final Color BG = new Color(18, 20, 26);

    private static final Color PANEL = new Color(27, 30, 38);

    private static final Color PANEL_2 = new Color(35, 39, 49);

    private static final Color BORDER = new Color(55, 60, 72);

    private static final Color TEXT = new Color(232, 235, 242);

    private static final Color MUTED = new Color(150, 156, 170);

    private static final Color ACCENT = new Color(92, 120, 255);

    private static final Color ACCENT_HOVER = new Color(112, 138, 255);

    private static final Color DANGER = new Color(190, 65, 75);

    // =========================================================
    // MAIN UI
    // =========================================================

    private final JTabbedPane mainTabs = new JTabbedPane();

    private final JTextArea sqlEditor = new JTextArea();

    private final JTextArea sqlOutput = new JTextArea();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public UnitMain() {

        super("UnitMain");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        setSize(
                1450,
                880);

        setMinimumSize(
                new Dimension(
                        1000,
                        650));

        setLocationRelativeTo(
                null);

        buildUI();

        setVisible(
                true);
    }

    // =========================================================
    // BUILD MAIN UI
    // =========================================================

    private void buildUI() {

        JPanel root = new JPanel(
                new BorderLayout());

        root.setBackground(
                BG);

        JLabel topTitle = new JLabel(
                "  UnitMain");

        topTitle.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        20));

        topTitle.setForeground(
                TEXT);

        topTitle.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        10,
                        10));

        JPanel header = new JPanel(
                new BorderLayout());

        header.setBackground(
                PANEL);

        header.setBorder(
                new MatteBorder(
                        0,
                        0,
                        1,
                        0,
                        BORDER));

        header.add(
                topTitle,
                BorderLayout.WEST);

        mainTabs.addTab(
                "SQL IDE",
                buildSqlIDE());

        mainTabs.addTab(
                "UnitApp Builder",
                new AppBuilder());

        root.add(
                header,
                BorderLayout.NORTH);

        root.add(
                mainTabs,
                BorderLayout.CENTER);

        setContentPane(
                root);
    }

    // =========================================================
    // SQL IDE
    // =========================================================

    private JPanel buildSqlIDE() {

        JPanel root = new JPanel(
                new BorderLayout());

        root.setBackground(
                BG);

        JToolBar toolbar = new JToolBar();

        toolbar.setFloatable(
                false);

        toolbar.setBorder(
                new EmptyBorder(
                        8,
                        8,
                        8,
                        8));

        JButton run = new JButton(
                "▶ Run SQL");

        JButton tables = new JButton(
                "Tables");

        JButton schema = new JButton(
                "Schema");

        JButton records = new JButton(
                "Records");

        JButton clear = new JButton(
                "Clear Output");

        styleButton(
                run);

        styleSecondaryButton(
                tables);

        styleSecondaryButton(
                schema);

        styleSecondaryButton(
                records);

        styleSecondaryButton(
                clear);

        toolbar.add(
                run);

        toolbar.addSeparator();

        toolbar.add(
                tables);

        toolbar.add(
                schema);

        toolbar.add(
                records);

        toolbar.addSeparator();

        toolbar.add(
                clear);

        sqlEditor.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        16));

        sqlEditor.setTabSize(
                4);

        sqlEditor.setText(
                "-- UnitMain SQL IDE\n" +
                        "-- Ctrl + Enter = Run\n\n" +
                        "SELECT * FROM records;\n");

        sqlEditor.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15));

        sqlOutput.setEditable(
                false);

        sqlOutput.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14));

        sqlOutput.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12));

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(
                        sqlEditor),
                new JScrollPane(
                        sqlOutput));

        split.setResizeWeight(
                .70);

        split.setDividerSize(
                7);

        run.addActionListener(
                e -> runSQL(
                        sqlEditor.getText()));

        tables.addActionListener(
                e -> sqlOutput.setText(
                        runSQLite(
                                "SELECT name " +
                                        "FROM sqlite_master " +
                                        "WHERE type='table' " +
                                        "ORDER BY name;")));

        schema.addActionListener(
                e -> sqlOutput.setText(
                        runSQLite(
                                "SELECT sql " +
                                        "FROM sqlite_master " +
                                        "WHERE sql IS NOT NULL;")));

        records.addActionListener(
                e -> sqlOutput.setText(
                        runSQLite(
                                "SELECT * FROM records;")));

        clear.addActionListener(
                e -> sqlOutput.setText(
                        ""));

        sqlEditor
                .getInputMap()
                .put(
                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ENTER,
                                InputEvent.CTRL_DOWN_MASK),
                        "runSQL");

        sqlEditor
                .getActionMap()
                .put(
                        "runSQL",

                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {

                                runSQL(
                                        sqlEditor.getText());
                            }
                        });

        root.add(
                toolbar,
                BorderLayout.NORTH);

        root.add(
                split,
                BorderLayout.CENTER);

        return root;
    }

    // =========================================================
    // RUN SQL
    // =========================================================

    private void runSQL(
            String sql) {

        sqlOutput.setText(
                "Running...\n");

        new Thread(
                () -> {

                    String result = runSQLite(
                            sql);

                    SwingUtilities.invokeLater(
                            () -> {

                                if (result == null ||
                                        result.isBlank()) {

                                    sqlOutput.setText(
                                            "Completed successfully.");

                                } else {

                                    sqlOutput.setText(
                                            result);
                                }
                            });
                },

                "unitmain-sql"

        ).start();
    }

    // =========================================================
    // APP BUILDER
    // =========================================================

    class AppBuilder extends JPanel {

        private final DefaultListModel<UnitComponent> componentModel = new DefaultListModel<>();

        private final JList<UnitComponent> components = new JList<>(
                componentModel);

        private final JPanel preview = new JPanel();

        private final JTextField appName = new JTextField(
                "My UnitApp");

        private final JComboBox<String> tableSelector = new JComboBox<>();

        private final JTextField componentName = new JTextField();

        private final JTextField componentText = new JTextField();

        private final JCheckBox liveRefresh = new JCheckBox(
                "Live database updates",
                true);

        private final JCheckBox autosaveEnabled = new JCheckBox(
                "Autosave",
                true);

        private final JLabel status = new JLabel(
                "Ready");

        private javax.swing.Timer refreshTimer;

        private javax.swing.Timer autosaveTimer;

        private boolean loadingProperties = false;

        private boolean loadingProject = false;

        private File currentProjectFile = null;

        private File lastBuiltJar = null;

        private final Path autosaveDirectory = Paths.get(
                System.getProperty(
                        "user.home"),
                "DB",
                "autosave");

        private final Path autosaveFile = autosaveDirectory.resolve(
                "UnitApp-autosave.unitapp");

        // =====================================================
        // CONSTRUCTOR
        // =====================================================

        AppBuilder() {

            setLayout(
                    new BorderLayout());

            setBackground(
                    BG);

            add(
                    buildBuilderToolbar(),
                    BorderLayout.NORTH);

            add(
                    buildWorkspace(),
                    BorderLayout.CENTER);

            add(
                    buildStatus(),
                    BorderLayout.SOUTH);

            refreshTables();

            createDefaultApp();

            startLiveRefresh();

            startAutoSave();

            checkForRecovery();

            rebuildPreview();
        }

        // =====================================================
        // DEFAULT APP
        // =====================================================

        private void createDefaultApp() {

            componentModel.clear();

            componentModel.addElement(
                    new UnitComponent(
                            "TITLE",
                            "title",
                            "Records"));

            componentModel.addElement(
                    new UnitComponent(
                            "SEARCH",
                            "search",
                            "Search..."));

            componentModel.addElement(
                    new UnitComponent(
                            "TABLE",
                            "recordsTable",
                            "Database Table"));

            componentModel.addElement(
                    new UnitComponent(
                            "BUTTON",
                            "newButton",
                            "+ New Record"));

            if (!componentModel.isEmpty()) {

                components.setSelectedIndex(
                        0);
            }
        }

        // =====================================================
        // BUILDER TOOLBAR
        // =====================================================

        private JToolBar buildBuilderToolbar() {

            JToolBar bar = new JToolBar();

            bar.setFloatable(
                    false);

            bar.setBorder(
                    new EmptyBorder(
                            8,
                            8,
                            8,
                            8));

            JButton newProject = new JButton(
                    "+ New");

            JButton run = new JButton(
                    "▶ Preview App");

            JButton build = new JButton(
                    "⚒ Build Java + JAR");

            JButton runJar = new JButton(
                    "▶ Run JAR");

            JButton save = new JButton(
                    "Save");

            JButton saveAs = new JButton(
                    "Save As");

            JButton load = new JButton(
                    "Open");

            JButton refresh = new JButton(
                    "↻ Refresh");

            styleSecondaryButton(
                    newProject);

            styleButton(
                    run);

            styleButton(
                    build);

            styleSecondaryButton(
                    runJar);

            styleSecondaryButton(
                    save);

            styleSecondaryButton(
                    saveAs);

            styleSecondaryButton(
                    load);

            styleSecondaryButton(
                    refresh);

            JLabel appLabel = new JLabel(
                    " App ");

            appName.setMaximumSize(
                    new Dimension(
                            220,
                            34));

            JLabel tableLabel = new JLabel(
                    " Table ");

            tableSelector.setMaximumSize(
                    new Dimension(
                            190,
                            34));

            bar.add(
                    newProject);

            bar.addSeparator();

            bar.add(
                    appLabel);

            bar.add(
                    appName);

            bar.addSeparator();

            bar.add(
                    tableLabel);

            bar.add(
                    tableSelector);

            bar.addSeparator();

            bar.add(
                    run);

            bar.add(
                    build);

            bar.add(
                    runJar);

            bar.add(
                    save);

            bar.add(
                    saveAs);

            bar.add(
                    load);

            bar.add(
                    refresh);

            bar.addSeparator();

            bar.add(
                    liveRefresh);

            bar.add(
                    autosaveEnabled);

            // =============================================
            // EVENTS
            // =============================================

            newProject.addActionListener(
                    e -> newProject());

            run.addActionListener(
                    e -> runApp());

            build.addActionListener(
                    e -> buildStandaloneApp(
                            false));

            runJar.addActionListener(
                    e -> {

                        if (lastBuiltJar == null ||
                                !lastBuiltJar.exists()) {

                            buildStandaloneApp(
                                    true);

                        } else {

                            runJar(
                                    lastBuiltJar);
                        }
                    });

            save.addActionListener(
                    e -> saveProject());

            saveAs.addActionListener(
                    e -> saveProjectAs());

            load.addActionListener(
                    e -> loadProject());

            refresh.addActionListener(
                    e -> {

                        refreshTables();

                        rebuildPreview();

                        status.setText(
                                "Refreshed");
                    });

            tableSelector.addActionListener(
                    e -> {

                        if (!loadingProject) {

                            rebuildPreview();
                        }
                    });

            appName
                    .getDocument()
                    .addDocumentListener(
                            documentListener(
                                    () -> {

                                        if (!loadingProject) {

                                            rebuildPreview();
                                        }
                                    }));

            return bar;
        }

        // =====================================================
        // WORKSPACE
        // =====================================================

        private Component buildWorkspace() {

            JSplitPane outer = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT);

            outer.setDividerLocation(
                    210);

            outer.setDividerSize(
                    7);

            outer.setLeftComponent(
                    buildPalette());

            JSplitPane inner = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT);

            inner.setDividerLocation(
                    300);

            inner.setDividerSize(
                    7);

            inner.setLeftComponent(
                    buildComponentEditor());

            inner.setRightComponent(
                    buildPreviewPanel());

            outer.setRightComponent(
                    inner);

            return outer;
        }

        // =====================================================
        // PALETTE
        // =====================================================

        private JPanel buildPalette() {

            JPanel panel = createPanel(
                    "Components");

            JPanel buttons = new JPanel(
                    new GridLayout(
                            0,
                            1,
                            7,
                            7));

            buttons.setOpaque(
                    false);

            JButton title = new JButton(
                    "+ Title");

            JButton label = new JButton(
                    "+ Label");

            JButton field = new JButton(
                    "+ Text Field");

            JButton search = new JButton(
                    "+ Search");

            JButton button = new JButton(
                    "+ Button");

            JButton table = new JButton(
                    "+ Database Table");

            JButton spacer = new JButton(
                    "+ Spacer");

            JButton divider = new JButton(
                    "+ Divider");

            JButton[] all = {
                    title,
                    label,
                    field,
                    search,
                    button,
                    table,
                    spacer,
                    divider
            };

            for (JButton b : all) {

                styleSecondaryButton(
                        b);

                buttons.add(
                        b);
            }

            title.addActionListener(
                    e -> addComponent(
                            "TITLE",
                            "New Title"));

            label.addActionListener(
                    e -> addComponent(
                            "LABEL",
                            "New Label"));

            field.addActionListener(
                    e -> addComponent(
                            "FIELD",
                            "Enter text..."));

            search.addActionListener(
                    e -> addComponent(
                            "SEARCH",
                            "Search..."));

            button.addActionListener(
                    e -> addComponent(
                            "BUTTON",
                            "Button"));

            table.addActionListener(
                    e -> addComponent(
                            "TABLE",
                            "Database Table"));

            spacer.addActionListener(
                    e -> addComponent(
                            "SPACER",
                            ""));

            divider.addActionListener(
                    e -> addComponent(
                            "DIVIDER",
                            ""));

            JPanel wrapper = new JPanel(
                    new BorderLayout());

            wrapper.setOpaque(
                    false);

            wrapper.setBorder(
                    new EmptyBorder(
                            8,
                            8,
                            8,
                            8));

            wrapper.add(
                    buttons,
                    BorderLayout.NORTH);

            panel.add(
                    wrapper,
                    BorderLayout.CENTER);

            return panel;
        }

        // =====================================================
        // COMPONENT EDITOR
        // =====================================================

        private JPanel buildComponentEditor() {

            JPanel panel = createPanel(
                    "App Layout");

            components.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION);

            components.setFixedCellHeight(
                    36);

            components.addListSelectionListener(
                    e -> {

                        if (!e.getValueIsAdjusting()) {

                            loadProperties();
                        }
                    });

            panel.add(
                    new JScrollPane(
                            components),
                    BorderLayout.CENTER);

            JPanel properties = new JPanel();

            properties.setOpaque(
                    false);

            properties.setLayout(
                    new BoxLayout(
                            properties,
                            BoxLayout.Y_AXIS));

            properties.setBorder(
                    new EmptyBorder(
                            10,
                            10,
                            10,
                            10));

            properties.add(
                    new JLabel(
                            "Component ID"));

            properties.add(
                    Box.createVerticalStrut(
                            4));

            properties.add(
                    componentName);

            properties.add(
                    Box.createVerticalStrut(
                            10));

            properties.add(
                    new JLabel(
                            "Text"));

            properties.add(
                    Box.createVerticalStrut(
                            4));

            properties.add(
                    componentText);

            properties.add(
                    Box.createVerticalStrut(
                            12));

            JPanel actions = new JPanel(
                    new GridLayout(
                            2,
                            2,
                            6,
                            6));

            actions.setOpaque(
                    false);

            JButton up = new JButton(
                    "↑ Up");

            JButton down = new JButton(
                    "↓ Down");

            JButton duplicate = new JButton(
                    "Duplicate");

            JButton remove = new JButton(
                    "Delete");

            styleSecondaryButton(
                    up);

            styleSecondaryButton(
                    down);

            styleSecondaryButton(
                    duplicate);

            styleDangerButton(
                    remove);

            actions.add(
                    up);

            actions.add(
                    down);

            actions.add(
                    duplicate);

            actions.add(
                    remove);

            properties.add(
                    actions);

            componentName
                    .getDocument()
                    .addDocumentListener(
                            documentListener(
                                    this::saveProperties));

            componentText
                    .getDocument()
                    .addDocumentListener(
                            documentListener(
                                    this::saveProperties));

            up.addActionListener(
                    e -> moveSelected(
                            -1));

            down.addActionListener(
                    e -> moveSelected(
                            1));

            duplicate.addActionListener(
                    e -> duplicateSelected());

            remove.addActionListener(
                    e -> removeSelected());

            panel.add(
                    properties,
                    BorderLayout.SOUTH);

            return panel;
        }

        // =====================================================
        // PREVIEW PANEL
        // =====================================================

        private JPanel buildPreviewPanel() {

            JPanel holder = createPanel(
                    "Live Preview");

            preview.setBackground(
                    PANEL);

            preview.setLayout(
                    new BoxLayout(
                            preview,
                            BoxLayout.Y_AXIS));

            preview.setBorder(
                    new EmptyBorder(
                            25,
                            25,
                            25,
                            25));

            JScrollPane scroll = new JScrollPane(
                    preview);

            scroll
                    .getVerticalScrollBar()
                    .setUnitIncrement(
                            16);

            holder.add(
                    scroll,
                    BorderLayout.CENTER);

            return holder;
        }

        // =====================================================
        // REBUILD PREVIEW
        // =====================================================

        private void rebuildPreview() {

            SwingUtilities.invokeLater(
                    () -> {

                        preview.removeAll();

                        JLabel appTitle = new JLabel(
                                appName
                                        .getText()
                                        .isBlank()

                                                ? "Untitled UnitApp"

                                                : appName
                                                        .getText());

                        appTitle.setForeground(
                                TEXT);

                        appTitle.setFont(
                                new Font(
                                        Font.SANS_SERIF,
                                        Font.BOLD,
                                        28));

                        appTitle.setAlignmentX(
                                Component.LEFT_ALIGNMENT);

                        preview.add(
                                appTitle);

                        preview.add(
                                Box.createVerticalStrut(
                                        20));

                        for (int i = 0; i < componentModel.size(); i++) {

                            JComponent component = createPreviewComponent(
                                    componentModel.get(
                                            i));

                            if (component != null) {

                                component.setAlignmentX(
                                        Component.LEFT_ALIGNMENT);

                                preview.add(
                                        component);

                                preview.add(
                                        Box.createVerticalStrut(
                                                10));
                            }
                        }

                        preview.revalidate();

                        preview.repaint();

                        status.setText(
                                "Preview updated");
                    });
        }

        // =====================================================
        // CREATE PREVIEW COMPONENT
        // =====================================================

        private JComponent createPreviewComponent(
                UnitComponent c) {

            switch (c.type) {

                case "TITLE": {

                    JLabel label = new JLabel(
                            c.text);

                    label.setForeground(
                            TEXT);

                    label.setFont(
                            new Font(
                                    Font.SANS_SERIF,
                                    Font.BOLD,
                                    21));

                    return label;
                }

                case "LABEL": {

                    JLabel label = new JLabel(
                            c.text);

                    label.setForeground(
                            TEXT);

                    return label;
                }

                case "FIELD": {

                    JTextField field = new JTextField(
                            c.text);

                    styleInput(
                            field);

                    return field;
                }

                case "SEARCH": {

                    JTextField search = new JTextField();

                    search.setToolTipText(
                            c.text);

                    search.putClientProperty(
                            "JTextField.placeholderText",
                            c.text);

                    styleInput(
                            search);

                    return search;
                }

                case "BUTTON": {

                    JButton button = new JButton(
                            c.text);

                    styleButton(
                            button);

                    return button;
                }

                case "TABLE":

                    return createDatabaseTable();

                case "SPACER": {

                    JPanel spacer = new JPanel();

                    spacer.setOpaque(
                            false);

                    spacer.setPreferredSize(
                            new Dimension(
                                    1,
                                    25));

                    spacer.setMaximumSize(
                            new Dimension(
                                    Integer.MAX_VALUE,
                                    25));

                    return spacer;
                }

                case "DIVIDER": {

                    JSeparator separator = new JSeparator();

                    separator.setMaximumSize(
                            new Dimension(
                                    Integer.MAX_VALUE,
                                    1));

                    return separator;
                }

                default: {

                    JLabel unknown = new JLabel(
                            c.text);

                    unknown.setForeground(
                            TEXT);

                    return unknown;
                }
            }
        }

        // =====================================================
        // DATABASE TABLE PREVIEW
        // =====================================================

        private JComponent createDatabaseTable() {

            String table = (String) tableSelector
                    .getSelectedItem();

            if (table == null ||
                    table.isBlank()) {

                JLabel message = new JLabel(
                        "No database table selected.");

                message.setForeground(
                        MUTED);

                return message;
            }

            JTable tableView = new JTable(
                    loadTableModel(
                            table));

            tableView.setRowHeight(
                    28);

            tableView.setAutoResizeMode(
                    JTable.AUTO_RESIZE_OFF);

            JScrollPane scroll = new JScrollPane(
                    tableView);

            scroll.setPreferredSize(
                    new Dimension(
                            720,
                            270));

            scroll.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            300));

            return scroll;
        }

        // =====================================================
        // REFRESH TABLE LIST
        // =====================================================

        private void refreshTables() {

            Object previous = tableSelector
                    .getSelectedItem();

            String data = runSQLiteSeparated(
                    "SELECT name " +
                            "FROM sqlite_master " +
                            "WHERE type='table' " +
                            "AND name NOT LIKE 'sqlite_%' " +
                            "ORDER BY name;");

            tableSelector.removeAllItems();

            for (String line : data.split("\\R")) {

                String name = line.trim();

                if (!name.isBlank()) {

                    tableSelector.addItem(
                            name);
                }
            }

            if (previous != null) {

                tableSelector.setSelectedItem(
                        previous.toString());
            }
        }

        // =====================================================
        // LOAD DATABASE TABLE
        // =====================================================

        private DefaultTableModel loadTableModel(
                String table) {

            if (!table.matches(
                    "[A-Za-z_][A-Za-z0-9_]*")) {

                return new DefaultTableModel(
                        new Object[][] {},
                        new Object[] {
                                "Invalid table"
                        });
            }

            String schema = runSQLiteSeparated(
                    "PRAGMA table_info(\"" +
                            table +
                            "\");");

            List<String> columns = new ArrayList<>();

            for (String line : schema.split("\\R")) {

                String[] parts = line.split(
                        "\\u001f",
                        -1);

                if (parts.length >= 2) {

                    columns.add(
                            parts[1]);
                }
            }

            if (columns.isEmpty()) {

                return new DefaultTableModel();
            }

            String data = runSQLiteSeparated(
                    "SELECT * FROM \"" +
                            table +
                            "\" LIMIT 250;");

            DefaultTableModel model = new DefaultTableModel(
                    columns.toArray(),
                    0);

            for (String line : data.split("\\R")) {

                if (line.isBlank()) {

                    continue;
                }

                String[] row = line.split(
                        "\\u001f",
                        -1);

                Object[] values = new Object[columns.size()];

                for (int i = 0; i < values.length; i++) {

                    values[i] = i < row.length
                            ? row[i]
                            : "";
                }

                model.addRow(
                        values);
            }

            return model;
        }

        // =====================================================
        // LIVE REFRESH
        // =====================================================

        private void startLiveRefresh() {

            refreshTimer = new javax.swing.Timer(
                    2000,

                    e -> {

                        if (liveRefresh
                                .isSelected()
                                &&
                                mainTabs
                                        .getSelectedComponent() == this) {

                            rebuildPreview();
                        }
                    });

            refreshTimer.start();
        }

        // =====================================================
        // AUTOSAVE
        // =====================================================

        private void startAutoSave() {

            autosaveTimer = new javax.swing.Timer(
                    3000,

                    e -> autoSave());

            autosaveTimer.start();
        }

        private void autoSave() {

            if (!autosaveEnabled
                    .isSelected()
                    ||
                    loadingProject) {

                return;
            }

            try {

                Files.createDirectories(
                        autosaveDirectory);

                writeProject(
                        autosaveFile.toFile());

                status.setText(
                        "Autosaved");

            } catch (Exception ex) {

                status.setText(
                        "Autosave failed: " +
                                ex.getMessage());
            }
        }

        // =====================================================
        // RECOVERY
        // =====================================================

        private void checkForRecovery() {

            if (!Files.exists(
                    autosaveFile)) {

                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                    UnitMain.this,

                    "UnitMain found an autosaved UnitApp.\n" +
                            "Restore it?",

                    "Recover UnitApp",

                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {

                loadProjectFile(
                        autosaveFile.toFile());

                status.setText(
                        "Recovered autosaved project");
            }
        }

        // =====================================================
        // COMPONENT ACTIONS
        // =====================================================

        private void addComponent(
                String type,
                String text) {

            String id = type.toLowerCase() +
                    (componentModel.size()
                            +
                            1);

            UnitComponent component = new UnitComponent(
                    type,
                    id,
                    text);

            componentModel.addElement(
                    component);

            components.setSelectedIndex(
                    componentModel.size()
                            -
                            1);

            rebuildPreview();
        }

        private void loadProperties() {

            UnitComponent c = components
                    .getSelectedValue();

            loadingProperties = true;

            if (c == null) {

                componentName.setText(
                        "");

                componentText.setText(
                        "");

            } else {

                componentName.setText(
                        c.id);

                componentText.setText(
                        c.text);
            }

            loadingProperties = false;
        }

        private void saveProperties() {

            if (loadingProperties ||
                    loadingProject) {

                return;
            }

            UnitComponent c = components
                    .getSelectedValue();

            if (c == null) {

                return;
            }

            c.id = componentName
                    .getText();

            c.text = componentText
                    .getText();

            components.repaint();

            rebuildPreview();
        }

        private void moveSelected(
                int direction) {

            int index = components
                    .getSelectedIndex();

            if (index < 0) {

                return;
            }

            int target = index +
                    direction;

            if (target < 0 ||
                    target >= componentModel.size()) {

                return;
            }

            UnitComponent component = componentModel.get(
                    index);

            componentModel.remove(
                    index);

            componentModel.add(
                    target,
                    component);

            components.setSelectedIndex(
                    target);

            rebuildPreview();
        }

        private void duplicateSelected() {

            UnitComponent c = components
                    .getSelectedValue();

            if (c == null) {

                return;
            }

            UnitComponent copy = new UnitComponent(
                    c.type,
                    c.id + "Copy",
                    c.text);

            componentModel.addElement(
                    copy);

            components.setSelectedIndex(
                    componentModel.size()
                            -
                            1);

            rebuildPreview();
        }

        private void removeSelected() {

            int index = components
                    .getSelectedIndex();

            if (index < 0) {

                return;
            }

            componentModel.remove(
                    index);

            if (!componentModel.isEmpty()) {

                components.setSelectedIndex(
                        Math.min(
                                index,
                                componentModel.size() - 1));
            }

            rebuildPreview();
        }

        // =====================================================
        // NEW APP
        // =====================================================

        private void newProject() {

            int answer = JOptionPane.showConfirmDialog(
                    UnitMain.this,

                    "Create a new UnitApp?",

                    "New UnitApp",

                    JOptionPane.YES_NO_OPTION);

            if (answer != JOptionPane.YES_OPTION) {

                return;
            }

            loadingProject = true;

            appName.setText(
                    "My UnitApp");

            currentProjectFile = null;

            lastBuiltJar = null;

            createDefaultApp();

            loadingProject = false;

            rebuildPreview();

            status.setText(
                    "New UnitApp");
        }

        // =====================================================
        // PREVIEW APP WINDOW
        // =====================================================

        private void runApp() {

            JFrame app = new JFrame(
                    appName
                            .getText()
                            .isBlank()

                                    ? "UnitApp"

                                    : appName
                                            .getText());

            app.setDefaultCloseOperation(
                    JFrame.DISPOSE_ON_CLOSE);

            app.setSize(
                    950,
                    650);

            app.setMinimumSize(
                    new Dimension(
                            600,
                            400));

            app.setLocationRelativeTo(
                    UnitMain.this);

            JPanel content = new JPanel();

            content.setBackground(
                    PANEL);

            content.setLayout(
                    new BoxLayout(
                            content,
                            BoxLayout.Y_AXIS));

            content.setBorder(
                    new EmptyBorder(
                            25,
                            25,
                            25,
                            25));

            JLabel title = new JLabel(
                    appName
                            .getText()
                            .isBlank()

                                    ? "UnitApp"

                                    : appName
                                            .getText());

            title.setForeground(
                    TEXT);

            title.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.BOLD,
                            28));

            title.setAlignmentX(
                    Component.LEFT_ALIGNMENT);

            content.add(
                    title);

            content.add(
                    Box.createVerticalStrut(
                            20));

            for (int i = 0; i < componentModel.size(); i++) {

                JComponent component = createPreviewComponent(
                        componentModel.get(
                                i));

                if (component != null) {

                    component.setAlignmentX(
                            Component.LEFT_ALIGNMENT);

                    content.add(
                            component);

                    content.add(
                            Box.createVerticalStrut(
                                    10));
                }
            }

            JScrollPane scroll = new JScrollPane(
                    content);

            scroll
                    .getVerticalScrollBar()
                    .setUnitIncrement(
                            16);

            app.add(
                    scroll);

            app.setVisible(
                    true);
        }

        // =====================================================
        // COMPILER
        // =====================================================

        private void buildStandaloneApp(
                boolean runAfterBuild) {

            status.setText(
                    "Building...");

            new Thread(
                    () -> {

                        try {

                            String name = appName
                                    .getText()
                                    .isBlank()

                                            ? "UnitApp"

                                            : appName
                                                    .getText();

                            String className = javaClassName(
                                    name);

                            Path buildDir = Paths.get(
                                    System.getProperty(
                                            "user.home"),
                                    "DB",
                                    "build",
                                    safeName(
                                            name));

                            Path sourceDir = buildDir.resolve(
                                    "src");

                            Path classesDir = buildDir.resolve(
                                    "classes");

                            Files.createDirectories(
                                    sourceDir);

                            if (Files.exists(
                                    classesDir)) {

                                deleteTree(
                                        classesDir);
                            }

                            Files.createDirectories(
                                    classesDir);

                            Path javaFile = sourceDir.resolve(
                                    className +
                                            ".java");

                            Path jarFile = buildDir.resolve(
                                    safeName(
                                            name)
                                            +
                                            ".jar");

                            String source = generateJavaSource(
                                    className);

                            Files.writeString(
                                    javaFile,
                                    source,
                                    StandardCharsets.UTF_8,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING);

                            // =================================
                            // COMPILE
                            // =================================

                            JavaCompiler compiler = ToolProvider
                                    .getSystemJavaCompiler();

                            if (compiler == null) {

                                throw new Exception(
                                        "Java compiler not found.\n\n" +
                                                "Install a JDK:\n" +
                                                "sudo apt install default-jdk");
                            }

                            ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();

                            int result = compiler.run(
                                    null,
                                    compilerOutput,
                                    compilerOutput,

                                    "-encoding",
                                    "UTF-8",

                                    "-d",
                                    classesDir.toString(),

                                    javaFile.toString());

                            String compilerMessage = compilerOutput
                                    .toString(
                                            StandardCharsets.UTF_8);

                            if (result != 0) {

                                throw new Exception(
                                        "Compilation failed:\n\n" +
                                                compilerMessage);
                            }

                            // =================================
                            // JAR
                            // =================================

                            createJar(
                                    classesDir,
                                    jarFile,
                                    className);

                            lastBuiltJar = jarFile.toFile();

                            SwingUtilities.invokeLater(
                                    () -> {

                                        status.setText(
                                                "Built: " +
                                                        jarFile.getFileName());

                                        JOptionPane.showMessageDialog(
                                                UnitMain.this,

                                                "Build complete!\n\n" +

                                                        "Java source:\n" +
                                                        javaFile +
                                                        "\n\n" +

                                                        "Runnable JAR:\n" +
                                                        jarFile,

                                                "UnitMain Compiler",

                                                JOptionPane.INFORMATION_MESSAGE);
                                    });

                            if (runAfterBuild) {

                                runJar(
                                        jarFile.toFile());
                            }

                        } catch (Exception ex) {

                            SwingUtilities.invokeLater(
                                    () -> {

                                        status.setText(
                                                "Build failed");

                                        JOptionPane.showMessageDialog(
                                                UnitMain.this,

                                                ex.getMessage(),

                                                "Compiler Error",

                                                JOptionPane.ERROR_MESSAGE);
                                    });
                        }
                    },

                    "unitmain-builder"

            ).start();
        }

        // =====================================================
        // GENERATE JAVA SOURCE
        // =====================================================

        private String generateJavaSource(
                String className) {

            String name = appName
                    .getText()
                    .isBlank()

                            ? "UnitApp"

                            : appName
                                    .getText();

            String generatedAppName = javaString(
                    name);

            StringBuilder body = new StringBuilder();

            int componentNumber = 0;

            for (int i = 0; i < componentModel.size(); i++) {

                UnitComponent c = componentModel.get(
                        i);

                componentNumber++;

                String variable = "component" +
                        componentNumber;

                switch (c.type) {

                    case "TITLE" ->

                        body.append(
                                """
                                        {
                                            JLabel %s = new JLabel("%s");

                                            %s.setFont(
                                                    new Font(
                                                            Font.SANS_SERIF,
                                                            Font.BOLD,
                                                            22
                                                    )
                                            );

                                            %s.setForeground(TEXT);

                                            %s.setAlignmentX(
                                                    Component.LEFT_ALIGNMENT
                                            );

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,

                                        javaString(
                                                c.text),

                                        variable,
                                        variable,
                                        variable,
                                        variable));

                    case "LABEL" ->

                        body.append(
                                """
                                        {
                                            JLabel %s = new JLabel("%s");

                                            %s.setForeground(TEXT);

                                            %s.setAlignmentX(
                                                    Component.LEFT_ALIGNMENT
                                            );

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,

                                        javaString(
                                                c.text),

                                        variable,
                                        variable,
                                        variable));

                    case "FIELD" ->

                        body.append(
                                """
                                        {
                                            JTextField %s =
                                                    new JTextField("%s");

                                            styleField(%s);

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,

                                        javaString(
                                                c.text),

                                        variable,
                                        variable));

                    case "SEARCH" ->

                        body.append(
                                """
                                        {
                                            JTextField %s =
                                                    new JTextField();

                                            %s.setToolTipText("%s");

                                            styleField(%s);

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,
                                        variable,

                                        javaString(
                                                c.text),

                                        variable,
                                        variable));

                    case "BUTTON" ->

                        body.append(
                                """
                                        {
                                            JButton %s =
                                                    new JButton("%s");

                                            styleButton(%s);

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,

                                        javaString(
                                                c.text),

                                        variable,
                                        variable));

                    case "TABLE" -> {

                        Object selected = tableSelector
                                .getSelectedItem();

                        String tableName = selected == null
                                ? ""
                                : selected.toString();

                        body.append(
                                """
                                        {
                                            JComponent %s =
                                                    createDatabaseTable("%s");

                                            %s.setAlignmentX(
                                                    Component.LEFT_ALIGNMENT
                                            );

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,

                                        javaString(
                                                tableName),

                                        variable,
                                        variable));
                    }

                    case "SPACER" ->

                        body.append(
                                """
                                        content.add(
                                                Box.createVerticalStrut(25)
                                        );

                                        """);

                    case "DIVIDER" ->

                        body.append(
                                """
                                        {
                                            JSeparator %s =
                                                    new JSeparator();

                                            %s.setMaximumSize(
                                                    new Dimension(
                                                            Integer.MAX_VALUE,
                                                            2
                                                    )
                                            );

                                            %s.setAlignmentX(
                                                    Component.LEFT_ALIGNMENT
                                            );

                                            content.add(%s);

                                            content.add(
                                                    Box.createVerticalStrut(10)
                                            );
                                        }

                                        """.formatted(

                                        variable,
                                        variable,
                                        variable,
                                        variable));
                }
            }

            return """
                    import javax.swing.*;
                    import javax.swing.border.*;
                    import javax.swing.table.DefaultTableModel;

                    import java.awt.*;
                    import java.io.*;
                    import java.nio.charset.StandardCharsets;
                    import java.util.ArrayList;
                    import java.util.List;

                    public class %s {

                        static final Color PANEL =
                                new Color(
                                        27,
                                        30,
                                        38
                                );

                        static final Color PANEL_2 =
                                new Color(
                                        35,
                                        39,
                                        49
                                );

                        static final Color BORDER =
                                new Color(
                                        55,
                                        60,
                                        72
                                );

                        static final Color TEXT =
                                new Color(
                                        232,
                                        235,
                                        242
                                );

                        static final Color ACCENT =
                                new Color(
                                        92,
                                        120,
                                        255
                                );

                        static final File DB =
                                new File(
                                        System.getProperty("user.home")
                                        +
                                        "/DB/db/unit.db"
                                );

                        public static void main(
                                String[] args
                        ) {

                            SwingUtilities.invokeLater(
                                    %s::createWindow
                            );
                        }

                        static void createWindow() {

                            JFrame window =
                                    new JFrame(
                                            "%s"
                                    );

                            window.setDefaultCloseOperation(
                                    JFrame.EXIT_ON_CLOSE
                            );

                            window.setSize(
                                    1000,
                                    700
                            );

                            window.setMinimumSize(
                                    new Dimension(
                                            600,
                                            400
                                    )
                            );

                            window.setLocationRelativeTo(
                                    null
                            );

                            JPanel content =
                                    new JPanel();

                            content.setBackground(
                                    PANEL
                            );

                            content.setLayout(
                                    new BoxLayout(
                                            content,
                                            BoxLayout.Y_AXIS
                                    )
                            );

                            content.setBorder(
                                    new EmptyBorder(
                                            25,
                                            25,
                                            25,
                                            25
                                    )
                            );

                            JLabel appTitle =
                                    new JLabel(
                                            "%s"
                                    );

                            appTitle.setForeground(
                                    TEXT
                            );

                            appTitle.setFont(
                                    new Font(
                                            Font.SANS_SERIF,
                                            Font.BOLD,
                                            30
                                    )
                            );

                            appTitle.setAlignmentX(
                                    Component.LEFT_ALIGNMENT
                            );

                            content.add(
                                    appTitle
                            );

                            content.add(
                                    Box.createVerticalStrut(
                                            20
                                    )
                            );

                    %s

                            JScrollPane scroll =
                                    new JScrollPane(
                                            content
                                    );

                            scroll.setBorder(
                                    null
                            );

                            scroll
                                    .getVerticalScrollBar()
                                    .setUnitIncrement(
                                            16
                                    );

                            window.setContentPane(
                                    scroll
                            );

                            window.setVisible(
                                    true
                            );
                        }

                        static void styleField(
                                JTextField field
                        ) {

                            field.setBackground(
                                    PANEL_2
                            );

                            field.setForeground(
                                    TEXT
                            );

                            field.setCaretColor(
                                    Color.WHITE
                            );

                            field.setMaximumSize(
                                    new Dimension(
                                            Integer.MAX_VALUE,
                                            38
                                    )
                            );

                            field.setBorder(
                                    new CompoundBorder(

                                            new LineBorder(
                                                    BORDER
                                            ),

                                            new EmptyBorder(
                                                    8,
                                                    10,
                                                    8,
                                                    10
                                            )
                                    )
                            );

                            field.setAlignmentX(
                                    Component.LEFT_ALIGNMENT
                            );
                        }

                        static void styleButton(
                                JButton button
                        ) {

                            button.setBackground(
                                    ACCENT
                            );

                            button.setForeground(
                                    Color.WHITE
                            );

                            button.setFocusPainted(
                                    false
                            );

                            button.setBorderPainted(
                                    false
                            );

                            button.setOpaque(
                                    true
                            );

                            button.setBorder(
                                    new EmptyBorder(
                                            9,
                                            15,
                                            9,
                                            15
                                    )
                            );

                            button.setAlignmentX(
                                    Component.LEFT_ALIGNMENT
                            );
                        }

                        static JComponent createDatabaseTable(
                                String table
                        ) {

                            if (
                                    table == null ||
                                    table.isBlank()
                            ) {

                                JLabel label =
                                        new JLabel(
                                                "No database table selected."
                                        );

                                label.setForeground(
                                        TEXT
                                );

                                return label;
                            }

                            JTable tableView =
                                    new JTable(
                                            loadTable(
                                                    table
                                            )
                                    );

                            tableView.setRowHeight(
                                    28
                            );

                            tableView.setAutoResizeMode(
                                    JTable.AUTO_RESIZE_OFF
                            );

                            JScrollPane scroll =
                                    new JScrollPane(
                                            tableView
                                    );

                            scroll.setPreferredSize(
                                    new Dimension(
                                            800,
                                            300
                                    )
                            );

                            scroll.setMaximumSize(
                                    new Dimension(
                                            Integer.MAX_VALUE,
                                            320
                                    )
                            );

                            return scroll;
                        }

                        static DefaultTableModel loadTable(
                                String table
                        ) {

                            if (
                                    !table.matches(
                                            "[A-Za-z_][A-Za-z0-9_]*"
                                    )
                            ) {

                                return new DefaultTableModel();
                            }

                            String schema =
                                    runSQLite(
                                            "PRAGMA table_info(\\\"" +
                                            table +
                                            "\\\");"
                                    );

                            List<String> columns =
                                    new ArrayList<>();

                            for (
                                    String line :
                                    schema.split("\\\\R")
                            ) {

                                String[] parts =
                                        line.split(
                                                "\\\\u001f",
                                                -1
                                        );

                                if (
                                        parts.length >= 2
                                ) {

                                    columns.add(
                                            parts[1]
                                    );
                                }
                            }

                            DefaultTableModel model =
                                    new DefaultTableModel(
                                            columns.toArray(),
                                            0
                                    );

                            if (
                                    columns.isEmpty()
                            ) {

                                return model;
                            }

                            String rows =
                                    runSQLite(
                                            "SELECT * FROM \\"" +
                                            table +
                                            "\\" LIMIT 250;"
                                    );

                            for (
                                    String line :
                                    rows.split("\\\\R")
                            ) {

                                if (
                                        line.isBlank()
                                ) {

                                    continue;
                                }

                                String[] values =
                                        line.split(
                                                "\\\\u001f",
                                                -1
                                        );

                                Object[] row =
                                        new Object[
                                                columns.size()
                                        ];

                                for (
                                        int i = 0;
                                        i < row.length;
                                        i++
                                ) {

                                    row[i] =
                                            i < values.length
                                                    ? values[i]
                                                    : "";
                                }

                                model.addRow(
                                        row
                                );
                            }

                            return model;
                        }

                        static String runSQLite(
                                String sql
                        ) {

                            try {

                                ProcessBuilder pb =
                                        new ProcessBuilder(
                                                "sqlite3",
                                                "-separator",
                                                "\\u001f",
                                                DB.getAbsolutePath(),
                                                sql
                                        );

                                pb.redirectErrorStream(
                                        true
                                );

                                Process process =
                                        pb.start();

                                String result =
                                        new String(
                                                process
                                                        .getInputStream()
                                                        .readAllBytes(),

                                                StandardCharsets.UTF_8
                                        );

                                process.waitFor();

                                return result;

                            } catch (
                                    Exception ex
                            ) {

                                return "";
                            }
                        }
                    }
                    """.formatted(

                    className,

                    className,

                    generatedAppName,

                    generatedAppName,

                    body.toString());
        }

        // =====================================================
        // JAVA CLASS NAME
        // =====================================================

        private String javaClassName(
                String name) {

            String cleaned = name.replaceAll(
                    "[^A-Za-z0-9_$]",
                    "_");

            if (cleaned.isBlank()) {

                cleaned = "UnitApp";
            }

            if (!Character
                    .isJavaIdentifierStart(
                            cleaned.charAt(
                                    0))) {

                cleaned = "UnitApp_" +
                        cleaned;
            }

            return cleaned;
        }

        // =====================================================
        // JAVA STRING ESCAPING
        // =====================================================

        private String javaString(
                String value) {

            if (value == null) {

                return "";
            }

            return value
                    .replace(
                            "\\",
                            "\\\\")
                    .replace(
                            "\"",
                            "\\\"")
                    .replace(
                            "\n",
                            "\\n")
                    .replace(
                            "\r",
                            "\\r")
                    .replace(
                            "\t",
                            "\\t");
        }

        // =====================================================
        // CREATE JAR
        // =====================================================

        private void createJar(
                Path classesDir,
                Path jarFile,
                String mainClass)
                throws IOException {

            Manifest manifest = new Manifest();

            manifest
                    .getMainAttributes()
                    .put(
                            Attributes.Name.MANIFEST_VERSION,
                            "1.0");

            manifest
                    .getMainAttributes()
                    .put(
                            Attributes.Name.MAIN_CLASS,
                            mainClass);

            try (
                    JarOutputStream jar = new JarOutputStream(
                            Files.newOutputStream(
                                    jarFile),
                            manifest);

                    java.util.stream.Stream<Path> stream = Files.walk(
                            classesDir)) {

                for (Path path : stream
                        .filter(
                                Files::isRegularFile)
                        .toList()) {

                    String entryName = classesDir
                            .relativize(
                                    path)
                            .toString()
                            .replace(
                                    File.separatorChar,
                                    '/');

                    JarEntry entry = new JarEntry(
                            entryName);

                    jar.putNextEntry(
                            entry);

                    Files.copy(
                            path,
                            jar);

                    jar.closeEntry();
                }
            }
        }

        // =====================================================
        // RUN JAR
        // =====================================================

        private void runJar(
                File jar) {

            try {

                new ProcessBuilder(
                        "java",
                        "-jar",
                        jar.getAbsolutePath())
                        .inheritIO()
                        .start();

                SwingUtilities.invokeLater(
                        () -> status.setText(
                                "Running " +
                                        jar.getName()));

            } catch (IOException ex) {

                SwingUtilities.invokeLater(
                        () -> showError(
                                ex));
            }
        }

        // =====================================================
        // DELETE BUILD DIRECTORY
        // =====================================================

        private void deleteTree(
                Path root)
                throws IOException {

            if (!Files.exists(
                    root)) {

                return;
            }

            try (
                    java.util.stream.Stream<Path> stream = Files.walk(
                            root)) {

                List<Path> paths = stream
                        .sorted(
                                Comparator.reverseOrder())
                        .toList();

                for (Path path : paths) {

                    Files.deleteIfExists(
                            path);
                }
            }
        }

        // =====================================================
        // SAVE PROJECT
        // =====================================================

        private void saveProject() {

            if (currentProjectFile == null) {

                saveProjectAs();

                return;
            }

            try {

                writeProject(
                        currentProjectFile);

                status.setText(
                        "Saved: " +
                                currentProjectFile
                                        .getName());

            } catch (Exception ex) {

                showError(
                        ex);
            }
        }

        // =====================================================
        // SAVE AS
        // =====================================================

        private void saveProjectAs() {

            JFileChooser chooser = new JFileChooser();

            chooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "UnitMain Applications",
                            "unitapp"));

            chooser.setSelectedFile(
                    new File(
                            safeName(
                                    appName
                                            .getText())
                                    +
                                    ".unitapp"));

            if (chooser.showSaveDialog(
                    UnitMain.this) != JFileChooser.APPROVE_OPTION) {

                return;
            }

            File file = chooser.getSelectedFile();

            if (!file.getName()
                    .toLowerCase()
                    .endsWith(
                            ".unitapp")) {

                file = new File(
                        file.getParentFile(),

                        file.getName() +
                                ".unitapp");
            }

            currentProjectFile = file;

            saveProject();
        }

        // =====================================================
        // WRITE PROJECT
        // =====================================================

        private void writeProject(
                File file)
                throws IOException {

            Object table = tableSelector
                    .getSelectedItem();

            try (
                    PrintWriter out = new PrintWriter(
                            file,
                            StandardCharsets.UTF_8)) {

                out.println(
                        "UNITAPP=1");

                out.println(
                        "APP=" +
                                encode(
                                        appName
                                                .getText()));

                out.println(
                        "TABLE=" +
                                encode(
                                        table == null
                                                ? ""
                                                : table.toString()));

                for (int i = 0; i < componentModel.size(); i++) {

                    UnitComponent c = componentModel.get(
                            i);

                    out.println(
                            "COMP=" +

                                    encode(
                                            c.type)

                                    +

                                    "|"

                                    +

                                    encode(
                                            c.id)

                                    +

                                    "|"

                                    +

                                    encode(
                                            c.text));
                }
            }
        }

        // =====================================================
        // OPEN PROJECT
        // =====================================================

        private void loadProject() {

            JFileChooser chooser = new JFileChooser();

            chooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "UnitMain Applications",
                            "unitapp"));

            if (chooser.showOpenDialog(
                    UnitMain.this) != JFileChooser.APPROVE_OPTION) {

                return;
            }

            currentProjectFile = chooser.getSelectedFile();

            loadProjectFile(
                    currentProjectFile);
        }

        // =====================================================
        // LOAD PROJECT FILE
        // =====================================================

        private void loadProjectFile(
                File file) {

            try {

                loadingProject = true;

                List<String> lines = Files.readAllLines(
                        file.toPath(),
                        StandardCharsets.UTF_8);

                componentModel.clear();

                String requestedTable = null;

                for (String line : lines) {

                    if (line.startsWith(
                            "APP=")) {

                        appName.setText(
                                decode(
                                        line.substring(
                                                4)));

                    } else if (line.startsWith(
                            "TABLE=")) {

                        requestedTable = decode(
                                line.substring(
                                        6));

                    } else if (line.startsWith(
                            "COMP=")) {

                        String[] parts = line
                                .substring(
                                        5)
                                .split(
                                        "\\|",
                                        -1);

                        if (parts.length == 3) {

                            componentModel.addElement(
                                    new UnitComponent(

                                            decode(
                                                    parts[0]),

                                            decode(
                                                    parts[1]),

                                            decode(
                                                    parts[2])));
                        }
                    }
                }

                refreshTables();

                if (requestedTable != null &&
                        !requestedTable.isBlank()) {

                    tableSelector.setSelectedItem(
                            requestedTable);
                }

                if (!componentModel.isEmpty()) {

                    components.setSelectedIndex(
                            0);
                }

                loadingProject = false;

                rebuildPreview();

                status.setText(
                        "Opened: " +
                                file.getName());

            } catch (Exception ex) {

                loadingProject = false;

                showError(
                        ex);
            }
        }

        // =====================================================
        // STATUS BAR
        // =====================================================

        private JPanel buildStatus() {

            JPanel panel = new JPanel(
                    new BorderLayout());

            panel.setBackground(
                    PANEL);

            panel.setBorder(
                    new CompoundBorder(

                            new MatteBorder(
                                    1,
                                    0,
                                    0,
                                    0,
                                    BORDER),

                            new EmptyBorder(
                                    6,
                                    10,
                                    6,
                                    10)));

            status.setForeground(
                    MUTED);

            JLabel database = new JLabel(
                    DB.getAbsolutePath());

            database.setForeground(
                    MUTED);

            panel.add(
                    status,
                    BorderLayout.WEST);

            panel.add(
                    database,
                    BorderLayout.EAST);

            return panel;
        }
    }

    // =========================================================
    // COMPONENT MODEL
    // =========================================================

    static class UnitComponent {

        String type;

        String id;

        String text;

        UnitComponent(
                String type,
                String id,
                String text) {

            this.type = type;

            this.id = id;

            this.text = text;
        }

        @Override
        public String toString() {

            String value = text == null ||
                    text.isBlank()

                            ? id

                            : text;

            return type +
                    "   •   " +
                    value;
        }
    }

    // =========================================================
    // SQLITE
    // =========================================================

    static String runSQLite(
            String sql) {

        try {

            DB.getParentFile()
                    .mkdirs();

            ProcessBuilder pb = new ProcessBuilder(
                    "sqlite3",
                    "-header",
                    "-column",
                    DB.getAbsolutePath(),
                    sql);

            pb.redirectErrorStream(
                    true);

            Process process = pb.start();

            String output = new String(
                    process
                            .getInputStream()
                            .readAllBytes(),

                    StandardCharsets.UTF_8);

            process.waitFor();

            return output;

        } catch (Exception ex) {

            return "ERROR: " +
                    ex.getMessage();
        }
    }

    static String runSQLiteSeparated(
            String sql) {

        try {

            DB.getParentFile()
                    .mkdirs();

            ProcessBuilder pb = new ProcessBuilder(
                    "sqlite3",
                    "-separator",
                    "\u001f",
                    DB.getAbsolutePath(),
                    sql);

            pb.redirectErrorStream(
                    true);

            Process process = pb.start();

            String output = new String(
                    process
                            .getInputStream()
                            .readAllBytes(),

                    StandardCharsets.UTF_8);

            process.waitFor();

            return output;

        } catch (Exception ex) {

            return "";
        }
    }

    // =========================================================
    // PANEL
    // =========================================================

    private static JPanel createPanel(
            String title) {

        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBackground(
                BG);

        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(
                        BORDER),
                title);

        border.setTitleColor(
                MUTED);

        panel.setBorder(
                border);

        return panel;
    }

    // =========================================================
    // INPUT STYLE
    // =========================================================

    private static void styleInput(
            JTextField field) {

        field.setBackground(
                PANEL_2);

        field.setForeground(
                TEXT);

        field.setCaretColor(
                Color.WHITE);

        field.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        38));

        field.setBorder(
                new CompoundBorder(

                        new LineBorder(
                                BORDER),

                        new EmptyBorder(
                                8,
                                10,
                                8,
                                10)));
    }

    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private static void styleButton(
            JButton button) {

        button.setBackground(
                ACCENT);

        button.setForeground(
                Color.WHITE);

        button.setFocusPainted(
                false);

        button.setBorderPainted(
                false);

        button.setOpaque(
                true);

        button.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        13));

        button.setBorder(
                new EmptyBorder(
                        8,
                        14,
                        8,
                        14));

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            MouseEvent e) {

                        button.setBackground(
                                ACCENT_HOVER);
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent e) {

                        button.setBackground(
                                ACCENT);
                    }
                });
    }

    // =========================================================
    // SECONDARY BUTTON
    // =========================================================

    private static void styleSecondaryButton(
            JButton button) {

        button.setBackground(
                PANEL_2);

        button.setForeground(
                TEXT);

        button.setFocusPainted(
                false);

        button.setOpaque(
                true);

        button.setBorder(
                new CompoundBorder(

                        new LineBorder(
                                BORDER),

                        new EmptyBorder(
                                7,
                                12,
                                7,
                                12)));

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));
    }

    // =========================================================
    // DANGER BUTTON
    // =========================================================

    private static void styleDangerButton(
            JButton button) {

        button.setBackground(
                DANGER);

        button.setForeground(
                Color.WHITE);

        button.setFocusPainted(
                false);

        button.setBorderPainted(
                false);

        button.setOpaque(
                true);

        button.setBorder(
                new EmptyBorder(
                        8,
                        12,
                        8,
                        12));
    }

    // =========================================================
    // DOCUMENT LISTENER
    // =========================================================

    static DocumentListener documentListener(
            Runnable action) {

        return new DocumentListener() {

            @Override
            public void insertUpdate(
                    DocumentEvent e) {

                action.run();
            }

            @Override
            public void removeUpdate(
                    DocumentEvent e) {

                action.run();
            }

            @Override
            public void changedUpdate(
                    DocumentEvent e) {

                action.run();
            }
        };
    }

    // =========================================================
    // SAFE FILE NAME
    // =========================================================

    static String safeName(
            String value) {

        String result = value == null

                ? ""

                : value.replaceAll(
                        "[^A-Za-z0-9_-]",
                        "_");

        return result.isBlank()

                ? "UnitApp"

                : result;
    }

    // =========================================================
    // ENCODE / DECODE
    // =========================================================

    static String encode(
            String value) {

        return Base64
                .getEncoder()
                .encodeToString(
                        (value == null
                                ? ""
                                : value)
                                .getBytes(
                                        StandardCharsets.UTF_8));
    }

    static String decode(
            String value) {

        return new String(
                Base64
                        .getDecoder()
                        .decode(
                                value),

                StandardCharsets.UTF_8);
    }

    // =========================================================
    // ERROR
    // =========================================================

    static void showError(
            Exception ex) {

        JOptionPane.showMessageDialog(
                null,

                ex.getMessage(),

                "UnitMain Error",

                JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================
    // THEME
    // =========================================================

    private static void installTheme() {

        UIManager.put(
                "Panel.background",
                BG);

        UIManager.put(
                "Viewport.background",
                PANEL);

        UIManager.put(
                "Label.foreground",
                TEXT);

        UIManager.put(
                "TabbedPane.background",
                BG);

        UIManager.put(
                "TabbedPane.foreground",
                TEXT);

        UIManager.put(
                "TabbedPane.selected",
                PANEL_2);

        UIManager.put(
                "TextArea.background",
                PANEL);

        UIManager.put(
                "TextArea.foreground",
                TEXT);

        UIManager.put(
                "TextArea.caretForeground",
                Color.WHITE);

        UIManager.put(
                "TextField.background",
                PANEL_2);

        UIManager.put(
                "TextField.foreground",
                TEXT);

        UIManager.put(
                "TextField.caretForeground",
                Color.WHITE);

        UIManager.put(
                "ComboBox.background",
                PANEL_2);

        UIManager.put(
                "ComboBox.foreground",
                TEXT);

        UIManager.put(
                "List.background",
                PANEL);

        UIManager.put(
                "List.foreground",
                TEXT);

        UIManager.put(
                "List.selectionBackground",
                ACCENT);

        UIManager.put(
                "List.selectionForeground",
                Color.WHITE);

        UIManager.put(
                "Table.background",
                PANEL);

        UIManager.put(
                "Table.foreground",
                TEXT);

        UIManager.put(
                "Table.gridColor",
                BORDER);

        UIManager.put(
                "Table.selectionBackground",
                ACCENT);

        UIManager.put(
                "Table.selectionForeground",
                Color.WHITE);

        UIManager.put(
                "TableHeader.background",
                PANEL_2);

        UIManager.put(
                "TableHeader.foreground",
                TEXT);

        UIManager.put(
                "ScrollPane.background",
                BG);

        UIManager.put(
                "ToolBar.background",
                PANEL);

        UIManager.put(
                "CheckBox.background",
                PANEL);

        UIManager.put(
                "CheckBox.foreground",
                TEXT);

        UIManager.put(
                "SplitPane.background",
                BG);

        UIManager.put(
                "TitledBorder.titleColor",
                MUTED);
    }

    // =========================================================
    // START
    // =========================================================

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    installTheme();

                    new UnitMain();
                });
    }
}
