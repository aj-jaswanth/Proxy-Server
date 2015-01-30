package in.rgukt.proxyserver.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * This does all the heavy duty work. Each HTTP transaction is managed by a
 * ServerThread. The thread is alive until it sees Connection: close header or
 * an explicit TCP connection tear down by the client or server.
 * 
 * @author Venkata Jaswanth
 *
 */
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

	/**
	 * Waits for the client to send HTTP request. It receives it and creates a
	 * HTTPRequest object to represent it.
	 */
	@SuppressWarnings("unused")
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

	private void readHTTPRequest2() {
		try {
			httpRequest = new HTTPRequest();
			HTTPRequestAutomata requestProcessor = new HTTPRequestAutomata(
					clientSocket.getInputStream());
			String header = null;
			while ((header = requestProcessor.nextString()).equals("") == false)
				httpRequest.setHeader(header);
			httpRequest.addToRequest("\n");
			if (httpRequest.getCompleteRequest().equals("\n"))
				closeConnectionImplicit = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the HTTPRequest to the intended server.
	 */
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

	/**
	 * Receives HTTP response from the server and creates a HTTPResponse object
	 * to represent it.
	 */
	private void readHTTPResponse() {
		if (closeConnectionImplicit)
			return;
		try {
			serverSocketByteReader = serverSocket.getInputStream();
			httpResponse = new HTTPResponse();
			int first = 0, second = 0;
			StringBuilder responseLine = new StringBuilder();
			boolean justReachedLineTerminator = false;
			/*
			 * This is a finite automata to process incoming response in bytes.
			 * It is needed because the line separator in the incoming HTTP
			 * response can be either '\n' or '\r\n'. This processes headers
			 * only.
			 */
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
					if (justReachedLineTerminator == true) {
						break;
					}
					justReachedLineTerminator = true;
					responseLine.append((char) first);
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
				} else if (first == '\r' && second == '\n') {
					if (justReachedLineTerminator == true) {
						break;
					}
					justReachedLineTerminator = true;
					httpResponse.setHeader(responseLine.toString());
					responseLine = new StringBuilder();
				} else if (second == '\r') {
					if (justReachedLineTerminator == true) {
						break;
					}
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
			// System.out.println(httpRequest.getCompleteRequest());
			int bodyLength = Integer.parseInt(httpResponse
					.getHeader("Content-Length"));
			byte[] body = new byte[bodyLength];
			serverSocketByteReader.read(body, 0, bodyLength);
			httpResponse.setBody(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the received HTTP response to the client.
	 */
	/*
	 * This sends the HTTP status and headers through buffered IO then sends
	 * body of the response using byte oriented IO.
	 */
	private void sendHTTPResponse() {
		if (closeConnectionImplicit)
			return;
		try {
			clientSocketWriter = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			clientSocketWriter.write(httpResponse.getCompletHTTPResponse());
			clientSocketWriter.write('\n');
			clientSocketWriter.flush();
			clientSocketByteWriter = clientSocket.getOutputStream();
			clientSocketByteWriter.write(httpResponse.getBody(), 0,
					Integer.parseInt(httpResponse.getHeader("Content-Length")));
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
		// System.out.println("Accepted TCP Connection!");
		while (closeConnectionImplicit == false && closeConnection() == false) {
			// System.out.println("HTTP Transaction");
			readHTTPRequest2();
			// if (!closeConnectionImplicit)
			// System.out.print(httpRequest.getCompleteRequest());
			sendHTTPRequest();
			readHTTPResponse();
			// if (!closeConnectionImplicit)
			// System.out.print(httpResponse.getCompletHTTPResponse());
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