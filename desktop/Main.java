package desktop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main extends JFrame {

    static final String DB = System.getProperty("user.home") + "/DB/db/unit.db";

    static final String DATA_JSON = System.getProperty("user.home") + "/DB/db/data.json";

    // ---------------------------------------------------------
    // Main controls
    // ---------------------------------------------------------

    private final JComboBox<String> languageBox = new JComboBox<>(new String[] {
            "en",
            "es",
            "fr",
            "de"
    });

    private final JComboBox<String> themeBox = new JComboBox<>(new String[] {
            "dark",
            "light"
    });

    private final JCheckBox notificationsBox = new JCheckBox("Notifications");

    private final JLabel status = new JLabel("Ready");

    private final JLabel titleLabel = new JLabel("UnitDB Desktop");

    // ---------------------------------------------------------
    // Settings table
    // ---------------------------------------------------------

    private final DefaultTableModel settingsModel = new DefaultTableModel(
            new Object[] { "Key", "Value" },
            0);

    private final JTable settingsTable = new JTable(settingsModel);

    // ---------------------------------------------------------
    // Data / SQL
    // ---------------------------------------------------------

    private final JTextArea dataArea = new JTextArea();

    private final JTextArea sqlInput = new JTextArea();

    private final JTextArea sqlOutput = new JTextArea();

    private final JTextArea jsonArea = new JTextArea();

    private final JLabel databasePathLabel = new JLabel(DB);

    private final JLabel databaseSizeLabel = new JLabel();

    private final JLabel settingsCountLabel = new JLabel();

    private JTabbedPane tabs;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public Main() {

        setTitle("UnitDB Desktop");
        setSize(950, 650);

        setMinimumSize(
                new Dimension(750, 500));

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        buildUI();

        loadSettings();
        loadSettingsTable();
        loadJSON();
        refreshDatabaseInfo();

        applyTheme(
                getValue("theme"));

        applyLanguage(
                getValue("language"));
    }

    // =========================================================
    // UI
    // =========================================================

    private void buildUI() {

        setLayout(new BorderLayout());

        // Header

        JPanel header = new JPanel(
                new BorderLayout());

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        20,
                        15,
                        20));

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        26));

        header.add(
                titleLabel,
                BorderLayout.WEST);

        header.add(
                status,
                BorderLayout.EAST);

        add(
                header,
                BorderLayout.NORTH);

        // Tabs

        tabs = new JTabbedPane();

        tabs.addTab(
                "Home",
                createHomePanel());

        tabs.addTab(
                "Settings",
                createSettingsPanel());

        tabs.addTab(
                "Data",
                createDataPanel());

        tabs.addTab(
                "SQL",
                createSQLPanel());

        tabs.addTab(
                "JSON",
                createJSONPanel());

        tabs.addTab(
                "More",
                createMorePanel());

        add(
                tabs,
                BorderLayout.CENTER);
    }

    // =========================================================
    // HOME
    // =========================================================

    private JPanel createHomePanel() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30,
                        40,
                        30,
                        40));

        JLabel heading = new JLabel("UnitDB Configuration");

        heading.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        22));

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        panel.add(heading);

        panel.add(
                Box.createVerticalStrut(30));

        panel.add(
                createSettingRow(
                        "Language",
                        languageBox));

        panel.add(
                Box.createVerticalStrut(15));

        panel.add(
                createSettingRow(
                        "Theme",
                        themeBox));

        panel.add(
                Box.createVerticalStrut(15));

        JPanel notifyPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT));

        notifyPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        notifyPanel.add(
                notificationsBox);

        panel.add(
                notifyPanel);

        panel.add(
                Box.createVerticalStrut(25));

        JButton save = new JButton(
                "Save Settings");

        save.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        save.addActionListener(
                e -> saveMainSettings());

        panel.add(save);

        return panel;
    }

    private JPanel createSettingRow(
            String name,
            JComponent component) {

        JPanel row = new JPanel(
                new BorderLayout(
                        20,
                        0));

        row.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40));

        row.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(name);

        label.setPreferredSize(
                new Dimension(
                        120,
                        30));

        row.add(
                label,
                BorderLayout.WEST);

        row.add(
                component,
                BorderLayout.CENTER);

        return row;
    }

    // =========================================================
    // SETTINGS EDITOR
    // =========================================================

    private JPanel createSettingsPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        10,
                        10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15));

        settingsTable.setRowHeight(28);

        JScrollPane scroll = new JScrollPane(
                settingsTable);

        panel.add(
                scroll,
                BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT));

        JButton reload = new JButton("Reload");

        JButton add = new JButton("Add");

        JButton save = new JButton("Save All");

        JButton delete = new JButton(
                "Delete Selected");

        reload.addActionListener(
                e -> loadSettingsTable());

        add.addActionListener(e -> {

            settingsModel.addRow(
                    new Object[] {
                            "new_key",
                            "value"
                    });
        });

        save.addActionListener(
                e -> saveSettingsTable());

        delete.addActionListener(
                e -> deleteSelectedSetting());

        buttons.add(reload);
        buttons.add(add);
        buttons.add(save);
        buttons.add(delete);

        panel.add(
                buttons,
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // DATA VIEWER
    // =========================================================

    private JPanel createDataPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        10,
                        10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15));

        dataArea.setEditable(false);

        dataArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14));

        panel.add(
                new JScrollPane(
                        dataArea),
                BorderLayout.CENTER);

        JButton refresh = new JButton(
                "Refresh Data");

        refresh.addActionListener(
                e -> refreshData());

        panel.add(
                refresh,
                BorderLayout.SOUTH);

        refreshData();

        return panel;
    }

    // =========================================================
    // SQL CONSOLE
    // =========================================================

    private JPanel createSQLPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        10,
                        10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15));

        sqlInput.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14));

        sqlOutput.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14));

        sqlOutput.setEditable(false);

        sqlInput.setText(
                "SELECT * FROM settings;");

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT);

        split.setTopComponent(
                new JScrollPane(
                        sqlInput));

        split.setBottomComponent(
                new JScrollPane(
                        sqlOutput));

        split.setResizeWeight(0.4);

        panel.add(
                split,
                BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT));

        JButton execute = new JButton(
                "Run SQL");

        JButton clear = new JButton(
                "Clear");

        execute.addActionListener(e -> {

            String sql = sqlInput
                    .getText()
                    .trim();

            if (sql.isEmpty())
                return;

            sqlOutput.setText(
                    runSQLFormatted(sql));

            loadSettingsTable();
            refreshData();
            refreshDatabaseInfo();
        });

        clear.addActionListener(e -> {
            sqlOutput.setText("");
        });

        buttons.add(execute);
        buttons.add(clear);

        panel.add(
                buttons,
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // JSON
    // =========================================================

    private JPanel createJSONPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(
                        10,
                        10));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15));

        jsonArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14));

        panel.add(
                new JScrollPane(
                        jsonArea),
                BorderLayout.CENTER);

        JPanel buttons = new JPanel();

        JButton reload = new JButton(
                "Reload JSON");

        JButton save = new JButton(
                "Save JSON");

        reload.addActionListener(
                e -> loadJSON());

        save.addActionListener(
                e -> saveJSON());

        buttons.add(reload);
        buttons.add(save);

        panel.add(
                buttons,
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // MORE
    // =========================================================

    private JPanel createMorePanel() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        30,
                        30,
                        30,
                        30));

        panel.add(
                new JLabel(
                        "Database Path"));

        panel.add(
                databasePathLabel);

        panel.add(
                Box.createVerticalStrut(20));

        panel.add(
                new JLabel(
                        "Database Size"));

        panel.add(
                databaseSizeLabel);

        panel.add(
                Box.createVerticalStrut(20));

        panel.add(
                new JLabel(
                        "Settings Count"));

        panel.add(
                settingsCountLabel);

        panel.add(
                Box.createVerticalStrut(30));

        JButton refresh = new JButton(
                "Refresh Info");

        refresh.addActionListener(
                e -> refreshDatabaseInfo());

        panel.add(refresh);

        return panel;
    }

    // =========================================================
    // LOAD MAIN SETTINGS
    // =========================================================

    private void loadSettings() {

        String language = getValue("language");

        String theme = getValue("theme");

        String notifications = getValue(
                "notifications");

        languageBox.setSelectedItem(
                language.isBlank()
                        ? "en"
                        : language);

        themeBox.setSelectedItem(
                theme.isBlank()
                        ? "dark"
                        : theme);

        notificationsBox.setSelected(
                notifications.equalsIgnoreCase(
                        "enabled"));

        status.setText(
                "Loaded from UnitDB");
    }

    private void saveMainSettings() {

        String language = Objects.toString(
                languageBox
                        .getSelectedItem(),
                "en");

        String theme = Objects.toString(
                themeBox
                        .getSelectedItem(),
                "dark");

        String notifications = notificationsBox
                .isSelected()
                        ? "enabled"
                        : "disabled";

        setValue(
                "language",
                language);

        setValue(
                "theme",
                theme);

        setValue(
                "notifications",
                notifications);

        applyTheme(theme);
        applyLanguage(language);

        loadSettingsTable();

        status.setText(
                "Settings saved");
    }

    // =========================================================
    // TABLE
    // =========================================================

    private void loadSettingsTable() {

        settingsModel.setRowCount(0);

        String output = runSQLRaw(
                "SELECT key, value FROM settings ORDER BY key;");

        for (String line : output.split("\n")) {

            if (line.isBlank())
                continue;

            String[] parts = line.split(
                    "\\|",
                    2);

            if (parts.length == 2) {

                settingsModel.addRow(
                        new Object[] {
                                parts[0],
                                parts[1]
                        });
            }
        }
    }

    private void saveSettingsTable() {

        for (int row = 0; row < settingsModel
                .getRowCount(); row++) {

            Object keyObject = settingsModel
                    .getValueAt(
                            row,
                            0);

            Object valueObject = settingsModel
                    .getValueAt(
                            row,
                            1);

            if (keyObject == null)
                continue;

            String key = keyObject
                    .toString()
                    .trim();

            String value = valueObject == null
                    ? ""
                    : valueObject
                            .toString();

            if (key.isEmpty())
                continue;

            setValue(
                    key,
                    value);
        }

        status.setText(
                "Settings table saved");

        loadSettings();
        refreshData();
        refreshDatabaseInfo();
    }

    private void deleteSelectedSetting() {

        int row = settingsTable
                .getSelectedRow();

        if (row < 0) {

            status.setText(
                    "Select a row first");

            return;
        }

        String key = settingsModel
                .getValueAt(
                        row,
                        0)
                .toString();

        int answer = JOptionPane
                .showConfirmDialog(
                        this,
                        "Delete setting '" +
                                key +
                                "'?",
                        "Delete",
                        JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION)
            return;

        runSQLRaw(
                "DELETE FROM settings WHERE key='" +
                        escape(key) +
                        "';");

        loadSettingsTable();
        refreshData();

        status.setText(
                "Deleted " + key);
    }

    // =========================================================
    // DATA
    // =========================================================

    private void refreshData() {

        String result = runSQLFormatted("""
                SELECT
                    key,
                    value
                FROM settings
                ORDER BY key;
                """);

        dataArea.setText(result);
    }

    // =========================================================
    // JSON
    // =========================================================

    private void loadJSON() {

        try {

            Path path = Paths.get(
                    DATA_JSON);

            if (!Files.exists(path)) {

                jsonArea.setText("""
                        {
                          "settings": {
                            "theme": "dark",
                            "language": "en",
                            "notifications": "enabled"
                          }
                        }
                        """);

                return;
            }

            jsonArea.setText(
                    Files.readString(path));

            status.setText(
                    "JSON loaded");

        } catch (IOException e) {

            jsonArea.setText(
                    "ERROR:\n" +
                            e.getMessage());
        }
    }

    private void saveJSON() {

        try {

            Path path = Paths.get(
                    DATA_JSON);

            Files.createDirectories(
                    path.getParent());

            Files.writeString(
                    path,
                    jsonArea.getText());

            status.setText(
                    "data.json saved");

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "JSON Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // DATABASE INFO
    // =========================================================

    private void refreshDatabaseInfo() {

        File file = new File(DB);

        if (file.exists()) {

            long bytes = file.length();

            databaseSizeLabel.setText(
                    formatSize(bytes));

        } else {

            databaseSizeLabel.setText(
                    "Database not found");
        }

        String count = runSQLRaw(
                "SELECT COUNT(*) FROM settings;").trim();

        settingsCountLabel.setText(
                count.isBlank()
                        ? "0"
                        : count);
    }

    private String formatSize(long bytes) {

        if (bytes < 1024)
            return bytes + " B";

        double kb = bytes / 1024.0;

        if (kb < 1024)
            return String.format(
                    "%.2f KB",
                    kb);

        return String.format(
                "%.2f MB",
                kb / 1024.0);
    }

    // =========================================================
    // DATABASE
    // =========================================================

    private String getValue(
            String key) {

        return runSQLRaw("""
                SELECT value
                FROM settings
                WHERE key='%s';
                """.formatted(
                escape(key))).trim();
    }

    private void setValue(
            String key,
            String value) {

        runSQLRaw("""
                INSERT INTO settings(key, value)

                VALUES('%s', '%s')

                ON CONFLICT(key)

                DO UPDATE SET
                value=excluded.value;
                """.formatted(
                escape(key),
                escape(value)));
    }

    private String runSQLRaw(
            String sql) {

        return executeSQLite(
                false,
                sql);
    }

    private String runSQLFormatted(
            String sql) {

        return executeSQLite(
                true,
                sql);
    }

    private String executeSQLite(
            boolean formatted,
            String sql) {

        try {

            ProcessBuilder builder;

            if (formatted) {

                builder = new ProcessBuilder(
                        "sqlite3",
                        "-header",
                        "-column",
                        DB,
                        sql);

            } else {

                builder = new ProcessBuilder(
                        "sqlite3",
                        DB,
                        sql);
            }

            builder.redirectErrorStream(
                    true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process
                                    .getInputStream()));

            StringBuilder output = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {

                output
                        .append(line)
                        .append("\n");
            }

            int code = process.waitFor();

            if (code != 0) {

                status.setText(
                        "Database error");
            }

            return output.toString();

        } catch (Exception e) {

            status.setText(
                    "Database error");

            return e.getMessage();
        }
    }

    private String escape(
            String text) {

        return text.replace(
                "'",
                "''");
    }

    // =========================================================
    // LANGUAGE
    // =========================================================

    private void applyLanguage(
            String language) {

        switch (language) {

            case "es" -> {

                titleLabel.setText(
                        "Escritorio UnitDB");

                tabs.setTitleAt(
                        0,
                        "Inicio");

                tabs.setTitleAt(
                        1,
                        "Configuración");

                tabs.setTitleAt(
                        2,
                        "Datos");

                tabs.setTitleAt(
                        3,
                        "SQL");

                tabs.setTitleAt(
                        4,
                        "JSON");

                tabs.setTitleAt(
                        5,
                        "Más");
            }

            case "fr" -> {

                titleLabel.setText(
                        "Bureau UnitDB");

                tabs.setTitleAt(
                        0,
                        "Accueil");

                tabs.setTitleAt(
                        1,
                        "Paramètres");

                tabs.setTitleAt(
                        2,
                        "Données");

                tabs.setTitleAt(
                        3,
                        "SQL");

                tabs.setTitleAt(
                        4,
                        "JSON");

                tabs.setTitleAt(
                        5,
                        "Plus");
            }

            case "de" -> {

                titleLabel.setText(
                        "UnitDB Desktop");

                tabs.setTitleAt(
                        0,
                        "Start");

                tabs.setTitleAt(
                        1,
                        "Einstellungen");

                tabs.setTitleAt(
                        2,
                        "Daten");

                tabs.setTitleAt(
                        3,
                        "SQL");

                tabs.setTitleAt(
                        4,
                        "JSON");

                tabs.setTitleAt(
                        5,
                        "Mehr");
            }

            default -> {

                titleLabel.setText(
                        "UnitDB Desktop");

                tabs.setTitleAt(
                        0,
                        "Home");

                tabs.setTitleAt(
                        1,
                        "Settings");

                tabs.setTitleAt(
                        2,
                        "Data");

                tabs.setTitleAt(
                        3,
                        "SQL");

                tabs.setTitleAt(
                        4,
                        "JSON");

                tabs.setTitleAt(
                        5,
                        "More");
            }
        }
    }

    // =========================================================
    // THEME
    // =========================================================

    private void applyTheme(
            String theme) {

        boolean dark = !"light"
                .equalsIgnoreCase(
                        theme);

        Color background = dark
                ? new Color(
                        28,
                        30,
                        34)
                : new Color(
                        245,
                        245,
                        245);

        Color foreground = dark
                ? new Color(
                        235,
                        235,
                        235)
                : new Color(
                        25,
                        25,
                        25);

        Color field = dark
                ? new Color(
                        42,
                        45,
                        50)
                : Color.WHITE;

        applyColors(
                getContentPane(),
                background,
                foreground,
                field);

        repaint();
    }

    private void applyColors(
            Component component,
            Color background,
            Color foreground,
            Color field) {

        if (component instanceof JTextArea ||
                component instanceof JTextField ||
                component instanceof JTable) {

            component.setBackground(
                    field);

            component.setForeground(
                    foreground);

        } else {

            component.setBackground(
                    background);

            component.setForeground(
                    foreground);
        }

        if (component instanceof Container container) {

            for (Component child : container
                    .getComponents()) {

                applyColors(
                        child,
                        background,
                        foreground,
                        field);
            }
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    Main app = new Main();

                    app.setVisible(true);
                });
    }
}
