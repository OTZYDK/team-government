
import java.util.ArrayList;

/*
ITEM CLASS:
ID -> Int
Price -> Double
Description -> String
Category -> Category Enumerator
ItemTags -> String List of "Tags" or descriptors
*/

public class CatalogItem {
	enum CategoryTag 
	{BOOK,DEVICE,TOOL,ERROR}
		
	public final int ID; //FIELD 0
	public final double Price; //FIELD 1
	public final String Description; //FIELD 2
	
	public final CategoryTag Category; //FIELD 3
	public final ArrayList<String> ItemTags = new ArrayList<String>(); //FIELD 4
	
	//CONVERT STRING INPUT TO CATEGORY ENUM
	private CategoryTag StringToTag(String argInput) {
		String searchStr = argInput.trim().toLowerCase();
		
		switch (searchStr) 
		{
			case "book": {return CategoryTag.BOOK;} 
			case "device": {return CategoryTag.DEVICE;} 
			case "tool": {return CategoryTag.TOOL;}
			default: {return CategoryTag.ERROR;}
		}	
	}
	
	//FILL TAG LIST
	private void fillTagList(String[] arg_Tags) {
		int inputSize = arg_Tags.length;
		
		//For Every item in the input list, add it to TagList
		for (int i = 0; i < (inputSize); i++)
		{ItemTags.add(arg_Tags[i]);}
	}
	
	//CLASS CONSTRUCTOR - PROVIDE INPUTS
	public CatalogItem(int arg_ID, double arg_Price, String arg_Description, String arg_Category, String[] arg_Tags) {

		ID = arg_ID;
		Price = arg_Price;
		Description = arg_Description;
		
		Category = StringToTag(arg_Category);
		fillTagList(arg_Tags);
	}
	
	//CLASS CONSTRUCTOR, LOADS FROM SAVE
	public CatalogItem(String arg_fileLine) {
		
		String[] inputArray = arg_fileLine.split(","); 
		
		
		//SET ID - FIELD 0
		this.ID = Integer.parseInt(inputArray[0]);
		
		//SET PRICE - FIELD 1
		this.Price = Double.parseDouble(inputArray[1]);
		
		//SET DESCRIPTION - FIELD 2
		this.Description = inputArray[2];
		
		//SET CATEGORY
		this.Category = StringToTag(inputArray[3]);
		
		//SET TAGS
        String[] loadTags = inputArray[4].split("-"); 
        fillTagList(loadTags);
	}
	
	//GETTERS
	public String getCategory() {return Category.toString();}
	
	//CONVERSIONS
	public String TagstoString() {
		String temp = "";
		for (int i = 0; i < (ItemTags.size()); i++)
		{temp += (ItemTags.get(i) + "-");}
		
		return temp;
	}
	
	public String DataToString() {
		String temp = "";
		
		//SET ID - FIELD 0
		temp += ID + ",";
		
		//SET PRICE - FIELD 1
		temp += Price + ",";
		
		//SET DESCRIPTION - FIELD 2
		temp += Description + ",";
		
		//SET CATEGORY - FIELD 3
		temp += Category.toString() + ",";
		
		//SET TAGS - FIELD 4
		temp += TagstoString();
		
		return temp;
	}
	
	//PRINTERS
	public void printTags() {
		for (int i = 0; i < (ItemTags.size()); i++)
		{System.out.println(ItemTags.get(i));}
	}
	
	public void printFields() {
		System.out.println("\nALL DATA FIELDS\n");
		System.out.println("ID: " + ID);
		System.out.println("PRICE: " + Price);
		System.out.println("DESCRIPTION: " + Description);
		System.out.println("CATEGORY: " + Category);
		System.out.println("TAGS: ");
		printTags();	
	}
	


}
