package db;

import java.io.*;
import java.util.*;

public class conf {

    static final String DB = System.getProperty("user.home") + "/DB/db/unit.db";

    static boolean lastSqlOk = true;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("""
                UnitDB Language v0.2
                -------------------
                Database: %s

                Type HELP for commands.
                """.formatted(DB));

        while (true) {

            System.out.print("unit> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            runCommand(line);
        }
    }

    // =========================================================
    // COMMAND PARSER
    // =========================================================

    static void runCommand(String line) {

        String[] parts = line.split("\\s+");

        String command = parts[0].toUpperCase();

        switch (command) {

            // SETTINGS
            case "PRINT" ->
                printCommand(parts);

            case "SET" ->
                setCommand(parts);

            // RECORDS
            case "CREATE" ->
                createCommand(line, parts);

            case "GET" ->
                getCommand(parts);

            case "FIND" ->
                findCommand(parts);

            case "UPDATE" ->
                updateCommand(line, parts);

            case "DELETE" ->
                deleteCommand(parts);

            // DATABASE
            case "SHOW" ->
                showCommand(parts);

            case "COUNT" ->
                countCommand(parts);

            case "SQL" ->
                rawSQL(line);

            case "HELP" ->
                help();

            case "CLEAR" -> {

                System.out.print("\033[H\033[2J");
                System.out.flush();
            }

            case "EXIT", "QUIT" -> {

                System.out.println("Goodbye.");
                System.exit(0);
            }

            default ->
                System.out.println(
                        "Unknown command: " + command +
                                "\nType HELP.");
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    static void printCommand(String[] parts) {

        if (parts.length < 2) {

            System.out.println(
                    "Usage: PRINT <key>");

            return;
        }

        String key = parts[1].toLowerCase();

        String query = """
                SELECT value
                FROM settings
                WHERE key='%s';
                """.formatted(
                escape(key));

        String result = sql(query);

        if (result.isBlank()) {

            System.out.println("NULL");

        } else {

            System.out.println(result);
        }
    }

    static void setCommand(String[] parts) {

        if (parts.length < 3) {

            System.out.println(
                    "Usage: SET <key> <value>");

            return;
        }

        String key = parts[1].toLowerCase();

        String value = join(parts, 2);

        String query = """
                INSERT INTO settings(key, value)
                VALUES('%s', '%s')

                ON CONFLICT(key)
                DO UPDATE SET
                value=excluded.value;
                """.formatted(
                escape(key),
                escape(value));

        sql(query);

        if (!lastSqlOk) {
            return;
        }

        System.out.println(
                key + " = " + value);
    }

    // =========================================================
    // CREATE RECORD
    //
    // CREATE user Raymond
    // CREATE user Raymond {"level":42}
    // =========================================================

    static void createCommand(
            String original,
            String[] parts) {

        if (parts.length < 3) {

            System.out.println("""
                    Usage:
                    CREATE <type> <name>
                    CREATE <type> <name> <data>
                    """);

            return;
        }

        String type = parts[1];

        String name = parts[2];

        String data = "";

        if (parts.length > 3) {

            data = join(parts, 3);
        }

        String query = """
                INSERT INTO records(
                    type,
                    name,
                    data
                )
                VALUES(
                    '%s',
                    '%s',
                    '%s'
                );
                """.formatted(
                escape(type),
                escape(name),
                escape(data));

        sql(query);

        if (!lastSqlOk) {
            return;
        }

        System.out.println(
                "Created " +
                        type + " " +
                        name);
    }

    // =========================================================
    // GET RECORD
    //
    // GET user Raymond
    // =========================================================

    static void getCommand(
            String[] parts) {

        if (parts.length < 3) {

            System.out.println(
                    "Usage: GET <type> <name>");

            return;
        }

        String type = parts[1];

        String name = parts[2];

        String query = """
                SELECT
                    id || ' | ' ||
                    type || ' | ' ||
                    name || ' | ' ||
                    COALESCE(data, '') || ' | ' ||
                    created_at

                FROM records

                WHERE type='%s'
                AND name='%s';
                """.formatted(
                escape(type),
                escape(name));

        String result = sql(query);

        if (result.isBlank()) {

            System.out.println(
                    "Record not found.");

            return;
        }

        System.out.println(result);
    }

    // =========================================================
    // FIND
    //
    // FIND user
    // FIND ALL
    // =========================================================

    static void findCommand(
            String[] parts) {

        if (parts.length < 2) {

            System.out.println(
                    "Usage: FIND <type>");

            return;
        }

        if (parts[1]
                .equalsIgnoreCase("ALL")) {

            showRecords();
            return;
        }

        String type = parts[1];

        String query = """
                SELECT
                    id || ' | ' ||
                    type || ' | ' ||
                    name || ' | ' ||
                    COALESCE(data, '')

                FROM records

                WHERE type='%s'

                ORDER BY id;
                """.formatted(
                escape(type));

        String result = sql(query);

        if (result.isBlank()) {

            System.out.println(
                    "No records.");

        } else {

            System.out.println(result);
        }
    }

    // =========================================================
    // UPDATE
    //
    // UPDATE user Raymond {"level":99}
    // =========================================================

    static void updateCommand(
            String original,
            String[] parts) {

        if (parts.length < 4) {

            System.out.println(
                    "Usage: UPDATE <type> <name> <data>");

            return;
        }

        String type = parts[1];

        String name = parts[2];

        String data = join(parts, 3);

        String query = """
                UPDATE records

                SET data='%s'

                WHERE type='%s'
                AND name='%s';
                """.formatted(
                escape(data),
                escape(type),
                escape(name));

        sql(query);

        if (!lastSqlOk) {
            return;
        }

        System.out.println(
                "Updated " +
                        type + " " +
                        name);
    }

    // =========================================================
    // DELETE
    //
    // DELETE user Raymond
    // =========================================================

    static void deleteCommand(
            String[] parts) {

        if (parts.length < 3) {

            System.out.println(
                    "Usage: DELETE <type> <name>");

            return;
        }

        String type = parts[1];

        String name = parts[2];

        String query = """
                DELETE FROM records

                WHERE type='%s'
                AND name='%s';
                """.formatted(
                escape(type),
                escape(name));

        sql(query);

        if (!lastSqlOk) {
            return;
        }

        System.out.println(
                "Deleted " +
                        type + " " +
                        name);
    }

    // =========================================================
    // SHOW
    // =========================================================

    static void showCommand(
            String[] parts) {

        if (parts.length < 2) {

            System.out.println("""
                    SHOW SETTINGS
                    SHOW RECORDS
                    SHOW TABLES
                    SHOW ALL
                    """);

            return;
        }

        String target = parts[1].toUpperCase();

        switch (target) {

            case "SETTINGS" -> {

                String result = sql("""
                        SELECT
                            key || ' = ' || value

                        FROM settings

                        ORDER BY key;
                        """);

                System.out.println(result);
            }

            case "RECORDS" ->
                showRecords();

            case "TABLES" -> {

                String result = sql("""
                        SELECT name
                        FROM sqlite_master
                        WHERE type='table'
                        ORDER BY name;
                        """);

                System.out.println(result);
            }

            case "ALL" -> {

                System.out.println(
                        "\n--- SETTINGS ---");

                System.out.println(
                        sql("""
                                SELECT
                                key || ' = ' || value
                                FROM settings;
                                """));

                System.out.println(
                        "\n--- RECORDS ---");

                showRecords();
            }

            default ->
                System.out.println(
                        "Unknown SHOW target.");
        }
    }

    static void showRecords() {

        String result = sql("""
                SELECT
                    id || ' | ' ||
                    type || ' | ' ||
                    name || ' | ' ||
                    COALESCE(data, '')

                FROM records

                ORDER BY id;
                """);

        if (result.isBlank()) {

            System.out.println(
                    "No records.");

        } else {

            System.out.println(result);
        }
    }

    // =========================================================
    // COUNT
    //
    // COUNT RECORDS
    // COUNT SETTINGS
    // COUNT user
    // =========================================================

    static void countCommand(
            String[] parts) {

        if (parts.length < 2) {

            System.out.println(
                    "Usage: COUNT <target>");

            return;
        }

        String target = parts[1];

        String query;

        if (target.equalsIgnoreCase(
                "RECORDS")) {

            query = "SELECT COUNT(*) FROM records;";

        } else if (target.equalsIgnoreCase(
                "SETTINGS")) {

            query = "SELECT COUNT(*) FROM settings;";

        } else {

            query = """
                    SELECT COUNT(*)
                    FROM records
                    WHERE type='%s';
                    """.formatted(
                    escape(target));
        }

        System.out.println(
                sql(query));
    }

    // =========================================================
    // RAW SQL
    //
    // SQL SELECT * FROM settings;
    // =========================================================

    static void rawSQL(
            String line) {

        if (line.length() <= 4) {

            System.out.println(
                    "Usage: SQL <query>");

            return;
        }

        String query = line.substring(4);

        String result = sql(query);

        if (!result.isBlank()) {

            System.out.println(result);
        }
    }

    // =========================================================
    // SQLITE
    // =========================================================

    static String sql(
            String query) {

        lastSqlOk = false;

        try {

            ProcessBuilder builder = new ProcessBuilder(
                    "sqlite3",
                    DB,
                    query);

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

                if (!output.isEmpty()) {

                    output.append(
                            "\n");
                }

                output.append(line);
            }

            int code = process.waitFor();

            if (code != 0) {

                System.out.println(
                        "SQLite error:\n" +
                                output);

                return "";
            }

            lastSqlOk = true;

            return output.toString();

        } catch (Exception e) {

            System.out.println(
                    "Database error: " +
                            e.getMessage());

            return "";
        }
    }

    // =========================================================
    // UTILITIES
    // =========================================================

    static String escape(
            String text) {

        return text.replace(
                "'",
                "''");
    }

    static String join(
            String[] parts,
            int start) {

        StringBuilder builder = new StringBuilder();

        for (int i = start; i < parts.length; i++) {

            if (i > start) {

                builder.append(" ");
            }

            builder.append(
                    parts[i]);
        }

        return builder.toString();
    }

    // =========================================================
    // HELP
    // =========================================================

    static void help() {

        System.out.println("""

                ╔══════════════════════════════════════╗
                ║         UnitDB Language v0.2        ║
                ╚══════════════════════════════════════╝

                SETTINGS

                  PRINT <key>
                    PRINT LANGUAGE
                    PRINT THEME

                  SET <key> <value>
                    SET LANGUAGE fr
                    SET THEME dark


                RECORDS

                  CREATE <type> <name>
                    CREATE user User*

                  CREATE <type> <name> <data>
                    CREATE user User* {"level":42}

                  GET <type> <name>
                    GET user User*

                  FIND <type>
                    FIND user

                  FIND ALL
                    Show every record

                  UPDATE <type> <name> <data>
                    UPDATE user User* {"level":100}

                  DELETE <type> <name>
                    DELETE user User*


                DATABASE

                  SHOW SETTINGS
                  SHOW RECORDS
                  SHOW TABLES
                  SHOW ALL

                  COUNT SETTINGS
                  COUNT RECORDS
                  COUNT <type>

                  SQL <query>
                    SQL SELECT * FROM records;


                SYSTEM

                  CLEAR
                  HELP
                  EXIT

                """);
    }
}
