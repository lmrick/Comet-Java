package com.cometproject.server.utilities.comporators;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {
	
	private final Map<String, Integer> base;
	
	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}
	
	@Override
	public int compare(String a, String b) {
		return base.get(a) >= base.get(b) ? 1 : -1;
	}
	
}
