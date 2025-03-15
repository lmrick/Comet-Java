package com.cometproject.server.locale;

import com.cometproject.server.storage.queries.config.LocaleDao;
import org.apache.log4j.Logger;
import java.text.MessageFormat;
import java.util.Map;

public class Locale {
	private static final Logger log = Logger.getLogger(Locale.class.getName());
	private static Map<String, String> locale;
	
	public static void initialize() {
		reload();
	}
	
	public static void reload() {
		if (locale != null) {
			locale.clear();
		}
		
		locale = LocaleDao.getAll();
		log.info(MessageFormat.format("Loaded {0} locale strings", locale.size()));
	}
	
	public static String get(String key) {
		return locale.getOrDefault(key, key);
	}
	
	public static String getOrDefault(String key, String defaultValue) {
		return locale.getOrDefault(key, defaultValue);
		
	}
	
	public static Map<String, String> getAll() {
		return locale;
	}
	
}
