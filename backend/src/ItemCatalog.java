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

/*
LOAD - CREATE CATALOG
This program will load the CSV file and create the Complete Item Catalog
the item catalog will be a static class/object that holds the total catalog
and includes functions to easily push queries onto it


ResultSet Navigation Methods:

    next(): Used to move to the next row in the ResultSet.
    previous(): Used for move to previous row in the ResultSet
    first(): Used for move to first row in the ResultSet
    last(): Used for move to last row in the ResultSet
    absolute(int row): Used to move to specific row
    relative(int rows): Used for Moves forward or backward by the specified number of rows
    beforeFirst(): Used for Positions the cursor before the first row
    afterLast(): Used for Positions the cursor after the last row
    
*/


public final class ItemCatalog {
	
	//---------VARIABLES
	//File Paths
	private static String csvRelativePath = "backend\\src\\data\\items.csv";
	private static String dbRelativePath = "backend\\src\\data\\inventory.db";
	
	//Item Catalog and Last Result
	final static ArrayList<CatalogItem> ItemCatalog = new ArrayList<CatalogItem>(); 
	static ArrayList<CatalogItem> LastResult = new ArrayList<CatalogItem>(); 
	
	//Database Located - indicates initialization
	static private Boolean dbLocated = false;
	
	

	//---------PRINTERS
	//PRINT LAST RESULT SAVED
	static void printLastResult() {
		util.fieldHeader();
		for (CatalogItem i: LastResult) {i.printFields();}
	}
	
	//PRIVATE, PRINTS FROM DATASET, FOR PRINTING QUERY RESULTS
	private static void printResult(ResultSet rs) 
	{
		try {
			System.out.printf(
			        "%d | %.2f | %s | %s | %s%n",
			        rs.getInt("ID"),
			        rs.getDouble("Price"),
			        rs.getString("Description"),
			        rs.getString("Category"),
			        rs.getString("Tags")
			    );
			}
			catch(Exception e) {
				System.out.println("ERR IN PRINTRESULT");
			}
	}

	
	
	
	
	//---------CREATE DATABASE AND INITIALIZE ITEM CATALOG
	//Create Database, requires path arguments
    private static void LoadCreateDatabase(String csvRelativePath, String dbRelativePath) {
    	
    	
    	//Relative Path of CSV
        Path csvFile = Path.of(csvRelativePath);
        
        //Relative Path of DB (may not exist)
        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        
        //Try to Create data directory
        try {Files.createDirectories(dbFile.getParent());} 
        catch (IOException e) {e.printStackTrace();}

        //Database URL
        String url = "jdbc:sqlite:" + dbFile;

        try {
            // Create or open the SQLite database.
            try (Connection conn = DriverManager.getConnection(url)) {

                System.out.println("FILE CREATED (file name): " +dbFile.getFileName());
                System.out.println("FILE CREATED (file path): " +dbFile);

                //CREATE THE TABLE (STATEMENT)
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

                //Import CSV rows into SQLite.
                String insertSQL = """
                    INSERT OR REPLACE INTO Items
                    (ID, Price, Description, Category, Tags)
                    VALUES (?, ?, ?, ?, ?)
                    """;

                try (	//Try to open CSV and make empty statement
                		BufferedReader reader = Files.newBufferedReader(csvFile);
                		PreparedStatement ps = conn.prepareStatement(insertSQL)
                	) 
                {

                    // Skip the header row.
                    reader.readLine();

                    //Hold current line
                    String line;

                    //While there are valid lines left in the SQL file
                    while ((line = reader.readLine()) != null) 
                    {
                    	//split with -1 preserves trailing space after ,
                        String[] fields = line.split(",", -1);
                        
                        //Check for too many fields
                        if (fields.length != 5) {
                            System.out.println("Skipping invalid row: " + line);
                            continue;
                        }

                        ps.setInt(1, Integer.parseInt(fields[0].trim()));
                        ps.setDouble(2, Double.parseDouble(fields[1].trim()));
                        ps.setString(3, fields[2]);
                        ps.setString(4, fields[3]);
                        ps.setString(5, fields[4]);

                        ps.executeUpdate();
                    }
                    
                    
                }
                
                //PRINT ALL ROWS TO ITEM CATALOG LIST
                String query = """
                    SELECT ID, Price, Description, Category, Tags
                    FROM Items
                    ORDER BY ID
                    """;

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    System.out.println("\n--- ALL DATABASE ROWS ---");
                    util.fieldHeader();

                    while (rs.next()) {
                    	printResult(rs); 
                    	
                        int ID = rs.getInt("ID");
                        double Price = rs.getDouble("Price");
                        String desc = rs.getString("Description");
                        String category = rs.getString("Category");
                        String tags = rs.getString("Tags");
                		
                		ItemCatalog.add(new CatalogItem(ID,Price,desc,category,tags));
                    }
                    
                    dbLocated = true;
                }
   
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            dbLocated = false;
            e.printStackTrace();
        }
    }
    
    //Init function, runs loadcreatedatabase with default values (no argument)
    //Accepts arguments for: 
    //	Relative path of csv file (string)
    //	Relative path of db file (string)
	public static void init(String... arg) {
		//If the database has not been located (initialized), run init code
		if (dbLocated == false) 
		{
			//If the two arguments are provided
			if (arg.length == 2)
			{
				try 
				{
					//Create Path variables
					String csvpath = arg[0];
					String dbpath = arg[1];
					if ((csvpath != null || csvpath != "")  && (dbpath != null || dbpath != ""))
					{
						LoadCreateDatabase(csvpath, dbpath);
						if (dbLocated) {csvRelativePath = csvpath; dbRelativePath = dbpath;}
					}
					
				}
				catch(Exception e) {System.out.println("ERR IN INIT, reading provided address section");}
			}
			else 
			{
				//csvRelativePath = "data/items.csv";
				//dbRelativePath = "data/inventory.db";
				LoadCreateDatabase(csvRelativePath, dbRelativePath);
			}
		}
	}
	
	
	
	
	
	//---------DATABASE FUNCTIONS
	//ENTER QUERY
	public static ArrayList<CatalogItem> EnterQuery(String arg_query) {
		//Create database if it hasnt been done yet
		if (!dbLocated)
		{init(null, null);}
		
		//Create File Path
        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        //Try to Connect to Database
        try {
            // Create or open the SQLite database.
            try (Connection conn = DriverManager.getConnection(url)) {

            	//PRINT FILE PATH
                System.out.println("FILE CREATED (file name): " +dbFile.getFileName());
                System.out.println("FILE CREATED (file path): " +dbFile);

                //QUERY VARIABLE, SET AS ARG
                String query = arg_query;

                /*THE STATEMENT OBJECT
                The Statement Objects class can create objects that send SQL statements to the Database
                They can send statements or queries, Use a ResultSet to store the results of the query
                
                Whats important to pay attention to is ResultSet rs, since that holds the results of our query
                stmt.executeQuery(query) simply passes the string, query to the Statement object as a SQL query
                this returns results, which are stored in ResultSet rs.
                */
                
                try (
                		//Create Statement Object, this is used to 
                		Statement stmt = conn.createStatement();
                		ResultSet rs = stmt.executeQuery(query);
                		
            		) 
                {
                	//Update Last Result
                	LastResult = new ArrayList<CatalogItem>(); 
                	
                	//PRINT INPUT QUERY
                	System.out.println("\n[-- QUERY --]");
                	System.out.println(query);
                	
                	
                	//PRINT ALL ROWS/SAVE ALL ROWS
                    System.out.println("\n--- RESULT ---");
                    util.fieldHeader();
                    while (rs.next()) {
                    	printResult(rs); 
                    	
                        int ID = rs.getInt("ID");
                        double Price = rs.getDouble("Price");
                        String desc = rs.getString("Description");
                        String category = rs.getString("Category");
                        String tags = rs.getString("Tags");
                		
                		LastResult.add(new CatalogItem(ID,Price,desc,category,tags));
                		
                    	}
                    
                    return LastResult;
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ItemCatalog;
        }
	}



}
