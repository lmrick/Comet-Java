package com.cometproject.api.config;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class Configuration extends Properties {
	
	private static final Logger log = Logger.getLogger(Configuration.class.getName());
	private static Configuration configuration;
	
	public Configuration(String file) {
		super();
		
		try {
			var stream = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			this.load(stream);
			stream.close();
		} catch (Exception e) {
			log.error("Failed to fetch the server configuration (%s)".formatted(file));
			System.exit(1);
		}
	}
	
	public void override(Map<String, String> config) {
		config.forEach((key, value) -> {
			if (this.containsKey(key)) {
				this.remove(key);
				this.put(key, value);
			} else {
				this.put(key, value);
			}
		});
	}
	
	public String get(String key) {
		return this.getProperty(key);
	}
	
	public String get(String key, String fallback) {
		return this.containsKey(key) ? this.get(key) : fallback;
	}
	
	public static Configuration currentConfig() {
		return configuration;
	}
	
	public static void setConfiguration(Configuration conf) {
		configuration = conf;
	}
	
}
