package in.rgukt.proxyserver.core;

import java.util.HashMap;

public final class HTTPResponse {
	private String initialResponseLine;
	private String[] initialResponseLineArray = new String[3];
	private HashMap<String, String> headers = new HashMap<String, String>();
	private StringBuilder completeHTTPResponse = new StringBuilder();
	private byte[] body;
	private boolean firstHeader = true;

	public HTTPResponse() {
	}

	public void setInitialResponseLine(String line) {
		int pivot = line.indexOf(' '), previousPivot;
		initialResponseLineArray[0] = line.substring(0, pivot);
		previousPivot = pivot;
		pivot = line.indexOf(' ', previousPivot + 1);
		initialResponseLineArray[1] = line.substring(previousPivot + 1, pivot);
		initialResponseLineArray[2] = line.substring(pivot + 1, line.length());
	}

	public void addToResponse(String response) {
		completeHTTPResponse.append(response);
	}

	public void setHeader(String header) {
		completeHTTPResponse.append(header);
		if (firstHeader) {
			setInitialResponseLine(header);
			firstHeader = false;
			return;
		}
		int pivot = header.indexOf(':');
		headers.put(header.substring(0, pivot),
				header.substring(pivot + 2, header.length()));
	}

	public String getCompletHTTPResponse() {
		return completeHTTPResponse.toString();
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public byte[] getBody() {
		return body;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getHTTPVersion() {
		return initialResponseLineArray[0];
	}

	public int getStatusCode() {
		return Integer.parseInt(initialResponseLineArray[1]);
	}

	public String getStatusCodeHumanReadableForm() {
		return initialResponseLineArray[2];
	}
}