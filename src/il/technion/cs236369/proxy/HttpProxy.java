package il.technion.cs236369.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class HttpProxy extends AbstractHttpProxy {
	ServerSocket serverSocket;
	
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
		// Add your code here
	}

	public void receiveRequestFromClientSocket(HashMap<String, String> hm) {
		
	}
	
	/**
	 * Creates a client socket and connects it to the server socket.
	 * @param host The website hostname.
	 * @return A new client socket connected with the server socket,
	 * both sockets having the same port number.
	 */
	public Socket createClientSocket(String host) {
		try {
			Socket clientSocket = sockFactory.createSocket(host, port);
			clientSocket.connect(serverSocket.getLocalSocketAddress());
			return clientSocket;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	//I have been looking at this tutorial: 
	//docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/networking/sockets/examples/KnockKnockServer.java
	public void bind() throws IOException {
		serverSocket = srvSockFactory.createServerSocket(port); //This line does the bind.
		
		//clientSocket = sockFactory.createSocket();
		//clientSocket.connect(serverSocket.getLocalSocketAddress());
	}

	@Override
	public void start() {
		/* We'll need to get cache entries from previous instances of the proxy. */
		//String[] = ourDatabase.getPreviousCacheEntries();  
        //Need to use HTTPCore here.
		while (true) {
			try {
				serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
