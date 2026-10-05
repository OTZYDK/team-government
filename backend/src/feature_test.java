import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files

/*
You can Ignore this file if you like. 
This is a data file I made to test new backend code,
that way I dont have to disturb the new code in Main

I'll try to keep everything here very commented
in case anyone needs to refer the features tested here
*/


/*
FEATURE: Add Items to Catalog
UPDATE: Category List - snacks, drinks, stationery, exam supplies, art supplies, apparel, course supplies, and BOOKS
FEATURE: Search Item by Category
FEATURE: Search Item by ID
FEATURE: Add Item to Shopping Cart

Each Cart item should have a variable indicating the purchase amount (how many of each item in cart)
Each Cart item should have a Tax Exempt boolean (which factors in price calculation) 


CART CLASS OBJECT SHOULD BE AN OBJECT THAT HOLDS MULTIPLE CATALOG ITEMS
Since we can access data like an items price, its ID, and name,
the class needs to provide easy ways to show: 

I think the best way we could do this is to create a print checkout function
that returns all items in the shopping cart with price modifiers applied

I also think it would be most efficient to apply pricemodifiers on checkout
as opposed to story discounts in each item instance

function adjustedprice(bool taxempt = false, double discountpercent )
{
double adjustedprice = catalogitem.price;

//apply discount
adjustedprice -= (catalogitem.price/100) *  discountpercent

if (!tax exempt)
{


adjustedprice += catalogitem.price * .007;

}


}



then the shopping cart class would be a class that mostly provides functions to filter and orgnaize these objects
this class should handle applying discounts


*/


public class feature_test {
	
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
		
		if (LoadFile("LibraryItems.csv") != null)
		{
			//Create Temporary Array-List to hold loaded data
			ArrayList<String> LoadItems = LoadFile("LibraryItems.csv");
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
		

		/*
		 if (LoadFile("LibraryItems.csv") != null) 
		 {
			List<String[]> LoadItems = LoadFile("LibraryItems.csv");
			for (int i = 0; i < (LoadItems.size()); i++)
			{System.out.println(LoadItems.get(i));}
		 }
		*/
		
		//SAVE TO CSV
		
		/*
		
		//ALL ITEMS LIST
		List<CatalogItem> ItemLibrary = new ArrayList<CatalogItem>(); 
		ItemLibrary.add(Item_A);
		ItemLibrary.add(Item_B);
		ItemLibrary.add(Item_C);
		
		
		
		//SAVE ALL OBJECTS TO ARRAY
		String[] SaveItems = new String[]{Item_A.DataToString(),Item_B.DataToString(),Item_C.DataToString()};
		//ArrayList<String> SaveItems = new ArrayList<String>(Arrays.asList(Item_A.DataToString(),Item_B.DataToString(),Item_C.DataToString()));
		
		//SAVE STRING LIST OF OBJECTS
		SaveFile("test_save.csv", SaveItems);
		
		CatalogItem Item_X;
		CatalogItem Item_Y;
		CatalogItem Item_Z;
		
		List<String[]> LoadItems;
		try {
			LoadItems = LoadFile("test_save.csv");
			for (int i = 0; i < (LoadItems.size()); i++)
			{
				//System.out.println(LoadItems.get(i));
				
			}
			
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		

		
	}



}

