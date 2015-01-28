package in.rgukt.proxyserver.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HTTPUtils {
	public static String lineSeparatorUnix = "\n";
	public static String lineSeparatorWindows = "\r\n";

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