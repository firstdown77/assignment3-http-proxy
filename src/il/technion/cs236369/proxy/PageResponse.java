package il.technion.cs236369.proxy;

import org.apache.http.Header;

public class PageResponse {
	String url;
	Header[] headers;
	boolean noCache, noStore = false;
	byte[] body;
	String lastModified;
	
	public PageResponse(String url, Header[] headers, boolean noCache, boolean noStore, 
			byte[] body, String lastModified)
	{
		this.url = url;
		this.body = body;
		this.noCache = noCache;
		this.noStore = noStore;
		this.lastModified = lastModified;
		this.headers = headers;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public boolean isNoCache() {
		return noCache;
	}
	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}
	
	public boolean isNoStore() {
		return noStore;
	}
	public void setNoStore(boolean noStore) {
		this.noStore = noStore;
	}
	
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	public Header[] getHeaders() {
		return headers;
	}
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
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
}
