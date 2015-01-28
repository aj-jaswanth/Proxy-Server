package in.rgukt.proxyserver;

import in.rgukt.proxyserver.core.ServerThread;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main program to start the proxy server. It waits until a client is connected.
 * Then starts a thread to handle the client.
 * 
 * @author Venkata Jaswanth
 *
 */
public class Main {
	public static void main(String[] args) throws Exception {
		ServerSocket clientServer = new ServerSocket(3128);
		while (true) {
			System.out.println("A");
			Socket socket = clientServer.accept();
			System.out.println("B");
			Thread serverThread = new Thread(new ServerThread(socket));
			serverThread.start();
			serverThread.join();
		}
	}
}