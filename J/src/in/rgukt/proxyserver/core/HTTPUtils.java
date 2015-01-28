package in.rgukt.proxyserver.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Useful utilities for HTTP protocol.
 * 
 * @author Venkata Jaswanth
 *
 */
public class HTTPUtils {
	public static String lineSeparatorUnix = "\n";
	public static String lineSeparatorWindows = "\r\n";

	/**
	 * Get socket address from Host header.
	 * 
	 * @param hostHeader
	 *            e.g., Host: 127.0.0.1
	 * @return SocketAddress : responsible for resolving the host name.
	 */
	/*
	 * Host names can be in two forms 1. localhost 2. localhost:1234. If the
	 * default port 80 of HTTP is used then it omits port number else it
	 * includes it.
	 */
	public static SocketAddress getSocketAddress(String hostHeader) {
		int pivot = hostHeader.indexOf(':');
		if (pivot == -1) {
			return new InetSocketAddress(hostHeader, 80);
		}
		return new InetSocketAddress(hostHeader.substring(0, pivot),
				Integer.parseInt(hostHeader.substring(pivot + 1,
						hostHeader.length())));
	}
}