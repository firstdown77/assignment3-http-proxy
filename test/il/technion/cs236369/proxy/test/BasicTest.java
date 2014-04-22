package il.technion.cs236369.proxy.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import il.technion.cs236369.proxy.HttpProxy;
import il.technion.cs236369.proxy.test.BlockingSocket;
import il.technion.cs236369.proxy.test.RequestExpectingSocket;
import il.technion.cs236369.proxy.test.ResponseExpectingSocket;
import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Scenario: - client sends request to proxy - proxy should forward request to
 * server - server replies with a cacheable response - proxy should cache the
 * response - proxy should send the response back to the client
 * 
 */
@SuppressWarnings("deprecation")
public class BasicTest {

	private final static String requestedURL = "http://jquery.com/index.html";
	private final static String responseBody = "some body";

	private static HttpProxyTestModule module;
	private static ResponseExpectingSocket clientToProxySocket1;
	private static RequestExpectingSocket proxyToServerSocket1;
	

	@BeforeClass
	public static void sendOneRequestAndRecvACachableResponse()
			throws Exception {

		module = new HttpProxyTestModule();

		clientToProxySocket1 = spy(new ResponseExpectingSocket(
				new ProxyHttpRequestBuilder(requestedURL).build()));

		when(module.getMockedServerSocket().accept()).thenReturn(
				clientToProxySocket1).thenReturn(new BlockingSocket());

		proxyToServerSocket1 = spy(new RequestExpectingSocket(
				new ProxyHttpResponseBuilder(HttpStatus.SC_OK)
						.addHeader("Last-Modified", "Sat, 28 Jan 2014 16:26:59 GMT")
						.addHeader("Content-Type", "text/plain")
						.addHeader("Content-Length", "" + responseBody.length())
						.setBody(responseBody).build()));

		when(module.getMockedSockFactory().createSocket(anyString(), anyInt()))
				.thenReturn(proxyToServerSocket1);

		Injector injector = Guice.createInjector(module);

	

		final HttpProxy proxy = injector.getInstance(HttpProxy.class);

		proxy.bind();
		new Thread(new Runnable() {
			@Override
			public void run() {
				proxy.start();
			}
		}).start();

		Thread.sleep(1000);

		// check the proxy listens on the correct port
		verify(module.getMockedServerSocketFactory(), times(1))
				.createServerSocket(8080);
		// check only a single connection was opened with the server
		verify(module.getMockedSockFactory(), times(1)).createSocket(
				"jquery.com", 80);

		// check all sockets were closed
		verify(clientToProxySocket1, times(1)).close();
		verify(proxyToServerSocket1, times(1)).close();
	}

	@Test
	public void clientShouldGetTheResponse() throws Exception {
		HttpResponse res = clientToProxySocket1.getRecvedResponse(3000);
		boolean passed = true;
		Assert.assertEquals("Wrong status code", HttpStatus.SC_OK, res
				.getStatusLine().getStatusCode());

		if (HttpStatus.SC_OK != res.getStatusLine().getStatusCode()) {
			System.out.println("Wrong status code");
			passed = false;
		}

		String entity = EntityUtils.toString(res.getEntity());

		if (!responseBody.equals(entity)) {
			System.out.println("Response has wrong content");
			passed = false;
		}
		Assert.assertEquals("Response has wrong content", responseBody, entity);

		if (passed) {
			System.out.println("Test Passed");
		}
		System.exit(0);
	}
}
