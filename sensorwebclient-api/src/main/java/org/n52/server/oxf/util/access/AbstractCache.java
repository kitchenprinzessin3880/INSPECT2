package org.n52.server.oxf.util.access;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class AbstractCache<T> {
	private static final Logger LOG = Logger.getLogger(AbstractCache.class);

	protected class CacheEntry {
		public long timestamp;
		public Object cachedObject;

		public CacheEntry(long timestamp, Object cachedObject) {
			this.timestamp = timestamp;
			this.cachedObject = cachedObject;
		}

	}

	private Map<T, CacheEntry> cache;
	// default time to live is one hour
	private long maxTimeToLive = 3600000;
	// sleep 5 minutes before check the available memory again and if less then
	// clear cache
	private long monitorSleepInterval = 300000;

	protected AbstractCache() {
		this.cache = new Hashtable<T, AbstractCache<T>.CacheEntry>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (this) {
						if (!isEnoughMemoryAvailable()) {
							// check if time to live of each entry is expired
							cleanCache();
							if (!isEnoughMemoryAvailable()) {
								// do not check time to live. clear cache
								// entirely
								cleanCache(false);
							}
						}
						try {
							wait(monitorSleepInterval);
						} catch (InterruptedException e) {
							LOG.error(this.getClass().getName() + "-cache monitoring throws an exception:\n", e);
						}
					}
				}
			}
		}).start();
	}

	public synchronized void put(T uniqueCacheId, Object objectToCache) {
		LOG.debug("putting new object to " + this.getClass().getName() + "-cache: " + uniqueCacheId);
		this.cache.put(uniqueCacheId, new CacheEntry(new Date().getTime(), objectToCache));
	}

	public synchronized Object getCachedObject(T uniqueCacheId) {
		CacheEntry result = this.cache.get(uniqueCacheId);
		if (result != null && this.isTtlExpired(result)) {
			return result.cachedObject;
		}
		return null;
	}

	public void setMaxTimeToLive(long ms) {
		this.maxTimeToLive = ms;
	}

	public synchronized void cleanCacheFromOldObjects() {
		this.cleanCache();
	}

	public synchronized void cleanEntireCache() {
		this.cleanCache(false);
	}

	/**
	 * check available memory
	 * 
	 * @return true if more then 2/3 of total memory is available
	 */
	private boolean isEnoughMemoryAvailable() {
		Runtime runTime = Runtime.getRuntime();
		long total = runTime.totalMemory();
		long usedMem = total - runTime.freeMemory();
		LOG.info(new StringBuffer("used/total memory: ").append(usedMem).append("/").append(total).toString());
		return usedMem < total * 0.66;
	}

	/**
	 * clean the cache from old objects
	 */
	private void cleanCache() {
		this.cleanCache(true);
	}

	private boolean isTtlExpired(CacheEntry cacheEntry) {
		return new Date().getTime() < cacheEntry.timestamp + this.maxTimeToLive;
	}

	/**
	 * clean cache
	 * 
	 * @param checkTtl
	 *            if true remove only ttl expired objects else remove all
	 *            objects from cache
	 */
	private void cleanCache(boolean checkTtl) {
		LOG.info("cleaning " + this.getClass().getName() + "-cache ...");
		List<Object> keysToRemove = new ArrayList<Object>();
		for (Object key : this.cache.keySet()) {
			CacheEntry entry = this.cache.get(key);
			if (!checkTtl || this.isTtlExpired(entry)) {
				keysToRemove.add(key);
			}
		}
		LOG.info(this.getClass().getName() + "-cache successfully cleaned.");
		for (Object k : keysToRemove) {
			this.cache.remove(k);
		}
	}

}
