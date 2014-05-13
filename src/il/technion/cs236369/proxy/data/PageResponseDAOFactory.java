package il.technion.cs236369.proxy.data;


public class PageResponseDAOFactory {
	
	public static IPageResponseDAO createPageResponseDAO()
	{
		return new PageResponseDAO();
	}

}
