package il.technion.cs236369.proxy;

import java.util.HashMap;

/**
 * Cache database requirements.
 * 
 * @author raphaelas
 *
 */
public interface IDatabaseRequirements {
	
	/**
	 * Inserts a HashMap entry.
	 * The HashMap contains the following according to the assignment instructions:
	 * 1. URL
	 * 2. headers
	 * 3. response
	 * 4. lastmodified - may be null
	 * 
	 * @param parsedRequest The current parsed request.
	 * @return true if successful, false if failed
	 */
	boolean insertRequest(HashMap<String, String> parsedRequest);
	
	/**
	 * Gets a saved cache entry, in the form of a HashMap of strings.
	 * 
	 * @return a HashMap containing a cache entry
	 */
	HashMap<String, String> getRequestFromCache();

}
