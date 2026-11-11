import java.util.ArrayList;
import java.util.List;

/*

Sale Attributes:

ID, Description, Query, 
Discount Object -> Tax Exempt, Discount Rate, Discount Dollar Amount
Subjects [List of Catalog Item IDs]
Members [List of CartItems built from search]

*/


public class Sale{
	
	//Sale ID
	private int ID;
	
	//Sale Description
	private String Desc;
	
	//Sale Query (discount is applied to results)
	private String Query;

	//Discount Object to apply to members
	private Discount SaleDiscount;
	
	//List of Subject IDs
	private ArrayList<Integer> SubjectIDs = new ArrayList<Integer>();
	
	//Indicates that a sale object is improper and should be ignored for use
	private Boolean Valid = true;
	
	static private Boolean printEnabled = false;
	
	//----------CONSTRUCTORS
	//ARGUMENTS
	//ID, DESCRIPTION, QUERY, Tax Exempt, Discount Rate, Discount Dollar Amount
	public Sale(String[] args) 
	{

		if (args.length == 6)
		{
			//Set properties with the first 3 arguments
			setProperties(args[0],args[1],args[2]);
			
			//Set Discount with the second 3 arguments
			setDiscount(args[3],args[4],args[5]);
		} 
		
		if (Valid) 
		{
			//Create list holding results of condition
			if (printEnabled) {ItemCatalog.printEnabled();} else {ItemCatalog.printDisabled();}
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			//Create List of Subject IDs and Build Cart Items
			for (CatalogItem i: pull_subjects) 
			{
				//Add Subject ID
				SubjectIDs.add(i.ID);
				
			}
			
			printDisabled();
			
		}
		else
		{
			//Fill with one junk member
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			SubjectIDs.add(-1);
		}
		
	}
	
	public Sale(String ID, String Description, String Query, String TaxExempt, String DiscountRate, String DiscountDollar) 
	{
		//Set properties with the first 3 arguments
		setProperties(ID,Description,Query);
		
		//Set Discount with the second 3 arguments
		setDiscount(TaxExempt,DiscountRate,DiscountDollar);
		
		if (Valid) 
		{
			//Create list holding results of condition
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			//Create List of Subject IDs and Build Cart Items
			for (CatalogItem i: pull_subjects) 
			{
				//Add Subject ID
				SubjectIDs.add(i.ID);
			}
		}
		else
		{
			//Fill with one junk member
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			SubjectIDs = new ArrayList<Integer>();
			SubjectIDs.add(-1);
		}
		
	}
	
	public Sale(ArrayList<String> list_args) 
	{
		//Convert Arraylist into Array
		String[] args = new String[list_args.size()];
		args = list_args.toArray(args);
		
		//If Length is Valid
		if (args.length == 6)
		{
			//Set properties with the first 3 arguments
			setProperties(args[0],args[1],args[2]);
			
			//Set Discount with the second 3 arguments
			setDiscount(args[3],args[4],args[5]);
		}
		else {Valid = false;}
		
		//If Inputs are Valid
		if (Valid) 
		{
			//Create list holding results of condition
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			//Create List of Subject IDs and Build Cart Items
			for (CatalogItem i: pull_subjects) 
			{
				//Add Subject ID
				SubjectIDs.add(i.ID);
			}
		}
		
		//If Invalid is checked
		else
		{
			//Fill with one junk member
			ArrayList<CatalogItem> pull_subjects = ItemCatalog.EnterQuery(Query);
			
			SubjectIDs = new ArrayList<Integer>();
			SubjectIDs.add(-1);
		}
		
	}
	
	//----------SETTERS (PRIVATE)
	private void setProperties(String id, String desc, String query) 
	{
		//Parse ID from String input
		try {ID = Integer.parseInt(id.trim());} catch (NumberFormatException e) {ID = -1; Valid = false;}
		
		//if (id > 0) {ID = id;}
		if (desc != null && desc != "") {Desc = desc;} else {Desc = "Invalid Sale Object"; Valid = false;}
		
		//Check for search validity, set if valid
		if (util.queryValid(query)) {Query = query;} else {Query = util.defaultQuery(); Valid = false;}
	}
	
	private void setDiscount(String tx, String dscntrte, String dscntdlr) 
	{
		//create discount from inputs
		Discount temp = new Discount(tx, dscntrte, dscntdlr);
		
		//if its valid, set the properties
		if (temp.isValid()) {SaleDiscount = new Discount(tx, dscntrte, dscntdlr);}
		
		//otherwise mark invalid and set generic properties
		else {SaleDiscount = new Discount(); Valid = false;}
	}
	
	//----------GETTERS
	//ID
	public int getID() {return ID;};
	
	//Description
	public String getDesc() {return Desc;}
	
	//Query
	public String getQuery() {return Query;}

	//Discount Object
	public Discount getDiscount() {return SaleDiscount;}
	
	//List of Subject IDs
	public ArrayList<Integer> getSubjects(){return SubjectIDs;}
	
	//Summary of Sale effects
	public ArrayList<String> getSummaryList() 
	{
		ArrayList<String> Lines = new ArrayList<String>();
		
		Lines.add(Integer.toString(ID));
		Lines.add(Desc);
		
		if (Valid)
		{
			if (SaleDiscount.DiscountRate > 0) {Lines.add(SaleDiscount.DiscountRate + "%");} else {Lines.add("");}
			if (SaleDiscount.DiscountDollar > 0) {Lines.add(Double.toString(util.priceFormat(SaleDiscount.DiscountDollar)) + "$");} else {Lines.add("");}
		}
		
		return Lines;
		
	}
	
	//Validity
	public Boolean getValid() {return Valid;}
	
	//----------PRINTERS
	
	public void printSaleData()
	{		
		System.out.printf("\nSALE: %d | %s", ID, Desc);
		System.out.println("\nQuery: " + Query);
		System.out.println("Tax Exempt: " + SaleDiscount.TaxExempt );
		System.out.println("Discount Rate: " + SaleDiscount.DiscountRate );
		System.out.println("Discount Dollar Amount: " + SaleDiscount.DiscountDollar );
		//util.printCartList(Members);
		
	}
	
	//---------SETTERS - TOGGLES
	public static void printEnabled() {printEnabled = true;}
	public static void printDisabled() {printEnabled = false;}
}
