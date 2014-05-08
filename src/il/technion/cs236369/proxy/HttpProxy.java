package il.technion.cs236369.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
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
	ServerSocket serverSocket;
	DefaultBHttpServerConnection conn;
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
		if (!serverSocket.isBound()) {
			System.out.println("Not bound!");
		}
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        HttpRequestHandler handler = new OurRequestHandler();
        reqistry.register("*", handler);

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);
    	HttpConnectionFactory<DefaultBHttpServerConnection> connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;

            HttpContext coreContext = new BasicHttpContext();
            while (true) {
            	try {
        				Socket socket = serverSocket.accept();
        	            DefaultBHttpServerConnection conn = connFactory.createConnection(socket);
        	            /*
        				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        				String firstLine = in.readLine();
        				if (!firstLine.startsWith("GET") || firstLine.length()<14 ||
        					    !(firstLine.endsWith("HTTP/1.1") || firstLine.endsWith("HTTP/1.0"))) {
        					System.out.println("Bad request");
        				}
        			    String req = firstLine.substring(4, firstLine.length()-9).trim();
        			    if (req.indexOf("..")!=-1 || req.indexOf("/.ht")!=-1 || req.endsWith("~")) {
        			    	System.out.println("Probably a hacker");
        			    }
        	            BasicHttpRequest request = new BasicHttpRequest("GET", req);
                        System.out.println(">> Request URI: " + request.getRequestLine().getUri());
                        String newLine;
                        //Print the rest of the request.
                        while (((newLine = in.readLine()) != null) && newLine.length() != 0) {
                        	int colonIndex = newLine.indexOf(":");
                        	String headerName = newLine.substring(0, colonIndex);
                        	String headerValue = newLine.substring(colonIndex+2, newLine.length());
                        	request.addHeader(headerName, headerValue);
                        }
                        */
                        /* Print all the headers to the console:
                    	Header[] allHeaders = request.getAllHeaders();
                    	for (Header h : allHeaders) {
                    		System.out.println(h.getName() + " " + h.getValue());
                    	}
                    	*/
                        //System.out.println(coreContext.toString());
                        //HttpConnectionMetrics met = conn.getMetrics();
                        //System.out.println(met.getRequestCount());
                    	httpService.handleRequest(conn, coreContext);
                    	System.out.println("Out");
                        /*HttpResponse response = null;
            			//OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            			
                        try {
							httpexecutor.preProcess(request, httpproc, coreContext);
	                        response = httpexecutor.execute(request, conn, coreContext);
	                        httpexecutor.postProcess(response, httpproc, coreContext);
						} catch (HttpException e) {
							e.printStackTrace();
						}
						


                        System.out.println("<< Response: " + response.getStatusLine());
                        System.out.println(EntityUtils.toString(response.getEntity()));
                        System.out.println("==============");
                        if (!connStrategy.keepAlive(response, coreContext)) {
                            conn.close();
                        } else {
                            System.out.println("Connection kept alive...");
                        } 
                        */
            	} catch (IOException e) {
					e.printStackTrace();
            	} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                finally {
                	System.out.println("Finally");
                	
                    try {
						conn.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
    }
		
		
		// request handler loop
		// ------
		/*
		HttpRequest hRequest = new BasicHttpRequest("GET", "/", HttpVersion.HTTP_1_1);			
		System.out.println(hRequest.getRequestLine().getMethod());
		System.out.println(hRequest.getRequestLine().getUri());
		System.out.println(hRequest.getProtocolVersion());
		System.out.println(hRequest.getRequestLine().toString());
		
		HttpResponse hResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		System.out.println(hResponse.getProtocolVersion());
		System.out.println(hResponse.getStatusLine().getStatusCode());
		System.out.println(hResponse.getStatusLine().getReasonPhrase());
		System.out.println(hResponse.getStatusLine().toString());
		
		hResponse.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
		hResponse.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");
		Header h1 = hResponse.getFirstHeader("Set-Cookie");
		System.out.println(h1);
		Header h2 = hResponse.getLastHeader("Set-Cookie");
		System.out.println(h2);
		Header[] hs = hResponse.getHeaders("Set-Cookie");
		System.out.println(hs.length);
		HeaderIterator it = hResponse.headerIterator("Set-Cookie");
		while (it.hasNext()) {
			 System.out.println(it.next());
		}
		HeaderElementIterator it2 = new BasicHeaderElementIterator(hResponse.headerIterator("Set-Cookie"));
		while (it2.hasNext()) {
			 HeaderElement elem = it2.nextElement();
			 System.out.println(elem.getName() + " = " + elem.getValue());
			 NameValuePair[] params = elem.getParameters();
			 for (int i = 0; i < params.length; i++) {
				 System.out.println(" " + params[i]);
			 }
		}
		StringEntity myEntity = new StringEntity("important message", Consts.UTF_8);
		System.out.println(myEntity.getContentType());
		System.out.println(myEntity.getContentLength());
		try {
			System.out.println(EntityUtils.toString(myEntity));
			System.out.println(EntityUtils.toByteArray(myEntity).length);
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HttpEntity entity = hResponse.getEntity();
		if (entity != null) {
			 InputStream instream = null;
			try {
				instream = entity.getContent();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				 try {
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		}
		*/
		/*
		HttpClientConnection h = null;
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent())
				.add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("MyClient/1.1"))
				.add(new RequestExpectContinue(true))
				.build();
				HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
				HttpRequest request2 = new BasicHttpRequest("GET", "/");
				HttpCoreContext context = HttpCoreContext.create();
				try {
					httpexecutor.preProcess(request2, httpproc, context);
					HttpResponse response = httpexecutor.execute(request2, h, context);
					httpexecutor.postProcess(response, httpproc, context);
					HttpEntity entity2 = response.getEntity();
					EntityUtils.consume(entity2);
				} catch (HttpException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	}
	*/

		//-----
				/*
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
*/
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
