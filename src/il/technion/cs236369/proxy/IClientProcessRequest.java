/**
 * 
 */
package il.technion.cs236369.proxy;

import java.util.HashMap;

/**
 * Interface for listening to HTTP requests on the client's web browser.
 * 
 * @author raphaelas
 *
 */
public interface IClientProcessRequest {
	
	/**
	 * Note: if you think the server side should parse the HTML, you can send
	 * all of this in a single String instead.
	 * The HashMap contains the following according to the assignment instructions:
	 * 1. URL
	 * 2. headers
	 * 3. response
	 * 4. lastmodified - may be null
	 * 
	 * @return a HashMap of parsed HTML data.
	 */
	HashMap<String, String> sendRequestToServerSocket();

}
