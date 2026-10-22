
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


/* BACKEND TEST
This is a class I made to store and call miscellaneous tests
This way, I can avoid modifying my main over and over



*/


/*CURRENT ISSUES
 * 
 * it seems like all my current goals hinge on first being able to load and search items
 * so right now, I am going to focus on building a rudimentary library 
 * and making code so the library can easily be browsed
 * 
 * 
 * 
 * 
 * 
 * 
 * */

public class BackendTest {
	
	//LOAD FILE
	private static ArrayList<String> LoadFile(String file)
	{
		//Create File Object from source file
	    File sourceFile = new File(file);
	    
	    //Create Empty Array list to hold data
	    ArrayList<String> returnData = new ArrayList<String>(); 
	    
	    //Try-Catch for Scanner
	    //If Scanner is successful
	    try (Scanner readFile = new Scanner(sourceFile)) 
	    {
	      while (readFile.hasNextLine())
	      {
	    	  String data = readFile.nextLine();
	    	  //System.out.println(data);
	    	  returnData.add(data);
	      }
	      readFile.close();
	      return returnData;
	      
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	      return null;
	    }
	}

	public BackendTest() {}
	
	public void current() {testItemLibary();}
	
	public void createCatalogItemTest() {
		/*
		ITEM A is constructed with the normal class constructor, where the fields are provided directly to create an object
		ITEM B is constructed with a String holding all data fields, this constructor splits the string and uses that data as its fields
		ITEM C is constructed by recieving all the fields from ITEM B as a string, then loading them all as its values
		
		Item B and C are tests designed to show functionality for loading and saving data
		the DataToString function is designed to save content as a line in a text file
		and the alternate constructor within the class is then meant to load all fields from that line (as a string)
		
		I have set the fields to be Final so they cannot be changed after the object is created. This may need to change however
		*/
		
		CatalogItem Item_A = new CatalogItem(50, 200.0, "TI 84 Calculator", "device", new String[]{ "calculator", "mathematics", "calculus", "algrebra", "Texas Instruments"});
		CatalogItem Item_B = new CatalogItem("50,200.0,TI 84 Calculator,device,calculator-mathematics-calculus-algrebra-Texas Instruments-");
		CatalogItem Item_C = new CatalogItem(Item_B.DataToString());
		
		System.out.println("\nITEM A - DATA TO FIELD CONSTRUCTOR");
		Item_A.printFields();
		
		System.out.println("\nITEM B - STRING TO FIELD CONSTRUCTOR");
		Item_B.printFields();
		
		System.out.println("\nITEM C - EXPORT ITEM DATA AND LOAD INTO NEW OBJECT");
		Item_C.printFields();
	}
	
	public void loadCatalogItemsToList() {
		//ALL ITEMS LIST
		ArrayList<CatalogItem> ItemLibrary = new ArrayList<CatalogItem>(); 
		
		//LOAD FILE AND PRINT VALUES
		if (LoadFile("items.csv") != null)
		{
			//Create Temporary Array-List to hold loaded data
			ArrayList<String> LoadItems = LoadFile("items.csv");
			System.out.println("\nFILE LOAD SUCCESSFUL");
			
			for (int i = 1; i < (LoadItems.size()); i++)
			{
				//Create a catalog item from each line and add it to the item library
				CatalogItem temp_item = new CatalogItem(LoadItems.get(i));
				ItemLibrary.add(temp_item);	
			}
			
			System.out.println("\nPRINTING FULL LIBRARY WITH LOADED DATA");
			for (int i = 0; i < (ItemLibrary.size()); i++)
			{
				ItemLibrary.get(i).printFields();
			}
		}	
	}
	
	public void createCartItemTest() {
		
		
		CatalogItem Item_A = new CatalogItem(50, 200.0, "TI 84 Calculator", "device", new String[]{ "calculator", "mathematics", "calculus", "algrebra", "Texas Instruments"});
		
		System.out.println("\nCART ITEM OPTIONAL PARAMTERS TEST");
		CartItem test = new CartItem(Item_A, 50 + "", false + "", 6 + "");
		test.printFields();

	}
	
	public void testItemLibary() {
		//ItemLibrary test = new ItemLibrary("items.csv");
		
		System.out.println("INIT WITH ARGUMENTS");
		ItemCatalog.init();
		
	    String query = ("""
	    	    SELECT *
	    	    FROM Items
	    	    WHERE CATEGORY = 'SNACK';
	    	    """);
	    
	    //ItemCatalog.EnterQuery(query);
	    System.out.println("SEARCH BY ID TEST");
	    ItemCatalog.EnterQuery(Search.byID("-2"));
	    
	    System.out.println("SEARCH BY CATEGORY TEST");
	    ItemCatalog.EnterQuery(Search.byCategory("BOOK"));
	    
	    System.out.println("SEARCH BY KEYWORD TEST");
	    //Search.byKeyword("Math Book");
	    ItemCatalog.EnterQuery(Search.byKeyword("calculator"));
	    
	
	    util.printDatabase();
    	//System.out.println("PRINTING RESULT SET");
    	//ItemCatalog.printLastResult();
	    
	    //Policy joe = new Policy(Search.byCategory("BOOK"), "60", "true");
	    

	}
	
	
	
}
