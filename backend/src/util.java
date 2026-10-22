import java.util.ArrayList;

/*----------UTIL CLASS 

the util class is a static class containing a bunch of convinient methods you can acess universally


*/


public class util {
	
	//----------UNIVERSAL PRINT METHODS
	//PRINT ITEM FIELD HEADER
	public static void fieldHeader() {System.out.println("ID | PRICE | DESCRIPTION | CATEGORY | TAGS");}
	
	//PRINT ITEM FIELD HEADER
	//public static void cartHeader() {System.out.println("ID | PRICE | DESCRIPTION | CATEGORY | TAGS");}
	
	//PRINT ITEM LIST
	public static void printItemList(CatalogItem[] arg_items) {
		util.fieldHeader();
		for (CatalogItem i: arg_items) {i.printFields();}
	}
	
	public static void printCartList(ArrayList<CartItem> arg_items) {
		//util.fieldHeader();
		for (CartItem i: arg_items) {i.printFields();}
	}
	
	//PRINT CART LIST
	
	//PRINT FULL ITEM CATALOG DATABASE
	public static void printDatabase() {
		String query = ("SELECT * FROM Items;");
		ItemCatalog.EnterQuery(query);
	}

	//----------ENUM CLASSES
	//Category Tag enum
	public static enum CategoryTag 
	{BOOK,DEVICE,TOOL,SNACK,DRINK,STATIONERY,APPAREL,ERROR}
	
	//Item Field enum
	public static enum ItemField
	{ID,Price,Description,Category,Tags}
	
	//----------ENUM METHODS
	public static CategoryTag StringToCategory(String argInput) {
		String searchStr = argInput.trim().toLowerCase();
		
		switch (searchStr) 
		{
			case "book": {return CategoryTag.BOOK;} 
			case "device": {return CategoryTag.DEVICE;} 
			case "tool": {return CategoryTag.TOOL;}
			case "snack": {return CategoryTag.SNACK;}
			case "drink": {return CategoryTag.DRINK;}
			case "stationery": {return CategoryTag.STATIONERY;}
			case "apparel": {return CategoryTag.APPAREL;}
			default: {return CategoryTag.ERROR;}
		}	
	}
	
	public static ItemField StringToField(String argInput) {
		String searchStr = argInput.trim().toLowerCase();
		
		switch (searchStr) 
		{
			case "id": {return ItemField.ID;} 
			case "price": {return ItemField.Price;} 
			case "description": {return ItemField.Description;} 
			case "category": {return ItemField.Category;} 
			case "tags": {return ItemField.Tags;} 
			default: {return ItemField.ID;}
		}	
	}
	

}
