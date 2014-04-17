package il.technion.cs236369.proxy;

import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class HttpProxyModule extends AbstractModule {

	private final Properties properties;

	private Properties getDefaultProperties() {
		Properties defaultProps = new Properties();

		defaultProps.setProperty("httproxy.db.driver", "com.mysql.jdbc.Driver");
		defaultProps.setProperty("httproxy.db.url",
				"jdbc:mysql://127.0.0.1:3306/");
		defaultProps.setProperty("httproxy.db.name", "proxy");
		defaultProps.setProperty("httproxy.db.username", "root");
		defaultProps.setProperty("httproxy.db.password", "1234");
		defaultProps.setProperty("httproxy.net.port", "8080");
		defaultProps.setProperty("httproxy.db.table", "cache");

		return defaultProps;
	}

	public HttpProxyModule() {
		this(new Properties());
	}

	public HttpProxyModule(Properties properties) {
		this.properties = getDefaultProperties();
		this.properties.putAll(properties);
	}

	public HttpProxyModule setProperty(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	@Override
	protected void configure() {
		Names.bindProperties(binder(), properties);

		bind(ServerSocketFactory.class).toInstance(
				ServerSocketFactory.getDefault());
		bind(SocketFactory.class).toInstance(SocketFactory.getDefault());

		bind(ICache.class).to(Cache.class).in(Scopes.SINGLETON);
		bind(IHttpProxy.class).to(HttpProxy.class).in(Scopes.SINGLETON);

		// bind(HttpProxy.class).in(Scopes.SINGLETON);
	}

}
