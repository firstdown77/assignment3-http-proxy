package il.technion.cs236369.proxy.data;

import java.io.FileInputStream;
import java.util.Properties;

import il.technion.cs236369.proxy.HttpProxyModule;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class PageResponseDAOFactory {
	
	public static IPageResponseDAO createPageResponseDAO() throws Exception
	{
		Properties p = new Properties();
		p.load(new FileInputStream("config"));
		Injector inj = Guice.createInjector(new HttpProxyModule(p));
		IPageResponseDAO storage = inj.getInstance(PageResponseDAO.class);
		return storage;
	}

}
