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



public final class SalesCatalog {

	
	//---------VARIABLES
	//File Paths
	private static String csvRelativePath = "data/sales.csv";
	private static String dbRelativePath = "data/itemsales.db";
	
	//Item Catalog and Last Result
	final static ArrayList<Sale> SalesCatalog = new ArrayList<Sale>(); 
	static ArrayList<Sale> LastResult = new ArrayList<Sale>(); 
	
	//Database Located - indicates initialization
	static private Boolean dbLocated = false;
	
	static private Boolean printEnabled = false;

	//---------PRINTERS
	//PRINT LAST RESULT SAVED
	static void printLastResult() {
		util.fieldHeader();
		for (Sale i: LastResult) {i.printSaleData();}
	}
	
	//PRIVATE, PRINTS FROM DATASET, FOR PRINTING QUERY RESULTS
	private static void printResult(ResultSet rs) 
	{
		try {
        	if (printEnabled) {        	
            	String ID = rs.getString("ID");
            	String desc = rs.getString("Description");
            	String _query = rs.getString("Query");
            	String tx = rs.getString("TaxExempt");
            	String dscntrte = rs.getString("DiscountRate");
            	String dscntdlr = rs.getString("DiscountDollarAmount");
            	
            	Sale temp = new Sale(ID,desc,_query,tx,dscntrte,dscntdlr);
            	if (printEnabled) {temp.printSaleData();}
        	
        	}
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

                System.out.println("DISCOUNT FILE CREATED (file name): " +dbFile.getFileName());
                System.out.println("DISCOUNT FILE CREATED (file path): " +dbFile);

                //CREATE THE TABLE (STATEMENT)
                ////ID, DESCRIPTION, QUERY, Tax Exempt, Discount Rate, Discount Dollar Amount
                try (Statement stmt = conn.createStatement()) 
                {
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS Items (
                            ID INTEGER PRIMARY KEY,
                            Description TEXT,
                            Query TEXT,
                            TaxExempt TEXT,
                            DiscountRate REAL,
                            DiscountDollarAmount REAL
                        )
                        """);
                }
                

                //Import CSV rows into SQLite.
                String insertSQL = """
                    INSERT OR REPLACE INTO Items
                    (ID, Description, Query, TaxExempt, DiscountRate, DiscountDollarAmount)
                    VALUES (?, ?, ?, ?, ?, ?)
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
                        if (fields.length != 6) {
                            System.out.println("Skipping invalid row: " + line);
                            continue;
                        }

                        ps.setInt(1, Integer.parseInt(fields[0].trim()));
                        ps.setString(2, fields[1].trim());
                        ps.setString(3, fields[2].trim());
                        ps.setString(4, fields[3].trim());
                        ps.setDouble(5, Double.parseDouble(fields[4].trim()));
                        ps.setDouble(6, Double.parseDouble(fields[5].trim()));

                        ps.executeUpdate();
                    }
                    
                    
                }
                
                //PRINT ALL ROWS TO ITEM CATALOG LIST
                String query = """
                    SELECT ID, Description, Query, TaxExempt, DiscountRate, DiscountDollarAmount
                    FROM Items
                    ORDER BY ID
                    """;

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                	if (printEnabled) {System.out.println("\n--- ALL SALES DATABASE ROWS ---");}

                    while (rs.next()) {
                    	printResult(rs); 
                    	
                    	String ID = rs.getString("ID");
                    	String desc = rs.getString("Description");
                    	String _query = rs.getString("Query");
                    	String tx = rs.getString("TaxExempt");
                    	String dscntrte = rs.getString("DiscountRate");
                    	String dscntdlr = rs.getString("DiscountDollarAmount");
                		
                        SalesCatalog.add(new Sale(ID,desc,_query,tx,dscntrte,dscntdlr));
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
				csvRelativePath = "data/sales.csv";
				dbRelativePath = "data/itemsales.db";
				LoadCreateDatabase(csvRelativePath, dbRelativePath);
			}
		}
	}
	
	
	//ENTER QUERY
	public static ArrayList<Sale> EnterQuery(String arg_query) {
		//Create database if it hasnt been done yet
		if (!dbLocated)
		{init();}
		
		//Create File Path
        Path dbFile = Path.of(dbRelativePath).toAbsolutePath();
        String url = "jdbc:sqlite:" + dbFile;

        //Try to Connect to Database
        try {
            // Create or open the SQLite database.
            try (Connection conn = DriverManager.getConnection(url)) {

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
                	LastResult = new ArrayList<Sale>(); 
                	
                	if (printEnabled) 
                	{
                    	//PRINT INPUT QUERY
                    	System.out.println("\n[-- QUERY --]");
                    	System.out.println(query);
                    	
                    	
                    	//PRINT ALL ROWS/SAVE ALL ROWS
                        System.out.println("\n--- RESULT ---");
                        util.fieldHeader();
                	}

                    while (rs.next()) {
                    	printResult(rs); 
                    	
                    	String ID = rs.getString("ID");
                    	String desc = rs.getString("Description");
                    	String _query = rs.getString("Query");
                    	String tx = rs.getString("TaxExempt");
                    	String dscntrte = rs.getString("DiscountRate");
                    	String dscntdlr = rs.getString("DiscountDollarAmount");
                		
                        LastResult.add(new Sale(ID,desc,_query,tx,dscntrte,dscntdlr));
                    }
                    printDisabled();
                    return LastResult;
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return SalesCatalog;
        }
	}


	
	
	//---------SETTERS - TOGGLES
	public static void printEnabled() {printEnabled = true;}
	public static void printDisabled() {printEnabled = false;}
	
	
	//---------GETTERS
	public static ArrayList<Sale> getSalesByID(int arg)
	{
		ArrayList<Sale> buildSales = new ArrayList<Sale>(); 
		
		//For Each Sale object in our catalog
		for (Sale i: SalesCatalog)
		{
			//If the ID is in the sales subjects
			if (i.getSubjects().contains(arg))
			{
				buildSales.add(i);
			}
		}
		
		return buildSales;
	}

	
}
