package il.technion.cs236369.proxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Cache implements ICache {

	private final String dbURL;
	private final String dbName;
	private final String tblName;
	private final String dbUsername;
	private final String dbPassword;
	private final String dbDriver;

	@Inject
	Cache(@Named("httproxy.db.url") String dbURL,
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
		loadDriver();
	}

	public void loadDriver() throws ClassNotFoundException {
		Class.forName(dbDriver);
	}

	private Connection openConnection() throws SQLException {
		return DriverManager.getConnection(dbURL + dbName, dbUsername,
				dbPassword);
	}

	@Override
	public void buildTable() throws SQLException {
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = openConnection();
			stmt = conn.createStatement();

			stmt.execute("CREATE TABLE IF NOT EXISTS " + tblName + " ("
					+ "url VARCHAR(255) PRIMARY KEY, " + "headers TEXT,"
					+ "body BLOB," + "lastmodified VARCHAR(255))");
		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}

	@Override
	public void destroyTable() throws SQLException {
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = openConnection();
			stmt = conn.createStatement();

			stmt.execute("DROP TABLE " + tblName);
		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}

	public void insert(String url, HttpResponse res) throws SQLException,
			IOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			String lastmodified = null;
			StringBuilder headers = new StringBuilder();
			for (Header h : res.getAllHeaders()) {
				if (h.getName().contentEquals("lastmodified")) {
					lastmodified = h.getValue();
				}
				headers.append(h.getName() + ": " + h.getValue() + "\r\n");
			}

			byte[] body = EntityUtils.toByteArray(res.getEntity());
			res.setEntity(new ByteArrayEntity(body));

			conn = openConnection();
			stmt = conn.prepareStatement("INSERT INTO " + tblName
					+ " VALUES(?, ?, ?,?)");
			stmt.setString(1, url);
			stmt.setString(2, headers.toString());
			stmt.setBytes(3, body);
			stmt.setString(4, lastmodified);
			stmt.executeUpdate();

		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}

	public void remove(String url) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = openConnection();
			stmt = conn.prepareStatement("DELETE FROM " + tblName
					+ " WHERE url = ?");
			stmt.setString(1, url);
			stmt.executeUpdate();

		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}

	public HttpResponse search(String url) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = openConnection();
			stmt = conn.prepareStatement("SELECT * FROM " + tblName
					+ " WHERE url = ?");
			stmt.setString(1, url);
			rs = stmt.executeQuery();
			if (!rs.next())
				return null;

			HttpResponse res = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					HttpStatus.SC_OK, "OK");
			String headers = rs.getString("headers");
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(headers);
			scanner.useDelimiter("\r\n");
			while (scanner.hasNext()) {
				String header = scanner.next();
				if (header.isEmpty())
					break;
				String name = header.substring(0, header.indexOf(":")).trim();
				String value = header.substring(name.length() + 1).trim();
				res.addHeader(name, value);
			}

			byte[] body = rs.getBytes("body");
			res.setEntity(new ByteArrayEntity(body));

			return res;

		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
	}
}
