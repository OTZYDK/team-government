
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public final class ItemCatalog {

    // File paths
    private static String csvRelativePath =
            "backend/src/data/items.csv";
    private static String dbRelativePath =
            "backend/src/data/inventory.db";

    // Item catalog and last query result
    static final ArrayList<CatalogItem> ItemCatalog =
            new ArrayList<>();
    static ArrayList<CatalogItem> LastResult =
            new ArrayList<>();

    // Indicates whether initialization succeeded
    private static boolean dbLocated = false;

    // Print the last query result
    static void printLastResult() {


        for (CatalogItem item : LastResult) {
            item.printFields();
        }
    }

    // Print a database row
    private static void printResult(ResultSet rs) {
        try {
            System.out.printf(
                "%d | %.2f | %s | %s | %s%n",
                rs.getInt("ID"),
                rs.getDouble("Price"),
                rs.getString("Description"),
                rs.getString("Category"),
                rs.getString("Tags")
            );
        } catch (Exception e) {
            System.out.println("ERR IN PRINTRESULT");
        }
    }

    // Create the database and initialize the catalog
    private static void LoadCreateDatabase(
            String csvRelativePath,
            String dbRelativePath) {

        Path csvFile = Path.of(csvRelativePath);
        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();

        try {
            Files.createDirectories(dbFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url)) {

            System.out.println(
                "DATABASE FILE: " + dbFile.getFileName());
            System.out.println(
                "DATABASE PATH: " + dbFile);

            // Create the table if it does not exist
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS Items (
                        ID INTEGER PRIMARY KEY,
                        Price REAL,
                        Description TEXT,
                        Category TEXT,
                        Tags TEXT
                    )
                    """);
            }

            // Determine whether the database needs its initial CSV import
            boolean databaseIsEmpty;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM Items")) {

                databaseIsEmpty =
                    rs.next() && rs.getInt(1) == 0;
            }

            // Import CSV data only when the table is empty
            if (databaseIsEmpty) {
                String insertSQL = """
                    INSERT INTO Items
                    (ID, Price, Description, Category, Tags)
                    VALUES (?, ?, ?, ?, ?)
                    """;

                try (BufferedReader reader =
                         Files.newBufferedReader(csvFile);
                     PreparedStatement ps =
                         conn.prepareStatement(insertSQL)) {

                    // Skip the header row
                    reader.readLine();

                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.split(",", -1);

                        if (fields.length != 5) {
                            System.out.println(
                                "Skipping invalid row: " + line);
                            continue;
                        }

                        ps.setInt(
                            1, Integer.parseInt(fields[0].trim()));
                        ps.setDouble(
                            2, Double.parseDouble(fields[1].trim()));
                        ps.setString(3, fields[2]);
                        ps.setString(4, fields[3]);
                        ps.setString(5, fields[4]);

                        ps.executeUpdate();
                    }

                    System.out.println("Initial CSV import completed.");
                }
            } else {
                System.out.println(
                    "Existing catalog found; CSV import skipped.");
            }

            // Load all database rows into the in-memory catalog
            ItemCatalog.clear();

            String query = """
                SELECT ID, Price, Description, Category, Tags
                FROM Items
                ORDER BY ID
                """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                System.out.println("\n--- ALL DATABASE ROWS ---");


                while (rs.next()) {
                    printResult(rs);

                    int id = rs.getInt("ID");
                    double price = rs.getDouble("Price");
                    String description = rs.getString("Description");
                    String category = rs.getString("Category");
                    String tags = rs.getString("Tags");

                    ItemCatalog.add(
                        new CatalogItem(
                            id, price, description, category, tags));
                }
            }

            dbLocated = true;

        } catch (Exception e) {
            System.err.println(
                "ERROR initializing catalog: " + e.getMessage());
            dbLocated = false;
            e.printStackTrace();
        }
    }

    // Initialize using optional CSV and database paths
    public static void init(String... arg) {
        if (dbLocated) {
            return;
        }

        if (arg != null && arg.length == 2
                && arg[0] != null && !arg[0].isBlank()
                && arg[1] != null && !arg[1].isBlank()) {

            LoadCreateDatabase(arg[0], arg[1]);

            if (dbLocated) {
                csvRelativePath = arg[0];
                dbRelativePath = arg[1];
            }
        } else {
            LoadCreateDatabase(
                csvRelativePath, dbRelativePath);
        }
    }

    // Run a SELECT query and return its results
    public static ArrayList<CatalogItem> EnterQuery(
            String arg_query) {

        if (!dbLocated) {
            init();
        }

        if (!dbLocated) {
            return new ArrayList<>();
        }

        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(arg_query)) {

            LastResult = new ArrayList<>();

            System.out.println("\n[-- QUERY --]");
            System.out.println(arg_query);
            System.out.println("\n--- RESULT ---");


            while (rs.next()) {
                printResult(rs);

                int id = rs.getInt("ID");
                double price = rs.getDouble("Price");
                String description = rs.getString("Description");
                String category = rs.getString("Category");
                String tags = rs.getString("Tags");

                LastResult.add(
                    new CatalogItem(
                        id, price, description, category, tags));
            }

            return LastResult;

        } catch (Exception e) {
            System.err.println("ERROR running query: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // Add a catalog item to SQLite.
    public static CatalogItem addItem(
            double price,
            String description,
            String category,
            String tags) throws Exception {

        if (!dbLocated) {
            init();
        }

        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url)) {
            int nextId = 1;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COALESCE(MAX(ID), 0) + 1 FROM Items")) {
                if (rs.next()) {
                    nextId = rs.getInt(1);
                }
            }

            String sql = """
                INSERT INTO Items
                (ID, Price, Description, Category, Tags)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setDouble(2, price);
                ps.setString(3, description);
                ps.setString(4, category);
                ps.setString(5, tags);
                ps.executeUpdate();
            }

            refreshCatalog();

            return new CatalogItem(
                nextId, price, description, category, tags);
        }
    }

    // Update an existing catalog item.
    public static CatalogItem updateItem(
            int id,
            double price,
            String description,
            String category,
            String tags) throws Exception {

        if (!dbLocated) {
            init();
        }

        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = """
                UPDATE Items
                SET Price = ?, Description = ?, Category = ?, Tags = ?
                WHERE ID = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, price);
                ps.setString(2, description);
                ps.setString(3, category);
                ps.setString(4, tags);
                ps.setInt(5, id);

                if (ps.executeUpdate() == 0) {
                    return null;
                }
            }
        }

        refreshCatalog();

        return new CatalogItem(
            id, price, description, category, tags);
    }

    // Delete a catalog item.
    public static CatalogItem deleteItem(int id) throws Exception {
        if (!dbLocated) {
            init();
        }

        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        try (Connection conn = DriverManager.getConnection(url)) {
            CatalogItem existing = null;

            String selectSql = """
                SELECT ID, Price, Description, Category, Tags
                FROM Items WHERE ID = ?
                """;

            try (PreparedStatement ps =
                     conn.prepareStatement(selectSql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existing = new CatalogItem(
                            rs.getInt("ID"),
                            rs.getDouble("Price"),
                            rs.getString("Description"),
                            rs.getString("Category"),
                            rs.getString("Tags")
                        );
                    }
                }
            }

            if (existing == null) {
                return null;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Items WHERE ID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            refreshCatalog();
            return existing;
        }
    }

    // Refresh the in-memory catalog after a database change.
    private static void refreshCatalog() throws Exception {
        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        ArrayList<CatalogItem> refreshed = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT ID, Price, Description, Category, Tags
                 FROM Items ORDER BY ID
                 """)) {

            while (rs.next()) {
                refreshed.add(new CatalogItem(
                    rs.getInt("ID"),
                    rs.getDouble("Price"),
                    rs.getString("Description"),
                    rs.getString("Category"),
                    rs.getString("Tags")
                ));
            }
        }

        ItemCatalog.clear();
        ItemCatalog.addAll(refreshed);
    }

}
