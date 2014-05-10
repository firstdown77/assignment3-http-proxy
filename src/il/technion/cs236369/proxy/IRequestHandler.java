/**
 * 
 */
package il.technion.cs236369.proxy;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * @author raphaelas
 *
 */
public interface IRequestHandler extends HttpRequestHandler {

	void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException;
	
}
