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
	public static boolean shutdownServer;

	public static void main(String[] args) throws Exception {
		ServerSocket clientServer = new ServerSocket(3128);
		while (shutdownServer == false) {
			Socket socket = clientServer.accept();
			Thread serverThread = new Thread(new ServerThread(socket));
			serverThread.start();
			System.out.println(serverThread.getName());
			// serverThread.join();
		}
		clientServer.close();
	}
}