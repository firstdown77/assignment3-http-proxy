package il.technion.cs236369.proxy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class HttpProxy extends AbstractHttpProxy {
	ServerSocket serverSocket;
	Socket clientSocket;
	
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

	@Override
	//I have been looking at this tutorial: 
	//docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/networking/sockets/examples/KnockKnockServer.java
	public void bind() throws IOException {
		serverSocket = srvSockFactory.createServerSocket(port);
		clientSocket = sockFactory.createSocket();
        PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
                           
            String inputLine, outputLine;
	}

	@Override
	public void start() {
		while (true) {
			
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
