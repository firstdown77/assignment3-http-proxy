package il.technion.cs236369.proxy;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpRequestHandler;

public class OurRequestHandler implements HttpRequestHandler {
    private HttpHost target;
    private HttpProcessor httpproc;
    private HttpRequestExecutor httpexecutor;
    private ConnectionReuseStrategy connStrategy;
    
    private static final String HTTP_IN_CONN = "http.proxy.in-conn";
    private static final String HTTP_OUT_CONN = "http.proxy.out-conn";
    private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";

    public OurRequestHandler(HttpHost target, HttpProcessor httpproc, HttpRequestExecutor httpexecutor) {
        this.target = target;
        this.httpproc = httpproc;
        this.httpexecutor = httpexecutor;
        this.connStrategy = DefaultConnectionReuseStrategy.INSTANCE;
    }
    
    
    
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        /*if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestLine().getUri();
        String decodedTarget = URLDecoder.decode(target, "UTF-8");
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            byte[] entityContent = EntityUtils.toByteArray(entity);
            System.out.println("Incoming entity content (bytes): " + entityContent.length);
        }
        */
        final HttpClientConnection conn = (HttpClientConnection) context.getAttribute(
                HTTP_OUT_CONN);

        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, conn);
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        
        System.out.println(">> Request URI: " + request.getRequestLine().getUri());
        
        // Remove hop-by-hop headers
        request.removeHeaders(HTTP.CONTENT_LEN);
        request.removeHeaders(HTTP.TRANSFER_ENCODING);
        request.removeHeaders(HTTP.CONN_DIRECTIVE);
        request.removeHeaders("Keep-Alive");
        request.removeHeaders("Proxy-Authenticate");
        request.removeHeaders("TE");
        request.removeHeaders("Trailers");
        request.removeHeaders("Upgrade");
        
        this.httpexecutor.preProcess(request, this.httpproc, context);
        System.out.println(request);
        HttpResponse targetResponse = this.httpexecutor.execute(request, conn, context);
        this.httpexecutor.postProcess(response, this.httpproc, context);
        
        // Remove hop-by-hop headers
        targetResponse.removeHeaders(HTTP.CONTENT_LEN);
        targetResponse.removeHeaders(HTTP.TRANSFER_ENCODING);
        targetResponse.removeHeaders(HTTP.CONN_DIRECTIVE);
        targetResponse.removeHeaders("Keep-Alive");
        targetResponse.removeHeaders("TE");
        targetResponse.removeHeaders("Trailers");
        targetResponse.removeHeaders("Upgrade");
                        
        if (targetResponse.containsHeader("no-cache")) {
        	//store in cache if necessary but do not serve from cache.  Serve from fresh response.
        }
        
        if (!targetResponse.containsHeader("no-cache") && !targetResponse.containsHeader("no-store") && targetResponse.toString().length() < 65535
        		&& method.equals("GET") && targetResponse.getStatusLine().getStatusCode() == 200 && targetResponse.containsHeader("Last-Modified")) {
        	//may serve from cache if necessary.
        }
        
        response.setStatusLine(targetResponse.getStatusLine());
        response.setHeaders(targetResponse.getAllHeaders());
        response.setEntity(targetResponse.getEntity());
        
        System.out.println("<< Response: " + response.getStatusLine());

        final boolean keepalive = this.connStrategy.keepAlive(response, context);
        context.setAttribute(HTTP_CONN_KEEPALIVE, new Boolean(keepalive));
	}
}
