package in.rgukt.proxyserver.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
	private Socket clientSocket;
	private Socket serverSocket;
	private BufferedReader clientSocketReader;
	private BufferedWriter clientSocketWriter;
	private BufferedWriter serverSocketWriter;
	private InputStream serverSocketByteReader;
	private OutputStream clientSocketByteWriter;
	private HTTPRequest httpRequest;
	private HTTPResponse httpResponse;
	private boolean closeConnectionImplicit;

	public ServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	private void readHTTPRequest() {
		try {
			clientSocketReader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			httpRequest = new HTTPRequest();
			String initialRequestLine = clientSocketReader.readLine();
			if (initialRequestLine == null) {
				closeConnectionImplicit = true;
				return;
			}
			httpRequest.setInitialRequestLine(initialRequestLine);
			String header = null;
			while ((header = clientSocketReader.readLine()).equals("") == false) {
				httpRequest.setHeader(header);
			}
			httpRequest.addToRequest("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendHTTPRequest() {
		if (closeConnectionImplicit)
			return;
		serverSocket = new Socket();
		try {
			serverSocket.connect(HTTPUtils.getSocketAddress(httpRequest
					.getHeader("Host")));
			serverSocketWriter = new BufferedWriter(new OutputStreamWriter(
					serverSocket.getOutputStream()));
			serverSocketWriter.write(httpRequest.getCompleteRequest());
			serverSocketWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readHTTPResponse() {
		if (closeConnectionImplicit)
			return;
		try {
			serverSocketByteReader = serverSocket.getInputStream();
			httpResponse = new HTTPResponse();
			int first = 0, second = 0;
			StringBuilder responseLine = new StringBuilder();
			boolean justReachedLineTerminator = false;
			while (true) {
				first = serverSocketByteReader.read();
				second = serverSocketByteReader.read();
				if (first == '\n' && second != '\n') {
					if (justReachedLineTerminator == true) {
						httpResponse.addToResponse("\n");
						break;
					}
					justReachedLineTerminator = true;
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
					responseLine.append((char) second);
				} else if ((first != '\r' && first != '\n') && second == '\n') {
					if (justReachedLineTerminator == true)
						break;
					justReachedLineTerminator = true;
					responseLine.append((char) first);
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
				} else if (first == '\r' && second == '\n') {
					if (justReachedLineTerminator == true)
						break;
					justReachedLineTerminator = true;
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
				} else if (second == '\r') {
					if (justReachedLineTerminator == true)
						break;
					justReachedLineTerminator = true;
					responseLine.append((char) first);
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
					first = serverSocketByteReader.read();
					assert (first == '\n') : "\\n is missing in \\r\\n";
				} else {
					justReachedLineTerminator = false;
					responseLine.append((char) first);
					responseLine.append((char) second);
				}
				if (justReachedLineTerminator)
					httpResponse.addToResponse("\n");
			}
			int bodyLength = Integer.parseInt(httpResponse
					.getHeader("Content-Length"));
			byte[] body = new byte[bodyLength];
			serverSocketByteReader.read(body, 0, bodyLength);
			httpResponse.setBody(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendHTTPResponse() {
		if (closeConnectionImplicit)
			return;
		try {
			clientSocketWriter = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			clientSocketWriter.write(httpResponse.getCompletHTTPResponse());
			clientSocketWriter.flush();
			clientSocketByteWriter = clientSocket.getOutputStream();
			clientSocketByteWriter.write(httpResponse.getBody(), 0,
					Integer.parseInt(httpResponse.getHeader("Content-Length")));
			clientSocketByteWriter.write('\r');
			clientSocketByteWriter.write('\n');
			clientSocketByteWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean closeConnection() {
		if (httpRequest == null)
			return false;
		if (httpRequest.hasHeader("Connection") == false)
			return true;
		else if (httpRequest.getHeader("Connection").equals("close"))
			return true;
		return false;
	}

	@Override
	public void run() {
		System.out.println("Accepted TCP Connection!");
		while (closeConnectionImplicit == false && closeConnection() == false) {
			System.out.println("HTTP Transaction");
			readHTTPRequest();
			if (!closeConnectionImplicit)
				System.out.print(httpRequest.getCompleteRequest());
			sendHTTPRequest();
			readHTTPResponse();
			if (!closeConnectionImplicit)
				System.out.print(httpResponse.getCompletHTTPResponse());
			sendHTTPResponse();
		}
		try {
			clientSocket.close();
			serverSocket.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}