package il.technion.cs236369.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class HttpProxy extends AbstractHttpProxy {
	private ServerSocket serverSocket;
	private final int BUFSIZE = 8 * 1024;
    private static final String HTTP_IN_CONN = "http.proxy.in-conn";
    private static final String HTTP_OUT_CONN = "http.proxy.out-conn";
    private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";
    Socket insocket;
    Socket outsocket;

	@Inject
	HttpProxy(SocketFactory sockFactory, ServerSocketFactory srvSockFactory,

	@Named("httproxy.net.port") int port,
			@Named("httproxy.db.url") String dbURL,
			@Named("httproxy.db.name") String dbName,
			@Named("httproxy.db.table") String tblName,
			@Named("httproxy.db.username") String dbUsername,
			@Named("httproxy.db.password") String dbPassword,
			@Named("httproxy.db.driver") String dbDriver)
			throws ClassNotFoundException {
		super(sockFactory, srvSockFactory, port, dbURL, dbName, tblName,
				dbUsername, dbPassword, dbDriver);
	}

	/**
	 * The HashMap contains the following according to the assignment instructions:
	 * 1. URL
	 * 2. headers
	 * 3. response
	 * 4. lastmodified - may be null
	 * 
	 * @param hm A HashMap containing the request's information.
	 */
	
	@Override
	//I have been looking at this tutorial: 
	//docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/networking/sockets/examples/KnockKnockServer.java
	public void bind() throws IOException {
		serverSocket = srvSockFactory.createServerSocket(port); //This line does the bind.
	}

	@Override
	public void start() {
        // Set up HTTP protocol processor for incoming connections
        final HttpProcessor inhttpproc = new ImmutableHttpProcessor(
                new HttpRequestInterceptor[] {
                        new RequestContent(),
                        new RequestTargetHost(),
                        new RequestConnControl(),
                        new RequestUserAgent("Test/1.1"),
                        new RequestExpectContinue(true)
         });

        // Set up HTTP protocol processor for outgoing connections
        final HttpProcessor outhttpproc = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[] {
                        new ResponseDate(),
                        new ResponseServer("Test/1.1"),
                        new ResponseContent(),
                        new ResponseConnControl()
        });
        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		HttpHost target = new HttpHost("localhost", port);
        final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
        reqistry.register("*", new OurRequestHandler(target, outhttpproc, httpexecutor));
        // Set up the HTTP service
        HttpService httpService = new HttpService(inhttpproc, reqistry);
        HttpContext coreContext = new BasicHttpContext(null);
           while (true) {
           	try {
        			Socket insocket = serverSocket.accept();
                    DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(BUFSIZE);
                    System.out.println("Incoming connection from " + insocket.getInetAddress());
                    inconn.bind(insocket);
                    Socket outsocket = sockFactory.createSocket(target.getHostName(), target.getPort());
                    DefaultBHttpClientConnection outconn = new DefaultBHttpClientConnection(BUFSIZE);
        	        outconn.bind(outsocket);
                    coreContext.setAttribute(HTTP_IN_CONN, inconn);
					coreContext.setAttribute(HTTP_OUT_CONN, outconn);
                    System.out.println("Outgoing connection to " + outsocket.getInetAddress());
                    if (!inconn.isOpen()) {
                    	System.out.println("Closing");
                        outconn.close();
                        break;
                    }
                   	httpService.handleRequest(inconn, coreContext);
                    final Boolean keepalive = (Boolean) coreContext.getAttribute(HTTP_CONN_KEEPALIVE);
                    if (!Boolean.TRUE.equals(keepalive)) {
                        outconn.close();
                        inconn.close();
                        System.out.println("Don't keep alive.");
                        break;
                    }
           	} catch (IOException e) {
				e.printStackTrace();
           	} catch (HttpException e) {
				e.printStackTrace();
			}
            finally {
            	try {
            		if (!insocket.isClosed()) {
    					insocket.close();
            		}
            		if (!outsocket.isClosed()) {
    	            	outsocket.close();
            		}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
	public static void main(String[] args) throws Exception {
		Properties p = new Properties();
		p.load(new FileInputStream("config"));
		Injector inj = Guice.createInjector(new HttpProxyModule(p));
		// Injector inj = Guice.createInjector(new HttpProxyModule());
		IHttpProxy proxy = inj.getInstance(HttpProxy.class);
		proxy.bind();
		proxy.start();

	}
}
