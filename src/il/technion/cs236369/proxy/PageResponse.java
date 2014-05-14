package il.technion.cs236369.proxy;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

public class PageResponse {
	byte[] bodyBytes = null;
	
	public byte[] getBodyBytes() {
		if ((body != null) && (bodyBytes == null))
			bodyBytes = getResponseBody(body);
		return bodyBytes;
	}

	public void setBodyBytes(byte[] bodyBytes) {
		this.bodyBytes = bodyBytes;
	}

	int status;
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	String url;
	Header[] headers;
	boolean noCache, noStore, transferEncoding = false;
	public boolean isTransferEncoding() {
		return transferEncoding;
	}

	public void setTransferEncoding(boolean transferEncoding) {
		this.transferEncoding = transferEncoding;
	}

	HttpEntity body;
	String lastModified = "";
	
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
	
	public HttpEntity getBody() {
		return body;
	}
	public void setBody(HttpEntity body) {
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
			
			/*if (entity.getContentLength() <= 0)
				return null;
			byte[] body = new byte[(int)entity.getContentLength()];
			InputStream stream = null;
			try
			{
				stream = entity.getContent();
				int totalRead = 0;
				while (totalRead < body.length)
				{
					int read = stream.read(body, totalRead, 1024*8);
					if (read <= 0)
						break;
					else
						totalRead += read;
				}
				if (totalRead == body.length)
					return body;
				else
					return null;
			}
			catch (IOException ioe)
			{
				return null;
			}
			finally
			{
				try{
				if (stream != null)
					stream.close();
				}catch (Exception e){}
			}*/
		}
		else return new byte[0];
	}
}
