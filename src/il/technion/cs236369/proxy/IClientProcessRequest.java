/**
 * 
 */
package il.technion.cs236369.proxy;

/**
 * Interface for listening to HTTP requests on the client's web browser.
 * 
 * @author raphaelas
 *
 */
public interface IClientProcessRequest {
	
	/**
	 * This method should send a HashMap<String, String> to the
	 * receiveRequestFromClientSocket() method in the HttpProxy class.
	 * 
	 * This method should take advantage of the createClientSocket(String host)
	 * method in the HttpProxy class that is a parameter of the method.
	 * 
	 * The HashMap contains the following according to the assignment instructions:
	 * 1. URL
	 * 2. headers
	 * 3. response
	 * 4. lastmodified - may be null
	 * 
	 * Note: if you think the server side should parse the HTML, you can send
	 * all of this in a single String instead.
	 */
	void sendRequestToServerSocket(HttpProxy serverSocketClass);
}
