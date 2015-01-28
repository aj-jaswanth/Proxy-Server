package in.rgukt.proxyserver.modules;

import java.util.HashMap;

/**
 * CacheManager maintains cache of all objects irrespective of thier actual
 * type.
 * 
 * @author Venkata Jaswanth
 */
public class CacheManager {
	private final String LOG_DIRECTORY;
	private HashMap<String, String> hashTable = new HashMap<String, String>();

	public CacheManager(String logDirectory) {
		LOG_DIRECTORY = logDirectory;
	}

	public void addToCache() {

	}

	public String getFromCache() {
		return null;
	}

	public void refreshCache() {

	}
}