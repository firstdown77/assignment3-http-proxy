package il.technion.cs236369.proxy;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

/**
 * Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 * @author Raphael Astrow (922130174 - rastrow@andrew.cmu.edu) and David Sainz (927902023 - dsainz@cs.technion.ac.il)
 *
 */
public class PageResponse {
	byte[] bodyBytes = null;
	
	/**
	 * Response body getter.
	 * @return The byte array of the response.
	 */
	public byte[] getBodyBytes() {
		if ((body != null) && (bodyBytes == null))
			bodyBytes = getResponseBody(body);
		return bodyBytes;
	}

	/**
	 * Response body setter.
	 * @param bodyBytes The response body's byte array to set.
	 */
	public void setBodyBytes(byte[] bodyBytes) {
		this.bodyBytes = bodyBytes;
	}

	/**
	 * 
	 */
	int status;
	public int getStatus() {
		return status;
	}

	/**
	 * Response status setter.
	 * @param status Response status setter.
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	String url;
	Header[] headers;
	boolean noCache, noStore, transferEncoding = false;
	public boolean isTransferEncoding() {
		return transferEncoding;
	}

	/**
	 * Transfer encoding setter.
	 * @param transferEncoding Transfer encoding setter.
	 */
	public void setTransferEncoding(boolean transferEncoding) {
		this.transferEncoding = transferEncoding;
	}

	HttpEntity body;
	String lastModified = "";
	
	/**
	 * Constructor.
	 * 
	 * @param url Url of the response.
	 * @param status Status of the response.
	 * @param headers Headers of the response.
	 * @param noCache Whether or not the headers contain "no cache".
	 * @param noStore Whether or not the headers contains "no store"
	 * @param body The body of the response.
	 * @param lastModified The lastmodified header of the response.
	 */
	public PageResponse(String url, int status, Header[] headers, boolean noCache, boolean noStore, 
			HttpEntity body, String lastModified)
	{
		this.status = status;
		this.url = url;
		this.body = body;
		this.noCache = noCache;
		this.noStore = noStore;
		this.lastModified = lastModified;
		this.headers = headers;
	}
	
	/**
	 * Alternate page response constructor.  This one with a byte array entity of the response body.
	 * @param url Url of the response.
	 * @param status Status of the response.
	 * @param headers Headers of the response.
	 * @param noCache Whether or not the headers contain "no cache".
	 * @param noStore Whether or not the headers contains "no store"
	 * @param body The body of the response.
	 * @param lastModified The lastmodified header of the response.
	 */
	public PageResponse(String url, int status, Header[] headers, boolean noCache, boolean noStore, 
			byte[] body, String lastModified)
	{
		this.status = status;
		this.url = url;
		ByteArrayEntity bae = new ByteArrayEntity(body);
		this.body = bae;
		this.noCache = noCache;
		this.noStore = noStore;
		this.lastModified = lastModified;
		this.headers = headers;
	}
	
	/**
	 * URL getter.
	 * @return The response URL to get.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Response URL setter.
	 * @param url The response url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Determines if the response is no cache.
	 * @return Yes or no - the response is no cache.
	 */
	public boolean isNoCache() {
		return noCache;
	}
	
	/**
	 * No cache setter.
	 * @param noCache Whether or not the response is no-cache.
	 */
	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}
	
	/**
	 * Determines if the response is no store.
	 * @return Yes or no - response is no store.
	 */
	public boolean isNoStore() {
		return noStore;
	}
	
	/**
	 * No-store setter.
	 * @param noStore Whether or not the response is no store.
	 */
	public void setNoStore(boolean noStore) {
		this.noStore = noStore;
	}
	
	/**
	 * Response body getter.
	 * @return The HttpEntity format of the response body.
	 */
	public HttpEntity getBody() {
		return body;
	}
	
	/**
	 * Response body setter.
	 * @param body The response body to set.
	 */
	public void setBody(HttpEntity body) {
		this.body = body;
	}
	
	/**
	 * Get lastmodified header.
	 * @return The last modified header.
	 */
	public String getLastModified() {
		return lastModified;
	}
	
	/**
	 * Last modified setter.
	 * @param lastModified Sets the lastmodified header.
	 */
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	/**
	 * Response headers getter.
	 * @return The response headers.
	 */
	public Header[] getHeaders() {
		return headers;
	}
	
	/**
	 * Set the response headers
	 * @param headers The headers to set.
	 */
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
	
	/**
	 * Removes a header from the header list.
	 * @param type The type of header to remove.
	 */
	public void removeHeader(String type)
	{
		ArrayList<Header> newHeaders = new ArrayList<Header>();
		for (Header h: headers)
		{
			if (!h.getName().equals(type))
				newHeaders.add(h);
		}
		Header[] value = new Header[newHeaders.size()];
		headers = newHeaders.toArray(value);
	}
	
	/**
	 * Converts the headers array into a string.
	 * @return A string of all the headers.
	 */
	public String getHeadersString()
	{
		StringBuilder sb = new StringBuilder();
		if (headers.length > 0)
			sb.append(headers[0]);
		if (headers.length > 1)
			for (int i = 1; i < headers.length; i++)
			{
				sb.append("\r\n");
				sb.append(headers[i]);
				
			}
		return sb.toString();
	}
	
	/**
	 * Response body getter.
	 * @param entity The http entity.
	 * @return A byte array containing the complete response body.
	 */
	private byte[] getResponseBody(HttpEntity entity)
	{
		if (entity != null)
		{
			if (entity.getContentLength() > 65535)
				return new byte[0];
			
			try
			{
				return EntityUtils.toByteArray(entity);
			}
			catch (IOException ioe){return new byte[0];}
		}
		else return new byte[0];
	}
}
