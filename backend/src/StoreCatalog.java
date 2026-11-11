import java.util.ArrayList;




/*

STORE CATALOG

This is the main interface you should use to pull CartItems 


IMPLEMENT IMPORTANT FEATURES FROM CSVHANDLER HERE

*/



public final class StoreCatalog {

	//---------VARIABLES
	private static ArrayList<CartItem> StoreCatalog = new ArrayList<CartItem>(); 
	private static ArrayList<Receipt> PurchaseLedger = new ArrayList<Receipt>(); 
	
	static private Boolean printEnabled = false;
	
	
	//---------INITIALZIE STORE
	public static void init() {
		ItemCatalog.init();
		SalesCatalog.init();
		
		//Build StoreCatalog list
		for (CatalogItem i: ItemCatalog.ItemCatalog) {
			
			//Pull any sales this Item is included in
			ArrayList<Sale> build = SalesCatalog.getSalesByID(i.ID);	
			StoreCatalog.add(new CartItem(i,build));
		}
		
	}
	
	
	//---------QUERY
	//--->Search StoreCatalog for Item
	public static ArrayList<CartItem> SearchStore(String arg_query)
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
	
	//--->Search SaleCatalog for Sale
	public static ArrayList<Sale> SearchSales(String arg_query) 
	{
		return SalesCatalog.EnterQuery(arg_query);
	}
	
	//--->Search ItemCatalog for Item
	public static ArrayList<CatalogItem> SearchItems(String arg_query) 
	{
		return ItemCatalog.EnterQuery(arg_query);
	}
	
	
	//---------FUNCTIONS
	//--->FIND CART ITEM FROM CATALOG ITEM
	public static CartItem getCartFromCat(CatalogItem arg)
	{		
		for (CartItem i : StoreCatalog)
		{
			if (i.item.ID == arg.ID) {return i;}
		}
		return null;
	}

	
	//---------SETTERS
	//--->Accepts Store Cart Item
	public static void addTransaction(Receipt arg) 
	{
		if (arg != null && arg.getValid()) {PurchaseLedger.add(arg);} else {System.out.println("PURCHASE INVALID");}
	}
	
	//--->Purchases Current Cart, will not puchase if it is invalid
	public static String purchaseCart(double payment) 
	{
		//Save Message from Purchase
		String returnval = StoreCart.getTransaction(payment).getPaymentMessage();
		
		//Call Purchase
		StoreCart.purchaseClearCart(payment);
		
		return returnval;
	}
	
	public static String purchaseCart(String credit) 
	{
		//Save Message from Purchase
		String returnval = StoreCart.getTransaction(credit).getPaymentMessage();
		
		//Call Purchase
		StoreCart.purchaseClearCart(credit);
		
		return returnval;
	}
	
	
	//---------GETTERS
	//---> Get Store Catalog
	public static ArrayList<CartItem> getStore() 
	{
		return StoreCatalog;
	}
	
	//---> Get Purchase Ledger
	public static ArrayList<Receipt> getPurchaseLedger()
	{
		return PurchaseLedger;
	}
	
	//---> Get Store Item by ID
	public static CartItem getByID(int ID) 
	{	
		for (CartItem i : StoreCatalog)
		{
			if (i.item.ID == ID)
			{
				return i;
			}
		}
		
		System.out.println("ERROR, does not exist");
		return null;
	}
	
	
	
	
	//---------PRINTERS
	//PRINT
	public static void printAllItemSalesData() 
	{for (CartItem i: StoreCatalog) {i.printSales();}}
	
	public static void printItemSalesData(ArrayList<CartItem> args) 
	{for (CartItem i: args) {i.printSales();}}
	
	public static void printPurchaseLedger() 
	{
		util.printReceiptCondensedHead();
		
		for (Receipt i : PurchaseLedger)
		{
			//i.printReceipt();
			i.printReceiptCondensed();
		}
	}
	
}
