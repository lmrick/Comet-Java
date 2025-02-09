package com.cometproject.server.storage.cache;

import com.cometproject.api.config.Configuration;
import com.cometproject.api.utilities.Initializable;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.storage.cache.subscribers.GoToRoomSubscriber;
import com.cometproject.server.storage.cache.subscribers.ISubscriber;
import com.cometproject.server.storage.cache.subscribers.RefreshDataSubscriber;
import com.cometproject.server.tasks.CometThreadManager;
import com.cometproject.server.utilities.TimeSpan;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Arrays;

public class CacheManager extends CachableObject implements Initializable {
	
	private static CacheManager cacheManager;
	private final Logger log = Logger.getLogger(CacheManager.class.getName());
	private final String keyPrefix;
	private final String host;
	private final int port;
	private boolean enabled;
	private JedisPool jedis;
	
	private CacheManager() {
		this.enabled = Boolean.parseBoolean((String) Configuration.currentConfig().getOrDefault("comet.cache.enabled", "false"));
		this.keyPrefix = (String) Configuration.currentConfig().getOrDefault("comet.cache.prefix", "comet");
		this.host = (String) Configuration.currentConfig().getOrDefault("comet.cache.connection.host", "");
		this.port = Integer.parseInt((String) Configuration.currentConfig().getOrDefault("comet.cache.connection.port", ""));
	}
	
	public static CacheManager getInstance() {
		if (cacheManager == null) cacheManager = new CacheManager();
		return cacheManager;
	}
	
	@Override
	public void initialize() {
		if (!this.enabled) return;
		
		if (this.host.isEmpty()) {
			log.error("Invalid redis connection string");
			
			this.enabled = false;
			return;
		}
		
		if (!this.initializeConfig()) {
			log.error("Failed to load Redis cache configuration, disabling caching");
			this.enabled = false;
			return;
		}
		
		if (!this.initializeJedis()) {
			log.error("Failed to initialize Redis cluster, disabling caching");
			
			this.enabled = false;
			return;
		}
		
		this.doSubscriptions(new ISubscriber[] { new RefreshDataSubscriber(), new GoToRoomSubscriber() });
		
		log.info("Redis Caching is enabled");
	}
	
	private boolean initializeConfig() {
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("./config/cache.json"));
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.warn("Failed to close BufferedReader", e);
				}
			}
		}
	}
	
	private boolean initializeJedis() {
		try {
			final JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(100);
			poolConfig.setMaxWait(Duration.ofMillis(100));
			
			this.jedis = new JedisPool(poolConfig, this.host, this.port, 3000);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void doSubscriptions(ISubscriber[] subscribers) {
		Arrays.stream(subscribers).forEachOrdered(subscriber -> {
			subscriber.setJedis(this.jedis);
			CometThreadManager.getInstance().executeOnce(subscriber::subscribe);
			log.info(MessageFormat.format("Subscriber {0} initialized", subscriber.getClass().getSimpleName()));
		});
	}
	
	public void put(final String key, CachableObject object) {
		if (this.jedis == null) {
			return;
		}
		
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				final long startTime = System.currentTimeMillis();
				final String objectData = object.toString();
				
				jedis.set(this.getKey(key), objectData);
				
				log.info(MessageFormat.format("DataWrapper put to redis: {0} in {1}ms", object.getClass().getSimpleName(), new TimeSpan(startTime, System.currentTimeMillis()).toMilliseconds()));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error(MessageFormat.format("Error while setting object in Redis with key: {0}, type: {1}", key, object.getClass().getSimpleName()), e);
		}
	}
	
	public void publishString(final String key, final String value, boolean setter, String setterKey) {
		if (this.jedis == null) {
			return;
		}
		
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				final long startTime = System.currentTimeMillis();
				
				jedis.publish(this.getKey(key), value);
				
				if (setter && setterKey != null) jedis.set(this.getKey(setterKey), value);
				
				log.info(MessageFormat.format("DataWrapper published to redis channel: {0} in {1}ms", key, new TimeSpan(startTime, System.currentTimeMillis()).toMilliseconds()));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error("Error while setting string with key: " + key, e);
		}
	}
	
	public void putString(final String key, final String value) {
		if (this.jedis == null) {
			return;
		}
		
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				final long startTime = System.currentTimeMillis();
				
				jedis.set(this.getKey(key), value);
				
				log.info(MessageFormat.format("DataWrapper put to redis with key: {0} in {1}ms", key, new TimeSpan(startTime, System.currentTimeMillis()).toMilliseconds()));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error("Error while setting string with key: " + key, e);
		}
	}
	
	public String getString(String key) {
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				return jedis.get(this.getKey(key));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error(MessageFormat.format("Error while reading string from Redis with key: {0}", key), e);
		}
		
		return null;
	}
	
	public <T> T get(final Class<T> clazz, final String key) {
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				final String data = jedis.get(this.getKey(key));
				
				final T object = JsonUtil.getInstance().fromJson(data, clazz);
				
				if (object != null) {
					return object;
				}
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error(MessageFormat.format("Error while reading object from Redis with key: {0}, type: {1}", key, clazz.getSimpleName()), e);
		}
		
		return null;
	}
	
	public boolean exists(final String key) {
		try {
			try (final Jedis jedis = this.jedis.getResource()) {
				return jedis.exists(getKey(key));
			} catch (Exception e) {
				throw e;
			}
		} catch (Exception e) {
			log.error(MessageFormat.format("Error while reading EXISTS from redis, key: {0}", key), e);
		}
		
		return false;
	}
	
	private String getKey(final String key) {
		return "%s.%s".formatted(this.keyPrefix, key);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
}
