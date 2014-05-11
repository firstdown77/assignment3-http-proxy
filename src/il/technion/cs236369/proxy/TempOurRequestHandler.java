package il.technion.cs236369.proxy;

import java.io.IOException;
import java.util.Locale;
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

public class TempOurRequestHandler implements HttpRequestHandler {
    
    CloseableHttpClient client = HttpClients.createDefault();
    
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		
		String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestLine().getUri();
        System.out.println(target);
        /*
        HttpEntity theEntity = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            theEntity = ((HttpEntityEnclosingRequest) request).getEntity();
            byte[] entityContent = EntityUtils.toByteArray(theEntity);
            System.out.println("Incoming entity content (bytes): " + entityContent.length);
        }
        */
        HttpGet request2 = new HttpGet(target);
        CloseableHttpResponse response2 = client.execute(request2);
        HttpEntity theEntity = response2.getEntity();
        /*InputStream theReader = theEntity.getContent();
        //Get the response
        
        
        BufferedReader rd = new BufferedReader
          (new InputStreamReader(theReader));
            
        String line = "";
        while ((line = rd.readLine()) != null) {
        	System.out.println(line);
        } */
        
        //byte[] responseBody = method2.getResponseBody();
        
        response.setStatusCode(200);
        response.setHeaders(request.getAllHeaders());
        response.setEntity(theEntity);
        
        System.out.println("<< Response: " + response.getStatusLine());
	}
}