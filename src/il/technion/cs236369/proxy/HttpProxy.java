package il.technion.cs236369.proxy;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
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

	/**
	 * The HashMap contains the following according to the assignment instructions:
	 * 1. URL
	 * 2. headers
	 * 3. response
	 * 4. lastmodified - may be null
	 * 
	 * @param hm A HashMap containing the request's information.
	 */
	public void receiveRequestFromClientSocket(HashMap<String, String> hm) {
		hm.get("url");
		hm.get("headers");
		hm.get("response");
		hm.get("lastmodified");
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
	}

	@Override
	/**
	 * Current version copied from: http://cs.au.dk/~amoeller/WWW/javaweb/server.html
	 */
	public void start() {
		/* We'll need to get cache entries from previous instances of the proxy. */
		//String[] = ourDatabase.getPreviousCacheEntries();  
        //Need to use HTTPCore here.
		System.out.println("FileServer accepting connections on port " + port);
		// request handler loop
		while (true) {
		    Socket connection = null;
		    try {
			// wait for request
			connection = serverSocket.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			OutputStream out = new BufferedOutputStream(connection.getOutputStream());
			PrintStream pout = new PrintStream(out);
			
			// read first line of request (ignore the rest)
			String request = in.readLine();
			if (request==null)
			    continue;
			log(connection, request);
			while (true) {
			    String misc = in.readLine();
			    if (misc==null || misc.length()==0)
				break;
			}
			
			// parse the line
			if (!request.startsWith("GET") || request.length()<14 ||
			    !(request.endsWith("HTTP/1.1") || request.endsWith("HTTP/1.0"))) {
			    // bad request
			    errorReport(pout, connection, "400", "Bad Request", 
					"Your browser sent a request that " + 
					"this server could not understand.");
			} else {
			    String req = request.substring(4, request.length()-9).trim();
			    if (req.indexOf("..")!=-1 || 
				req.indexOf("/.ht")!=-1 || req.endsWith("~")) {
				// evil hacker trying to read non-wwwhome or secret file
				errorReport(pout, connection, "403", "Forbidden",
					    "You don't have permission to access the requested URL.");
			    } else {
				String path = req;
				File f = new File(path);
				if (f.isDirectory() && !path.endsWith("/")) {
				    // redirect browser if referring to directory without final '/'
				    pout.print("HTTP/1.0 301 Moved Permanently\r\n" +
					       "Location: http://" + 
					       connection.getLocalAddress().getHostAddress() + ":" +
					       connection.getLocalPort() + "/" + req + "/\r\n\r\n");
				    log(connection, "301 Moved Permanently");
				} else {
				    if (f.isDirectory()) { 
					// if directory, implicitly add 'index.html'
					path = path + "index.html";
					f = new File(path);
				    }
				    try { 
					// send file
					InputStream file = new FileInputStream(f);
					pout.print("HTTP/1.0 200 OK\r\n" +
						   "Content-Type: " + guessContentType(path) + "\r\n" +
						   "Date: " + new Date() + "\r\n" +
						   "Server: FileServer 1.0\r\n\r\n");
					sendFile(file, out); // send raw file 
					log(connection, "200 OK");
				    } catch (FileNotFoundException e) { 
					// file not found
					errorReport(pout, connection, "404", "Not Found",
						    "The requested URL was not found on this server.");
				    }
				}
			    }
			}
			out.flush();
		    } catch (IOException e) { System.err.println(e); }
		    try {
			if (connection != null) connection.close(); 
		    } catch (IOException e) { System.err.println(e); }
		}
	    }
	    
	    private static void log(Socket connection, String msg)
	    {
		System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress() + 
				   ":" + connection.getPort() + "] " + msg);
	    }

	    private static void errorReport(PrintStream pout, Socket connection,
					    String code, String title, String msg)
	    {
		pout.print("HTTP/1.0 " + code + " " + title + "\r\n" +
			   "\r\n" +
			   "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
			   "<TITLE>" + code + " " + title + "</TITLE>\r\n" +
			   "</HEAD><BODY>\r\n" +
			   "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n" +
			   "<HR><ADDRESS>FileServer 1.0 at " + 
			   connection.getLocalAddress().getHostName() + 
			   " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" +
			   "</BODY></HTML>\r\n");
		log(connection, code + " " + title);
	    }

	    private static String guessContentType(String path)
	    {
		if (path.endsWith(".html") || path.endsWith(".htm")) 
		    return "text/html";
		else if (path.endsWith(".txt") || path.endsWith(".java")) 
		    return "text/plain";
		else if (path.endsWith(".gif")) 
		    return "image/gif";
		else if (path.endsWith(".class"))
		    return "application/octet-stream";
		else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
		    return "image/jpeg";
		else 	
		    return "text/plain";
	    }

	    private static void sendFile(InputStream file, OutputStream out)
	    {
		try {
		    byte[] buffer = new byte[1000];
		    while (file.available()>0) 
			out.write(buffer, 0, file.read(buffer));
		} catch (IOException e) { System.err.println(e); }
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
