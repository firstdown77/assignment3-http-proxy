package il.technion.cs236369.proxy;

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;

import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

/**
 * Handles a single request from a client
 * @author david
 *
 */
public class ProxyImpl {
	
	public ProxyImpl() throws IOException
	{
	
	}
	
	public void serveRequest(Socket socket, SocketFactory sFactory) throws IOException
	{
		print("Request received from "+socket.getInetAddress());
		
		
		DefaultBHttpServerConnection conn = null;
		try
		{
			ProxyHandler handler = new ProxyHandler(sFactory);
			//handler.manualHandle();
			/*HttpRequest request = conn.receiveRequestHeader();
			print(request.getRequestLine().getUri());///
			String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
			if (method.equals("GET"))
			{
				if (request instanceof HttpEntityEnclosingRequest) {
					print("Contains entity");
				    conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
				    HttpEntity entity = ((HttpEntityEnclosingRequest) request)
				            .getEntity();
				    if (entity != null) {
				        EntityUtils.consume(entity);
				    }
				}
			}*/
			
			 // Set up the HTTP protocol processor
	       HttpProcessor httpproc = HttpProcessorBuilder.create()
	                .add(new ResponseDate())
	                .add(new ResponseServer("Test/1.1"))
	                .add(new ResponseContent())
	                .add(new ResponseConnControl()).build();
	        // Set up request handler
	        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
	        reqistry.register("*", handler);
	        // Set up the HTTP service
	        HttpService httpService = new HttpService(httpproc, reqistry);
	        HttpContext coreContext = new BasicHttpContext(null);
	        
	        conn = new DefaultBHttpServerConnection(ProxyHandler.BUFSIZE);
	    	conn.bind(socket);
	    	
			httpService.handleRequest(conn, coreContext);
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (socket != null)
				socket.close();
			//if (conn != null)
				//conn.close();
		}
	}
	
	private void print(String message)
	{
		System.out.println(message);
	}
}
