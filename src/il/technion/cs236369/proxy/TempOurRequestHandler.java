package il.technion.cs236369.proxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TempOurRequestHandler implements HttpRequestHandler {
    CloseableHttpClient client = HttpClients.createDefault();
	private Cache theCache;
    
    public TempOurRequestHandler() {
        Injector inj = Guice.createInjector(new HttpProxyModule());
		theCache = inj.getInstance(Cache.class);
    }
    
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestLine().getUri();
        System.out.println(target);
        HttpGet requestToClient = new HttpGet(target);
        CloseableHttpResponse responseFromClient = client.execute(requestToClient);
        HttpEntity theEntity = responseFromClient.getEntity();
        Header[] h = responseFromClient.getHeaders("Cache-Control");
        boolean nocache = false;
        boolean nostore = false;
        for (int i = 0; i < h.length; i++) {
        	if (h[i].getValue().equals("no-cache")) {
        		nocache = true;
        	}
        	if (h[i].getValue().equals("no-store")) {
        		nostore = true;
        	}
        }
		if (nocache) {
			//store in cache if necessary but do not serve from cache.  Serve from fresh response.
			try {
				theCache.insert(target, responseFromClient);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    if (!nocache && !nostore && responseFromClient.toString().length() < 65535
	    		&& method.equals("GET") && responseFromClient.getStatusLine().getStatusCode() == 200
	    		&& responseFromClient.containsHeader("Last-Modified")) {
	    	//may serve from cache if the cache contains the resource.
	    	HttpResponse cachedResponse;
			try {
				cachedResponse = theCache.search(target);
		    	if (cachedResponse != null) {
		    		System.out.println("Serving from cache");
		    		theEntity = cachedResponse.getEntity();
		    	}
		    	else {
		    		theCache.insert(target, cachedResponse);
		    	}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        response.setStatusCode(200);
        response.setHeaders(request.getAllHeaders());
        response.setEntity(theEntity);
        System.out.println("<< Response: " + response.getStatusLine());
	}
}