package in.rgukt.proxyserver.core;

import java.util.HashMap;

public final class HTTPRequest {
	private String[] initialRequestLineArray;
	private String initialRequestLine;
	private HashMap<String, String> headers = new HashMap<String, String>();
	private StringBuilder completeHTTPRequest = new StringBuilder();

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

	public final void addToRequest(String request) {
		completeHTTPRequest.append(request);
	}
}