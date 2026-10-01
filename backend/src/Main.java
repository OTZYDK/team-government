import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files
/*
ITEM CLASS REQUIREMENTS:
ID -> Int
Price -> Double
Description -> String
Category(Traits) -> Separate Class Object

DATABASE REQUIREMENTS:
Save as csv file
holds all fields and items
is hardcoded / prewritten
*/


public class Main {
	
	//LOAD FILE - RETURNS AN ARRAY-LIST WITH ALL DATA
	public static ArrayList<String> LoadFile(String file)
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

	public static void main(String[] args) {
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
		
		//ALL ITEMS LIST
		List<CatalogItem> ItemLibrary = new ArrayList<CatalogItem>(); 
		
		if (LoadFile("backend\\src\\data\\items.csv") != null)
		{
			//Create Temporary Array-List to hold loaded data
			ArrayList<String> LoadItems = LoadFile("backend\\src\\data\\items.csv");
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



}
