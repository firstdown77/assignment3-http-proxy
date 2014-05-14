package il.technion.cs236369.proxy.data;

import java.io.FileInputStream;
import java.util.Properties;

import il.technion.cs236369.proxy.HttpProxyModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 * Factory to improve our cache use architecture.
 * @author Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 *
 */
public class PageResponseDAOFactory {
	
	/**
	 * Creates a page response database access object.
	 * @return The database access object.
	 * @throws Exception This method may throw an exception.
	 */
	public static IPageResponseDAO createPageResponseDAO() throws Exception
	{
		Properties p = new Properties();
		p.load(new FileInputStream("config"));
		Injector inj = Guice.createInjector(new HttpProxyModule(p));
		IPageResponseDAO storage = inj.getInstance(PageResponseDAO.class);
		return storage;
	}

}
