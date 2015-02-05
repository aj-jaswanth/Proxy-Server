package in.rgukt.proxyserver.core;

import java.util.HashMap;

/**
 * HTTPRequest is used to store incoming HTTP requests from web clients like
 * browsers etc., This has all the methods to conveniently access individual
 * pieces of a HTTP request like HTTP method, resource, version, headers.
 * 
 * @author Venkata Jaswanth
 *
 */
public final class HTTPRequest {
	private String[] initialRequestLineArray;
	private String initialRequestLine;
	/**
	 * HashTable to store all headers and their values.
	 */
	private HashMap<String, String> headers = new HashMap<String, String>();
	/**
	 * Stores the complete HTTP request (just for speed)
	 */
	private StringBuilder completeHTTPRequest = new StringBuilder();
	private boolean firstHeader = true;
	byte[] body = new byte[1];

	public HTTPRequest() {
	}

	public final void setInitialRequestLine(String initialRequestLine) {
		assert (initialRequestLine != null);
		this.initialRequestLine = initialRequestLine;
		completeHTTPRequest.append(initialRequestLine);
		completeHTTPRequest.append('\n');
		this.initialRequestLineArray = initialRequestLine.split(" ");
	}

	public final void setHeader(String header) {
		header = header.replaceAll("[\\r\\n]", "");
		if (firstHeader) {
			setInitialRequestLine(header);
			firstHeader = false;
			return;
		}
		int pivot = header.indexOf(':');
		headers.put(header.substring(0, pivot),
				header.substring(pivot + 2, header.length()));
		completeHTTPRequest.append(header);
		completeHTTPRequest.append('\n');
	}

	public final String getInitialRequestLine() {
		return initialRequestLine;
	}

	public final String getMethod() {
		return initialRequestLineArray[0];
	}

	public final String getResource() {
		return initialRequestLineArray[1];
	}

	public final String getHTTPVersion() {
		return initialRequestLineArray[2];
	}

	public final String getHeader(final String key) {
		return headers.get(key);
	}

	public final boolean hasHeader(final String key) {
		return headers.containsKey(key);
	}

	public final String getCompleteRequest() {
		return completeHTTPRequest.toString();
	}

	/**
	 * Add arbitary strings to the complete request. This gets added to the
	 * completeHTTPRequest data structure. (for flexibility)
	 * 
	 * @param request
	 *            The string to be added
	 */
	public final void addToRequest(String request) {
		completeHTTPRequest.append(request);
	}
}