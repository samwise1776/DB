package DSA;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DSA extends JFrame {

    private static final Color BG = new Color(18, 20, 26);
    private static final Color PANEL = new Color(27, 30, 38);
    private static final Color PANEL2 = new Color(35, 39, 49);
    private static final Color TEXT = new Color(235, 238, 245);
    private static final Color MUTED = new Color(150, 156, 170);
    private static final Color ACCENT = new Color(92, 120, 255);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Tool", "Status", "Version", "Package" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextArea console = new JTextArea();
    private final JLabel status = new JLabel("Ready");

    private final List<DevTool> tools = new ArrayList<>();

    public DSA() {
        super("DSA — Dev Setup Application");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        createTools();
        buildUI();
        scan();

        setVisible(true);
    }

    private void createTools() {
        tools.add(new DevTool(
                "Java JDK",
                "javac",
                "javac -version",
                "default-jdk"));

        tools.add(new DevTool(
                "Python",
                "python3",
                "python3 --version",
                "python3"));

        tools.add(new DevTool(
                "pip",
                "pip3",
                "pip3 --version",
                "python3-pip"));

        tools.add(new DevTool(
                "Git",
                "git",
                "git --version",
                "git"));

        tools.add(new DevTool(
                "GCC",
                "gcc",
                "gcc --version",
                "gcc"));

        tools.add(new DevTool(
                "G++",
                "g++",
                "g++ --version",
                "g++"));

        tools.add(new DevTool(
                "Make",
                "make",
                "make --version",
                "make"));

        tools.add(new DevTool(
                "Node.js",
                "node",
                "node --version",
                "nodejs"));

        tools.add(new DevTool(
                "npm",
                "npm",
                "npm --version",
                "npm"));

        tools.add(new DevTool(
                "SQLite",
                "sqlite3",
                "sqlite3 --version",
                "sqlite3"));

        tools.add(new DevTool(
                "GDB",
                "gdb",
                "gdb --version",
                "gdb"));

        tools.add(new DevTool(
                "CMake",
                "cmake",
                "cmake --version",
                "cmake"));
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Dashboard", buildDashboard());
        tabs.addTab("Tools", buildToolsPage());
        tabs.addTab("Presets", buildPresets());
        tabs.addTab("Console", buildConsole());

        root.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(PANEL);
        bottom.setBorder(new EmptyBorder(6, 10, 6, 10));

        status.setForeground(MUTED);

        bottom.add(status, BorderLayout.WEST);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("DSA");
        title.setForeground(TEXT);
        title.setFont(new Font(
                Font.SANS_SERIF,
                Font.BOLD,
                26));

        JLabel subtitle = new JLabel(
                "Developer Setup Application");

        subtitle.setForeground(MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(
                text,
                BoxLayout.Y_AXIS));

        text.add(title);
        text.add(subtitle);

        JButton rescan = new JButton("↻ Scan System");
        styleButton(rescan);

        rescan.addActionListener(e -> scan());

        panel.add(text, BorderLayout.WEST);
        panel.add(rescan, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(
                new GridLayout(2, 3, 12, 12));

        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(
                20, 20, 20, 20));

        panel.add(card(
                "Languages",
                "Java, Python, C, C++, JavaScript",
                "Detect and configure compilers"));

        panel.add(card(
                "Build Tools",
                "Make, CMake, javac",
                "Prepare projects for compiling"));

        panel.add(card(
                "Databases",
                "SQLite",
                "Set up local development data"));

        panel.add(card(
                "Version Control",
                "Git",
                "Prepare repositories and projects"));

        panel.add(card(
                "System Check",
                System.getProperty("os.name"),
                System.getProperty("os.arch")));

        panel.add(card(
                "UnitMain",
                "Database + IDE",
                "Future DSA integration"));

        return panel;
    }

    private JPanel card(
            String title,
            String main,
            String description) {
        JPanel card = new JPanel();
        card.setBackground(PANEL);
        card.setBorder(new EmptyBorder(
                18, 18, 18, 18));

        card.setLayout(new BoxLayout(
                card,
                BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(new Font(
                Font.SANS_SERIF,
                Font.BOLD,
                18));

        JLabel mainLabel = new JLabel(main);
        mainLabel.setForeground(ACCENT);
        mainLabel.setFont(new Font(
                Font.SANS_SERIF,
                Font.BOLD,
                15));

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setForeground(MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(mainLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    private JPanel buildToolsPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(
                12, 12, 12, 12));

        table.setRowHeight(30);
        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setGridColor(PANEL2);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        buttons.setBackground(BG);

        JButton installSelected = new JButton("Install Selected");

        JButton installMissing = new JButton("Install All Missing");

        JButton refresh = new JButton("Refresh");

        styleButton(installSelected);
        styleButton(installMissing);
        styleSecondary(refresh);

        buttons.add(installSelected);
        buttons.add(installMissing);
        buttons.add(refresh);

        installSelected.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                return;
            }

            installPackage(
                    tools.get(row).packageName);
        });

        installMissing.addActionListener(e -> {
            List<String> packages = new ArrayList<>();

            for (DevTool tool : tools) {
                if (!exists(tool.command)) {
                    packages.add(tool.packageName);
                }
            }

            if (!packages.isEmpty()) {
                installPackages(packages);
            }
        });

        refresh.addActionListener(e -> scan());

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildPresets() {
        JPanel root = new JPanel();

        root.setBackground(BG);
        root.setBorder(new EmptyBorder(
                20, 20, 20, 20));

        root.setLayout(new GridLayout(
                0, 2, 12, 12));

        root.add(preset(
                "Java Developer",
                "JDK + Git + SQLite + Make",
                List.of(
                        "default-jdk",
                        "git",
                        "sqlite3",
                        "make")));

        root.add(preset(
                "Python Developer",
                "Python + pip + Git + SQLite",
                List.of(
                        "python3",
                        "python3-pip",
                        "python3-venv",
                        "git",
                        "sqlite3")));

        root.add(preset(
                "C / C++ Developer",
                "GCC + G++ + Make + CMake + GDB",
                List.of(
                        "gcc",
                        "g++",
                        "make",
                        "cmake",
                        "gdb",
                        "git")));

        root.add(preset(
                "Web Developer",
                "Node + npm + Git",
                List.of(
                        "nodejs",
                        "npm",
                        "git")));

        root.add(preset(
                "Data Developer",
                "Python + SQLite + Git",
                List.of(
                        "python3",
                        "python3-pip",
                        "sqlite3",
                        "git")));

        root.add(preset(
                "Full Developer Setup",
                "Install the entire DSA starter stack",
                List.of(
                        "default-jdk",
                        "python3",
                        "python3-pip",
                        "python3-venv",
                        "git",
                        "gcc",
                        "g++",
                        "make",
                        "cmake",
                        "gdb",
                        "nodejs",
                        "npm",
                        "sqlite3")));

        return root;
    }

    private JPanel preset(
            String name,
            String description,
            List<String> packages) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(
                18, 18, 18, 18));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(
                text,
                BoxLayout.Y_AXIS));

        JLabel title = new JLabel(name);
        title.setForeground(TEXT);
        title.setFont(new Font(
                Font.SANS_SERIF,
                Font.BOLD,
                17));

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setForeground(MUTED);

        text.add(title);
        text.add(Box.createVerticalStrut(7));
        text.add(descriptionLabel);

        JButton install = new JButton("Set Up");

        styleButton(install);

        install.addActionListener(
                e -> installPackages(packages));

        panel.add(text, BorderLayout.CENTER);
        panel.add(install, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildConsole() {
        JPanel panel = new JPanel(new BorderLayout());

        console.setEditable(false);
        console.setFont(new Font(
                Font.MONOSPACED,
                Font.PLAIN,
                14));

        console.setBackground(new Color(
                15, 17, 22));

        console.setForeground(TEXT);

        panel.add(
                new JScrollPane(console),
                BorderLayout.CENTER);

        return panel;
    }

    private void scan() {
        status.setText("Scanning system...");
        model.setRowCount(0);

        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();

            for (DevTool tool : tools) {
                boolean installed = exists(tool.command);

                String version = installed
                        ? firstLine(
                                run(tool.versionCommand))
                        : "-";

                rows.add(new Object[] {
                        tool.name,
                        installed
                                ? "Installed ✓"
                                : "Missing",
                        version,
                        tool.packageName
                });
            }

            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);

                for (Object[] row : rows) {
                    model.addRow(row);
                }

                status.setText(
                        "System scan complete");
            });
        }).start();
    }

    private boolean exists(String command) {
        try {
            Process process = new ProcessBuilder(
                    "sh",
                    "-c",
                    "command -v " + command).start();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    private String run(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "sh",
                    "-c",
                    command);

            pb.redirectErrorStream(true);

            Process process = pb.start();

            String result = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            process.waitFor();

            return result.trim();

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String firstLine(String text) {
        if (text == null || text.isBlank()) {
            return "Unknown";
        }

        return text.split("\\R")[0];
    }

    private void installPackage(String packageName) {
        installPackages(List.of(packageName));
    }

    private void installPackages(List<String> packages) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Install:\n\n" +
                        String.join("\n", packages) +
                        "\n\nContinue?",
                "DSA Installer",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        List<String> command = new ArrayList<>();

        command.add("pkexec");
        command.add("apt-get");
        command.add("install");
        command.add("-y");
        command.addAll(packages);

        execute(command);
    }

    private void execute(List<String> command) {
        console.append(
                "\nDSA > " +
                        String.join(" ", command) +
                        "\n\n");

        status.setText("Running setup...");

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);

                pb.redirectErrorStream(true);

                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                process.getInputStream()))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        String output = line;

                        SwingUtilities.invokeLater(() -> {
                            console.append(
                                    output + "\n");

                            console.setCaretPosition(
                                    console.getDocument()
                                            .getLength());
                        });
                    }
                }

                int code = process.waitFor();

                SwingUtilities.invokeLater(() -> {
                    status.setText(
                            code == 0
                                    ? "Setup complete"
                                    : "Setup exited with " + code);

                    scan();
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    console.append(
                            "\nERROR: " +
                                    e.getMessage() +
                                    "\n");

                    status.setText(
                            "Setup failed");
                });
            }
        }).start();
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setBorder(
                new EmptyBorder(
                        9, 15, 9, 15));
    }

    private void styleSecondary(JButton button) {
        button.setBackground(PANEL2);
        button.setForeground(TEXT);
        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(
                        9, 15, 9, 15));
    }

    private static class DevTool {
        final String name;
        final String command;
        final String versionCommand;
        final String packageName;

        DevTool(
                String name,
                String command,
                String versionCommand,
                String packageName) {
            this.name = name;
            this.command = command;
            this.versionCommand = versionCommand;
            this.packageName = packageName;
        }
    }

    private static void installTheme() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("TableHeader.background", PANEL2);
        UIManager.put("TableHeader.foreground", TEXT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installTheme();
            new DSA();
        });
    }
}
