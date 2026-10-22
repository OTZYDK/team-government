/*----------SEARCH CLASS

The Search class is a static class that contains methods
for easily generating SQL queries.

Methods typically accept String list as argument, and return String (an SQL query) as the result

Pass the results of these directly into ItemCatalog.EnterQuery() to get results


*/


public final class Search {
	public static String getAll(){
		String result = "SELECT * FROM Items;";
		return result;
	}
	
	//----------SEARCH BY CATEGORY
	//Returns Records with matching Category
	public static String byCategory(String...args) {
		//DETERMINE CATEGORY
		String cat = args[0].trim().toLowerCase();
		
		//final args
		String order = null;
		
		//Determine search Category
		String category = "";
		switch (cat) 
		{
			case "book": {category = "\'BOOK\'"; break;} 
			case "device": {category = "\'DEVICE\'"; break;} 
			case "tool": {category = "\'TOOL\'"; break;}
			case "snack": {category = "\'SNACK\'"; break;}
			case "drink": {category = "\'DRINK\'"; break;}
			case "stationery": {category = "\'STATIONERY\'"; break;}
			case "apparel": {category = "\'APPAREL\'"; break;}
			default: {category = null; break;}
		}
		
    	//ITERATE OPTIONAL ARGUMENTS
		for (int i = 0; i < args.length; i++)
		{
			//SKIP 0
        	//ARG 1 - ORDER BY
        	if (i == 1 && args[i] != "")
        	{}
        	
        	//ARG 2 - ASCENDING OR DESCENDING
        	if (i == 2 && args[i] != "")
        	{}
		}
		
		//Default Result, Populate if the category is proper
		String result = "SELECT * FROM Items;";
		
		//If we have a valid category
		if (category != null) {result = "SELECT * FROM Items WHERE Category = " + category + ";"; }
		
		
		//System.out.println(result);
		
		return result;
	}
	
	//----------SEARCH BY ID
	//Returns Records with matching ID
	public static String byID(String...args) {
		
		//ID is first argument necessary
		String ID = args[0].trim().toLowerCase();
		
		//Check for valid number
		if (Integer.parseInt(ID) > 0) {} else {ID = "1";}
		
		String result = "SELECT * FROM Items WHERE ID = " + ID + ";";
		
	   	//ITERATE OPTIONAL ARGUMENTS
		for (int i = 0; i < args.length; i++)
		{
			//SKIP 0
        	//ARG 1 
        	if (i == 1 && args[i] != "") {}
        	
        	//ARG 2 
        	if (i == 2 && args[i] != "") {}
		}
		
		return result;
	}

	//----------SEARCH BY KEYWORD
	//Returns Records whos string fields (category, description, tags) match the input
	public static String byKeyword(String arg) {
		//Split argument into keywords
		String[] keywords = arg.toLowerCase().split(" ");
		
		String cat = "";
		String tag = "";
		String desc = "";
		
	   	//Search Keywords in all fields
		for (int i = 0; i < keywords.length; i++)
		{
			String key = "\'%" + keywords[i] + "%\'";
			//FIRST ITEM
			if (i == 0) 
			{
				tag = "LOWER(Tags) LIKE " + key;
				cat = "OR LOWER(Category) LIKE " + key;
				desc = " AND LOWER(Description) LIKE " + key ;
			}
			else if (keywords[i] != "" || keywords[i] != " ") {
				tag += " AND LOWER(Tags) LIKE " + key;
				cat += " OR LOWER(Category) LIKE " + key;
				desc += " AND LOWER(Description) LIKE " + key ;
			}
		}
		
		//Compile Statements
		String result = "SELECT * FROM Items WHERE " + tag + cat + desc;
		
		//Add line terminator and return
		result += ";";
		//System.out.println(result);
		
		return result;
	}
	
	//----------SEARCH BY PRICE

}
