import java.util.ArrayList;




/*

STORE CATALOG

This is the main interface you should use to pull CartItems 

*/



public final class StoreCatalog {

	static ArrayList<CartItem> StoreCatalog = new ArrayList<CartItem>(); 
	
	static private Boolean printEnabled = false;
	
	public static void init() {
		ItemCatalog.init();
		SalesCatalog.init();
		
		for (CatalogItem i: ItemCatalog.ItemCatalog) {
			
			//Pull any sales this Item is included in
			ArrayList<Sale> build = SalesCatalog.getSalesByID(i.ID);	
			StoreCatalog.add(new CartItem(i,build));
		}
		
	}
	
	
	//QUERY
	public static ArrayList<CartItem> EnterQuery(String arg_query)
	{
		ArrayList<CatalogItem> tempcat = ItemCatalog.EnterQuery(arg_query);
		ArrayList<CartItem> returnlist = new ArrayList<CartItem>(); 
		
		
		for (CatalogItem i : tempcat)
		{
			if (getCartFromCat(i) != null)
			{
				returnlist.add(getCartFromCat(i));
			}
		}
		printItemSalesData(returnlist) ;
		return returnlist;
		
	}
	
	//FIND CART ITEM FROM CATALOG ITEM
	public static CartItem getCartFromCat(CatalogItem arg)
	{
		for (CartItem i : StoreCatalog)
		{
			if (i.item.ID == arg.ID) {return i;}
		}
		return null;
	}
	
	//PRINT
	public static void printAllItemSalesData() 
	{for (CartItem i: StoreCatalog) {i.printSales();}}
	
	public static void printItemSalesData(ArrayList<CartItem> args) 
	{for (CartItem i: args) {i.printSales();}}
	
}
