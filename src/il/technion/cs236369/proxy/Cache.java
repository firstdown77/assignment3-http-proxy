package il.technion.cs236369.proxy;

import il.technion.cs236369.proxy.data.IPageResponseDAO;
import il.technion.cs236369.proxy.data.PageResponseDAOFactory;
import java.sql.SQLException;

public class Cache implements ICache {

	IPageResponseDAO storage = null;
	
	public Cache()
	{
		try
		{
			storage = PageResponseDAOFactory.createPageResponseDAO();
		}
		catch (Exception e)
		{
			System.err.println("Unable to load storage system. Is the properties file placed correctly?");
		}
	}
	
	@Override
	public void buildTable() throws SQLException {
		storage.buildTable();
	}

	@Override
	public void destroyTable() throws SQLException {
		storage.destroyTable();
	}

	public PageResponse retrieveFromCache(String url)
	{
		return storage.getPageResponse(url);
	}
	
	public boolean saveInCache(PageResponse page)
	{
		return storage.savePageResponse(page);
	}
	
	public boolean deleteFromCache(String url)
	{
		return storage.deletePageResponse(url);
	}
	
}
