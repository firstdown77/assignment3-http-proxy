package il.technion.cs236369.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.net.SocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

public class ProxyHandler implements HttpRequestHandler {
	
    //CloseableHttpClient client = HttpClients.createDefault();
	private Cache theCache;
	private SocketFactory sFactory;
    
    
    public final static int BUFSIZE = 8 * 1024;
    
    public ProxyHandler(SocketFactory sfactory) throws IOException {
        this.sFactory = sfactory;
			
		theCache = new Cache();
    }
    
    /*
     * Workaround instead of a service handler to use the socketfactory and pass the basic test
     * 
     */
    
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			{
		String target;
		String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
		print(method);
        if (!method.equals("GET") && !method.equals("HEAD")) {
        	print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Bypassing method: "+method);
        	//HttpResponse resp = bypassProxy(request);
            //response.setHeaders(resp.getAllHeaders());
            //response.setEntity(resp.getEntity());
        	//throw new MethodNotSupportedException(method + " method not supported");
        	response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        	return;
        }
        
        DefaultBHttpClientConnection connToWeb = null;
		Socket socketToWebServer = null;
	    try
		{
	        target = request.getRequestLine().getUri();
	        print(target);
	      
	        URI uri = encodeUrl(target);
			if (uri == null) throw new IOException("Malformed URL requesting a page from server");
			connToWeb = new DefaultBHttpClientConnection(BUFSIZE);			
			URL url = new URL(uri.toString());
			int port;
			if (url.getPort() <= 0)
				port = url.getDefaultPort();
			else
				port = url.getPort();
			socketToWebServer = sFactory.createSocket(url.getHost(), port);
			connToWeb.bind(socketToWebServer);
			
	        PageResponse page = cacheDecissionTree(connToWeb, socketToWebServer, request, uri);
	        
	        response.setStatusCode(page.getStatus());
	        page.removeHeader("Content-Length");
	        page.removeHeader("Transfer-Encoding");
	        response.setHeaders(page.getHeaders());
	        byte[] bytes = page.getBodyBytes();
	        if ((bytes != null)&&(bytes.length > 0))
	        {
	        	ByteArrayEntity be = new ByteArrayEntity(bytes);
	        	response.setEntity(be);
	        	EntityUtils.consume(page.getBody());
	        }else{
	        	//We set directly the entity into the response in case it is too big to be kept in
	        	//memory --> Streamed 
	        	response.setEntity(page.getBody());
	        }
	        
	        
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.setReasonPhrase("Internal Server Error.");
		}
		finally
		{
			//if (connToWeb != null)
				//try{connToWeb.close();}catch (Exception e){}
			if (socketToWebServer != null)
				try{socketToWebServer.close();}catch (Exception e){}
		}
	    print("<< Response: " + response.getStatusLine());
	}
	
	private void print(String message)
	{
		System.out.println(message);
	}
	
	private PageResponse cacheDecissionTree(DefaultBHttpClientConnection connToWeb, Socket socketToWebServer, HttpRequest request, URI uri) throws IOException
	{
			Header[] h = request.getHeaders("Cache-Control");
	        boolean nocache = false;
	        boolean nostore = false;
	        for (int i = 0; i < h.length; i++) {
	        	if (h[i].getValue().equals("no-cache")) {
	        		print("no cacheable");
	        		nocache = true;
	        	}
	        	if (h[i].getValue().equals("no-store")) {
	        		print("no storeable");
	        		nostore = true;
	        	}
	        }
	        
	        if ((!nocache)&&(!nostore))
	        {
	        	//Cache headers not found
	        	PageResponse page = theCache.retrieveFromCache(uri.toString());
	        	if (page == null)
	        	{
	        		page = getPageFromServer(connToWeb, socketToWebServer, request.getAllHeaders(), uri.toString());
	        		if ((page != null)&&(page.getStatus() == HttpStatus.SC_OK)&&(!page.isNoCache())&&
	        				(!page.isNoStore())&&(page.lastModified != ""))
	        			savePage(page);
	        		return page;
	        	}
	        	else
	        	{
	        		PageResponse newPage = getPageFromServer(connToWeb, socketToWebServer, request.getAllHeaders(), uri.toString(), 
	        				page.getLastModified());
	        		if (newPage.getStatus() == HttpStatus.SC_NOT_MODIFIED)
	        		{
	        			//The page has not been modified. The entry in cache is valid and can be returned
	        			return page;
	        		}
	        		else
	        		{
	        			//The page has been modified and must be refreshed in the cache
	        			if ((newPage != null)&&(newPage.getStatus() == HttpStatus.SC_OK))
	        			{
	        				if ((!newPage.isNoCache())&&
	                				(!newPage.isNoStore())&&(page.lastModified != ""))
	                			savePage(newPage);
	        				else
	        					theCache.deleteFromCache(uri.toString()); //Page not cacheable anymore
	        				return newPage;
	        			}
	        			else
	        			{
	        				//The page is not there anymore. delete from cache
	        				theCache.deleteFromCache(uri.toString());
	        				//Throw exception to create a 500 internal server error
	        				throw new IOException("Page is non existent anymore");
	        			}
	        		}
	        	}
	        }
	        else
	        {
	        	//Client wishes to bypass the cache
	        	PageResponse page = getPageFromServer(connToWeb, socketToWebServer, request.getAllHeaders(), uri.toString());
	        	if ((page != null)&&(page.getStatus() == HttpStatus.SC_OK)&&(!page.isNoCache())&&
	    				(!page.isNoStore())&&(page.lastModified != ""))
	    			savePage(page);
	    		return page;
	        }
		
	}
	
	private void savePage(PageResponse page)
	{
		try
		{
			theCache.saveInCache(page);
		}
		catch (IllegalArgumentException iae)
		{
			//TODO check if this message needs to be printed in the console
			System.err.println(">>>>>>>>>>>Page: "+page.getUrl()+"uncacheable: "+iae.getMessage());
		}
	}
	
/*	private HttpResponse bypassProxy(HttpRequest request) throws IOException
	{
		HttpHost host = new HttpHost(request.getFirstHeader("Host").getValue());
		return client.execute(host, request);
	}
	*/
	
	private PageResponse getPageFromServer(DefaultBHttpClientConnection connToWeb, Socket socketToWebServer, Header[] headers, String page) throws IOException
	{	
		return getPageFromServer(connToWeb, socketToWebServer, headers, page, null);
	}
	
	private PageResponse getPageFromServer(DefaultBHttpClientConnection connToWeb, Socket socketToWebServer, Header[] headers, String page, String validationDate) throws IOException
	{
		try
		{	
			HttpRequest requestToWebServer = new BasicHttpRequest("GET", page);
			for (Header h: headers)
				if (h.getName() != "Connection")
					requestToWebServer.addHeader(h);
			requestToWebServer.addHeader("Connection", "close");
			if (validationDate != null)
			{
				Header hdr = requestToWebServer.getFirstHeader("If-Modified-Since");
				if (hdr != null)
					requestToWebServer.removeHeader(hdr);
				requestToWebServer.addHeader("If-Modified-Since", validationDate);
			}

			connToWeb.sendRequestHeader(requestToWebServer);
			connToWeb.flush();
			HttpResponse responseFromClient = connToWeb.receiveResponseHeader();
	        int statusCode = responseFromClient.getStatusLine().getStatusCode();
	
	        Header[] h = responseFromClient.getHeaders("Cache-Control");
	        boolean nocache = false;
	        boolean nostore = false;
	        for (int i = 0; i < h.length; i++) {
	        	if (h[i].getValue().equals("no-cache")) {
	        		print("no cacheable");
	        		nocache = true;
	        	}
	        	if (h[i].getValue().equals("no-store")) {
	        		print("no storeable");
	        		nostore = true;
	        	}
	        }
	        Header[] modHeaders = responseFromClient.getHeaders("Last-Modified");
		    String modHeader = "";
	        if (modHeaders.length > 0)
	        	modHeader = modHeaders[0].getValue();
	        connToWeb.receiveResponseEntity(responseFromClient);
	        HttpEntity theEntity = responseFromClient.getEntity();
	        return new PageResponse(page, statusCode, responseFromClient.getAllHeaders(), nocache, 
	        		nostore, theEntity, modHeader);
		}
		catch (HttpException hte)
		{
			throw new IOException(hte);
		}
	}
	
	private URI encodeUrl(String page) throws IOException
	{
		URL url = new URL(page);
		try{
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			return uri;
		}catch (URISyntaxException e){e.printStackTrace(); return null;}
	}
}