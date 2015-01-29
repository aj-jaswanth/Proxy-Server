package in.rgukt.proxyserver.core;

import java.io.IOException;
import java.io.InputStream;

public class HTTPRequestAutomata {
	private InputStream clientSocketByteReader;
	private int first;
	private StringBuilder requestLine;

	public HTTPRequestAutomata(InputStream inputStream) {
		clientSocketByteReader = inputStream;
		first = 0;
	}

	public String nextString() throws IOException {
		requestLine = new StringBuilder();
		while (true) {
			first = clientSocketByteReader.read();
			if (first == -1) {
				return "";
			}
			if (first == '\r') {
				first = clientSocketByteReader.read();
				return requestLine.toString();
			} else if (first == '\n') {
				return requestLine.toString();
			} else
				requestLine.append((char) first);
		}
	}
}
