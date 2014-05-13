package il.technion.cs236369.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class HttpProxy extends AbstractHttpProxy {
	private ServerSocket serverSocket;
	

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
	
	@Override
	public void bind() throws IOException {
		serverSocket = srvSockFactory.createServerSocket(port); 
		//socket = sockFactory.createSocket();
	}

	@Override
	public void start() {
       
        while (true) {
        	
           	try {
                    ProxyImpl proxy = new ProxyImpl();
                    proxy.serveRequest(serverSocket.accept(), sockFactory);
           	}
           	/*catch (SocketTimeoutException e) {
           		//TODO confirm we want this exception handler.
           		shouldShutdown = false;
           		e.printStackTrace(System.err);
        	}*/ catch (IOException e) {
				e.printStackTrace(System.err);
        	} catch (Exception e) {
				e.printStackTrace(System.err);
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
