package il.technion.cs236369.proxy.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import il.technion.cs236369.proxy.PageResponse;

/**
 * Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 * Please refer to IPageResponseDAO for Javadoc entries.
 * @author Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 *
 */
public class PageResponseDAO implements IPageResponseDAO {

	private static int MAX_URL_LENGTH = 255;
	private static int MAX_BODY_LENGTH = 65535;
	private Connection con;
	boolean isOpen = false;
	
	private final String dbURL;
	private final String dbName;
	private final String tblName;
	private final String dbUsername;
	private final String dbPassword;
	private final String dbDriver;
	
	@Inject
	PageResponseDAO(@Named("httproxy.db.url") String dbURL,
			@Named("httproxy.db.name") String dbName,
			@Named("httproxy.db.table") String tblName,
			@Named("httproxy.db.username") String dbUsername,
			@Named("httproxy.db.password") String dbPassword,
			@Named("httproxy.db.driver") String dbDriver)
			throws ClassNotFoundException {

		this.dbURL = dbURL;
		this.dbName = dbName;
		this.tblName = tblName;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
		this.dbDriver = dbDriver;
	}
	
	/**
	 * Open a cache connection.
	 */
	@Override
	public void open() {
		if (isOpen) return;
		
        try {
	        Class.forName(dbDriver).newInstance();
	        con = DriverManager.getConnection(dbURL+dbName,dbUsername,dbPassword);
	        isOpen = true; 
        } catch (Exception e) {
        	e.printStackTrace(System.err);
        }
	}

	/**
	 * Close a cache connection.
	 */
	@Override
	public void close() {
		isOpen = false;
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Truncates the url is it exceeds the maximum length allowed in the database
	 * @param url
	 * @return
	 */
	private String adaptURL(String url)
	{
		if (url.length() > MAX_URL_LENGTH)
			return url.substring(0, MAX_URL_LENGTH-1);
		else
			return url;
	}

	/**
	 * Gets the page response from the cache.
	 */
	@Override
	public PageResponse getPageResponse(String url) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			open();
			stmt = con.prepareStatement("SELECT * FROM " + tblName
					+ " WHERE url = ?");
			stmt.setString(1, adaptURL(url));
			rs = stmt.executeQuery();
			if ((rs==null)||(!rs.next()))
				return null;

			String lastModified = rs.getString("lastmodified");
			String headers = rs.getString("headers");
			Scanner scanner = new Scanner(headers);
			scanner.useDelimiter("\r\n");
			ArrayList<Header> headersArray = new ArrayList<Header>();
			while (scanner.hasNext()) {
				String header = scanner.next();
				if (header.isEmpty())
					break;
				String name = header.substring(0, header.indexOf(":")).trim();
				String value = header.substring(name.length() + 1).trim();
				headersArray.add(new BasicHeader(name, value));
			}
			scanner.close();
			
			Header[] colHeaders = new Header[headersArray.size()];
			colHeaders = headersArray.toArray(colHeaders);
			byte[] body = rs.getBytes("body");
			PageResponse response = new PageResponse(url, HttpStatus.SC_OK, 
					colHeaders, false, false, body, lastModified);
			return response;

		} 
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			return null;
		}
		finally {
			if (rs != null)
				try{rs.close();}catch(SQLException e){}
			if (stmt != null)
				try{stmt.close();}catch(SQLException e){}
			close();
		}
	}

	/**
	 * Deletes a page response from the cache. Wrapper function.
	 */
	@Override
	public boolean deletePageResponse(String url)
	{
		return deletePageResponse(url, true);
	}
	
	/**
	 * Inner function to delete page response.
	 * @param url URL to delete
	 * @param close Whether or not to close the cache.
	 * @return
	 */
	private boolean deletePageResponse(String url, boolean close) {
		PreparedStatement stmt = null;

		try {
			open();
			stmt = con.prepareStatement("DELETE FROM " + tblName
					+ " WHERE url = ?");
			stmt.setString(1, adaptURL(url));
			return(stmt.executeUpdate() > 0);
		} 
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			return false;
		}
		finally {
			if (stmt != null)
				try{stmt.close();}catch(SQLException e){}
			if (close)
				close();
		}
	}

	@Override
	/**
	 * Saves a page response and overwrites it if it exists
	 */
	public boolean savePageResponse(PageResponse page) {
		PreparedStatement stmt = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			byte[] bodyBytes = page.getBodyBytes();
			if (bodyBytes.length > MAX_BODY_LENGTH)
				throw new IllegalArgumentException("Page body exceeds maximum length for caching.");
			open();
			String SQL_QUERY= "Select * from "+tblName+" where url='"+adaptURL(page.getUrl())+"'";
			st = con.createStatement();
			rs = st.executeQuery(SQL_QUERY);
						
			if(rs.next()) {
				//Page exists
				rs.close();
				rs = null;
				deletePageResponse(adaptURL(page.getUrl()), false);
			}
			st.close();
			st = null;

			stmt = con.prepareStatement("INSERT INTO " + tblName
					+ " VALUES(?, ?, ?,?)");
			stmt.setString(1, adaptURL(page.getUrl()));
			stmt.setString(2, page.getHeadersString());
			stmt.setBytes(3, bodyBytes);
			stmt.setString(4, page.getLastModified());
			return (stmt.executeUpdate() > 0);
		} 
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			return false;
		}
		finally {
			if (rs != null)
				try{rs.close();}catch(SQLException e){}
			if (st != null)
				try{st.close();}catch(SQLException e){}
			if (stmt != null)
				try{stmt.close();}catch(SQLException e){}
			close();
		}
	}

	/**
	 * Destroys a cache table.  Given in assignment.
	 */
	@Override
	public void destroyTable() {
		Statement stmt = null;

		try {
			open();
			stmt = con.createStatement();

			stmt.execute("DROP TABLE " + tblName);
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
		}
		finally {
			if (stmt != null)
				try{stmt.close();}catch(SQLException e){}
			close();
		}
	}

	/**
	 * Builds a cache table.  Given in assignment.
	 */
	@Override
	public void buildTable() {
		Statement stmt = null;

		try {
			open();
			stmt = con.createStatement();

			stmt.execute("CREATE TABLE IF NOT EXISTS " + tblName + " ("
					+ "url VARCHAR(255) PRIMARY KEY, " + "headers TEXT,"
					+ "body BLOB," + "lastmodified VARCHAR(255))");
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
		} finally {
			if (stmt != null)
				try{stmt.close();}catch(SQLException e){}
			close();
		}
		
	}
}
