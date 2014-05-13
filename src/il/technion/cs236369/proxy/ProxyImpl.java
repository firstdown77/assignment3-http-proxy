package il.technion.cs236369.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.util.EntityUtils;

/**
 * Handles a single request from a client
 * @author david
 *
 */
public class ProxyImpl {

	DefaultBHttpServerConnection conn;
	
	public ProxyImpl(Socket socket) throws IOException
	{
		System.out.println("Request received from "+socket.getInetAddress());
		conn = new DefaultBHttpServerConnection(8 * 1024);
		conn.bind(socket);
	}
	
	public void serveRequest() throws IOException
	{
		try
		{
			HttpRequest request = conn.receiveRequestHeader();
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
			}
		}
		catch (HttpException e)
		{
			
			throw new IOException(e);
		}
		finally
		{
			conn.shutdown();
		}
	}
	
	private void print(String message)
	{
		System.out.println(message);
	}
}
