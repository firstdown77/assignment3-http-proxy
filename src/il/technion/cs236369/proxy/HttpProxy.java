package il.technion.cs236369.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpService;
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
	public void bind() throws IOException {
		serverSocket = srvSockFactory.createServerSocket(port); //This line does the bind.
	}

	@Override
	public void start() {
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
		HttpHost target = new HttpHost("localhost", port);
        final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
        //reqistry.register("*", new OurRequestHandler(target, httpproc, httpexecutor));
        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);
        HttpContext coreContext = new BasicHttpContext(null);
        while (true) {
           	try {
        			Socket socket = serverSocket.accept();
                    DefaultBHttpServerConnection conn = new DefaultBHttpServerConnection(BUFSIZE);
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket);
                    httpService.handleRequest(conn, coreContext);
           	} catch (IOException e) {
				e.printStackTrace();
           	} catch (HttpException e) {
				e.printStackTrace();
			}
            finally {
            	//Close anything that needs to be closed.
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
