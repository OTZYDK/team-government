import java.util.ArrayList;

/*
STORE CART OBJECT

STORE CART FEATURES


ADD/REMOVE ITEM FROM CART

ADD CERTAIN AMOUNT OF ITEM TO CART

REMOVE ITEM FROM CART



*/


public class StoreCart {

	//List of Items in Cart
	private static ArrayList<CartItem> StoreCart = new ArrayList<CartItem>(); 
	private static ArrayList<CatalogItem> ItemList = new ArrayList<CatalogItem>();

	
	//Checkout
	/*Removes Items from stock, checks input amount, if valid, add to transaction list*/
	
	//------------SETTERS METHODS
	//--->Refresh CatalogItem list (based on CartItem list)
	private static void refreshItemList() 
	{
		//Clear List
		ItemList.clear();
		
		for (CartItem i : StoreCart)
		{
			ItemList.add(i.item);
		}
	}
	
	
	//--->Clear Cart
	public static void emptyCart() 
	{
		StoreCart.clear();
	}
	
	//--->Clear Cart and return receipt (only if receipt is valid)
	//Cash Payment
	public static void purchaseClearCart(double payment) 
	{
		//Save Receipt from Items
		Receipt temp = getTransaction(payment);
		
		if (temp != null && temp.getValid() && !StoreCart.isEmpty())
		{
			StoreCart.clear();
			StoreCatalog.addTransaction(temp);
		}
		
		
	}
	
	//Card
	public static void purchaseClearCart(String credit) 
	{
		//Save Receipt from Items
		Receipt temp = getTransaction(credit);
		
		if (temp != null && temp.getValid() && !StoreCart.isEmpty())
		{
			StoreCart.clear();
			StoreCatalog.addTransaction(temp);
		}
		
		
	}
	
	//--->Add to Cart
	//Accepts CartItem
	public static void addToCart(CartItem arg) 
	{
		Boolean inCart = false;
		Integer currentAmnt = arg.getUnitAmount();
		
		//Check our cart to increase purchase supply if its in our cart already
		for (CartItem i : StoreCart)
		{
			//If Item is in the cart
			if (i.item.ID == arg.item.ID)
			{
				inCart = true;
				i.incrementAmount(currentAmnt);
				break;
			}
		}
		
		//If it isnt in the cart
		//add it
		if (!inCart) 
		{
			CartItem set = arg;
			set.setAmount(1);
			StoreCart.add(set);
		}
		
		refreshItemList();
		//printCart();
	}
	
	public static void addToCart(CartItem arg, Integer amnt) 
	{
		Boolean inCart = false;
		
		
		//Check our cart to increase purchase supply if its in our cart already
		for (CartItem i : StoreCart)
		{
			
			//If Item is in the cart
			if (i.item.ID == arg.item.ID)
			{
				inCart = true;
				i.incrementAmount(amnt);
				break;
			}
		}
		
		//If it isnt in the cart
		//add it
		if (!inCart) 
		{
			CartItem temp = arg;
			temp.setAmount(amnt);
			StoreCart.add(temp);
		}
		
		refreshItemList();
		
		//printCart();
	}
	
	//Accepts ID
	public static void addToCart(int ID)
	{
		CartItem arg = StoreCatalog.getByID(ID);
		if (arg != null)
		{
			addToCart(arg);
		}		
	}
	
	public static void addToCart(int ID, int amnt)
	{
		CartItem arg = StoreCatalog.getByID(ID);
		if (arg != null) {addToCart(arg, amnt);}
	}
	
	
	//--->Remove From Cart
	//Accepts ID
	public static void removeFromCart(int ID) 
	{
		CartItem arg = getByID(ID);
		if (arg != null) {removeFromCart(arg);}
	}
	
	//Accepts CartItem
	public static void removeFromCart(CartItem arg) 
	{
		int scrub = getIndex(arg);
		
		if (scrub != -1)
		{
			StoreCart.remove(scrub);
		}
	}
	
	
	//--->Set Item Amount
	//Accepts ID and Amount
	public static void setItemAmount(int ID, int amnt) 
	{
		CartItem arg = getByID(ID);
		
		if (arg != null) {setItemAmount(arg, amnt);}
	}
	
	//Accepts Item and Amount
	public static void setItemAmount(CartItem arg, int amnt) 
	{
		int ind = getIndex(arg);
		
		if (ind != -1)
		{
			//Save Item
			CartItem mod = StoreCart.get(ind);
			
			//If we get rid of all items, remove it
			if (amnt <= 0)
			{
				StoreCart.remove(ind);
			}
			else
			{
				//Modify
				mod.setAmount(amnt);
				
				//Pass Index and updated item
				StoreCart.set(ind, mod);
			}
		}
	}
	

	//------------GETTERS
	//--->Get Full Cart List
	public static ArrayList<CartItem> getStoreCart() 
	{
		return StoreCart;
	}
	
	//--->Get Index of Cart Item in Cart
	public static Integer getIndex(CartItem arg)
	{
		refreshItemList();
		
		Integer cartIndex = -1;
		
		if (ItemList.contains(arg.item))
		{
			cartIndex = ItemList.indexOf(arg.item);
		}
		
		return cartIndex;
	}
	
	//--->Get Item in the Cart by ID
	public static CartItem getByID(int ID) 
	{
		for (CartItem i : StoreCart)
		{
			if (i.item.ID == ID)
			{
				return i;
			}
		}
		
		System.out.println("ERROR, does not exist");
		return null;
	}
	
	//--->Get Item in the Cart by Index (not recommended)
	public static CartItem getByIndex(int index) 
	{
		if (StoreCart.contains(StoreCart.get(index)))
		{
			return StoreCart.get(index);
		}
		else
		{
			return null;
		}
	}
	
	//--->Attempt Transaction
	//If the transaction is invalid, it returns null;
	public static Receipt getTransaction(double payment) 
	{
		Receipt temp = new Receipt(getStoreCart(), payment);
		
		if (temp.getValid()) 
		{
			return temp;
		}
		else
		{
			return null;
		}
	}
	
	public static Receipt getTransaction(String credit) 
	{
		Receipt temp = new Receipt(getStoreCart(), credit);
		
		if (temp.getValid()) 
		{
			return temp;
		}
		else
		{
			return null;
		}
	}
	
	//------------PRINTERS
	public static void printCart() 
	{
		System.out.println("PRINT CART");
		for (CartItem i : StoreCart)
		{
			i.printFieldLine();
		}
	}
}
