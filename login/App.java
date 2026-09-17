package login;

import java.sql.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.time.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class App {

        private static final String DB = "jdbc:sqlite:app.db";
        private static final Scanner scanner = new Scanner(System.in);
        private static final SecureRandom random = new SecureRandom();
        private static boolean loggedIn = false;

        public static void main(String[] args) throws Exception {
                Class.forName("org.sqlite.JDBC");

                while (true) {
                        System.out.println("""

                                        =========================
                                             UnitDB Accounts
                                        =========================
                                        1. Register
                                        2. Login
                                        3. Exit
                                        """);

                        System.out.print("> ");
                        String choice = scanner.nextLine();

                        switch (choice) {
                                case "1" -> register();
                                case "2" -> login();
                                case "3" -> {
                                        System.out.println("Bye.");
                                        return;
                                }
                                default -> System.out.println("Unknown option.");
                        }

                        if (loggedIn) return;
                }
        }

        // -----------------------------
        // REGISTER
        // -----------------------------

        private static void register() throws Exception {

                System.out.print("Username: ");
                String username = scanner.nextLine().trim();

                System.out.print("Email: ");
                String email = scanner.nextLine().trim();

                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (username.isBlank() || email.isBlank() || password.length() < 6) {
                        System.out.println("Invalid username, email, or password.");
                        return;
                }

                String passwordHash = hashPassword(password);

                String sql = """
                                INSERT INTO users
                                (username, email, password_hash, two_factor_enabled)
                                VALUES (?, ?, ?, 1)
                                """;

                try (
                                Connection conn = DriverManager.getConnection(DB);
                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, username);
                        stmt.setString(2, email);
                        stmt.setString(3, passwordHash);

                        stmt.executeUpdate();

                        System.out.println("Account created.");
                        System.out.println("2FA enabled.");

                } catch (SQLException e) {

                        if (e.getMessage().contains("UNIQUE")) {
                                System.out.println("Username or email already exists.");
                        } else {
                                throw e;
                        }
                }
        }

        // -----------------------------
        // LOGIN
        // -----------------------------

        private static void login() throws Exception {

                System.out.print("Username/email: ");
                String login = scanner.nextLine().trim();

                System.out.print("Password: ");
                String password = scanner.nextLine();

                String sql = """
                                SELECT *
                                FROM users
                                WHERE username = ? OR email = ?
                                """;

                try (
                                Connection conn = DriverManager.getConnection(DB);
                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, login);
                        stmt.setString(2, login);

                        ResultSet rs = stmt.executeQuery();

                        if (!rs.next()) {
                                System.out.println("Account not found.");
                                return;
                        }

                        int userId = rs.getInt("id");

                        String username = rs.getString("username");

                        String storedHash = rs.getString("password_hash");

                        boolean passwordCorrect = verifyPassword(password, storedHash);

                        if (!passwordCorrect) {
                                System.out.println("Wrong password.");
                                return;
                        }

                        boolean twoFactor = rs.getInt("two_factor_enabled") == 1;

                        if (!twoFactor) {
                                loginSuccess(username);
                                return;
                        }

                        String code = create2FACode(userId);

                        /*
                         * TEMPORARY:
                         *
                         * Later this becomes:
                         *
                         * sendEmail(email, code);
                         */

                        System.out.println();
                        System.out.println("======================");
                        System.out.println("2FA CODE: " + code);
                        System.out.println("======================");

                        System.out.print("Enter code: ");

                        String entered = scanner.nextLine().trim();

                        if (verify2FACode(userId, entered)) {

                                loginSuccess(username);

                        } else {

                                System.out.println("Invalid or expired code.");

                        }
                }
        }

        // -----------------------------
        // 2FA
        // -----------------------------

        private static String create2FACode(
                        int userId) throws Exception {

                int number = 100000 + random.nextInt(900000);

                String code = Integer.toString(number);

                String codeHash = sha256(code);

                Instant expires = Instant.now()
                                .plusSeconds(300);

                String sql = """
                                INSERT INTO auth_codes
                                (user_id, code_hash, purpose, expires_at)
                                VALUES (?, ?, 'login_2fa', ?)
                                """;

                try (
                                Connection conn = DriverManager.getConnection(DB);

                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setInt(1, userId);

                        stmt.setString(
                                        2,
                                        codeHash);

                        stmt.setString(
                                        3,
                                        expires.toString());

                        stmt.executeUpdate();
                }

                return code;
        }

        private static boolean verify2FACode(
                        int userId,
                        String code) throws Exception {

                String codeHash = sha256(code);

                String sql = """
                                SELECT id, code_hash, expires_at
                                FROM auth_codes
                                WHERE user_id = ?
                                AND purpose = 'login_2fa'
                                AND used = 0
                                ORDER BY id DESC
                                LIMIT 1
                                """;

                try (
                                Connection conn = DriverManager.getConnection(DB);

                                PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setInt(
                                        1,
                                        userId);

                        ResultSet rs = stmt.executeQuery();

                        if (!rs.next()) {
                                return false;
                        }

                        int codeId = rs.getInt("id");

                        String storedHash = rs.getString("code_hash");

                        Instant expiration = Instant.parse(
                                        rs.getString("expires_at"));

                        if (Instant.now().isAfter(expiration)) {
                                return false;
                        }

                        if (!storedHash.equals(codeHash)) {
                                return false;
                        }

                        markCodeUsed(
                                        conn,
                                        codeId);

                        return true;
                }
        }

        private static void markCodeUsed(
                        Connection conn,
                        int id) throws SQLException {

                try (
                                PreparedStatement stmt = conn.prepareStatement(
                                                """
                                                                UPDATE auth_codes
                                                                SET used = 1
                                                                WHERE id = ?
                                                                """)) {

                        stmt.setInt(
                                        1,
                                        id);

                        stmt.executeUpdate();
                }
        }

        // -----------------------------
        // PASSWORD HASHING
        // -----------------------------

        private static String hashPassword(
                        String password) throws Exception {

                byte[] salt = new byte[16];

                random.nextBytes(salt);

                int iterations = 120_000;

                KeySpec spec = new PBEKeySpec(
                                password.toCharArray(),
                                salt,
                                iterations,
                                256);

                SecretKeyFactory factory = SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256");

                byte[] hash = factory.generateSecret(spec)
                                .getEncoded();

                return iterations
                                + ":"
                                + Base64.getEncoder()
                                                .encodeToString(salt)
                                + ":"
                                + Base64.getEncoder()
                                                .encodeToString(hash);
        }

        private static boolean verifyPassword(
                        String password,
                        String stored) throws Exception {

                String[] parts = stored.split(":");

                if (parts.length != 3) {
                        return false;
                }

                int iterations = Integer.parseInt(parts[0]);

                byte[] salt = Base64.getDecoder()
                                .decode(parts[1]);

                byte[] expected = Base64.getDecoder()
                                .decode(parts[2]);

                KeySpec spec = new PBEKeySpec(
                                password.toCharArray(),
                                salt,
                                iterations,
                                256);

                SecretKeyFactory factory = SecretKeyFactory.getInstance(
                                "PBKDF2WithHmacSHA256");

                byte[] actual = factory.generateSecret(spec)
                                .getEncoded();

                return MessageDigest.isEqual(
                                expected,
                                actual);
        }

        // -----------------------------
        // HASH CODE
        // -----------------------------

        private static String sha256(
                        String value) throws Exception {

                MessageDigest digest = MessageDigest.getInstance(
                                "SHA-256");

                byte[] hash = digest.digest(
                                value.getBytes());

                return Base64.getEncoder()
                                .encodeToString(hash);
        }

        // -----------------------------
        // SUCCESS
        // -----------------------------

        private static void loginSuccess(
                        String username) {

                System.out.println();
                System.out.println(
                                "Welcome, " + username + "!");

                System.out.println(
                                "Login successful.");

                loggedIn = true;
                desktop.UnitMain.main(new String[0]);
        }
}
