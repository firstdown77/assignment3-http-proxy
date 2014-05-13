package il.technion.cs236369.proxy.data;

import il.technion.cs236369.proxy.PageResponse;

import java.util.Collection;

/**
 * Interface for the storage system
 * 
 * @author david
 *
 */
public interface IPageResponseDAO 
{
	
	/**
	 * opens the connection to the underlying storage system
	 */
	public void open();
	
	/**
	 * closes the connection to the underlying storage system
	 */
	public void close();
	
    /**
     * Returns the page response that matches the given URL
     * 
     * @param url A unique URL
     * @return a PageResponse containing the page response information
     */
	PageResponse getPageResponse(String url);
	
	/**
	 * Deletes the page response identified by the url 
	 * 
	 * @param url A unique URL
	 * @return true if there was a page response and was successfully deleted, false otherwise
	 */
	boolean deletePageResponse(String url);

    /**
     * Saves the pague response
     * Returns true if save successful.  Returns false if save failed.
     * Duplicate URLs will be overwritten
     *
     * @param page the pageResponse to save
     * @return True if save successful, false if failed.
     */
	boolean savePageResponse(PageResponse page);
	
	void destroyTable();
	void buildTable();
}
