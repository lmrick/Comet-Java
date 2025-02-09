package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum IntervalType {
	ONCE(0), DAYS(1), HOURS(2), MINUTES(3);
	
	private static final Map<Integer, IntervalType> map = new HashMap<>();
	private final int type;
	
	IntervalType(int type) {
		this.type = type;
	}
	
	static {
		Arrays.stream(IntervalType.values()).forEachOrdered(intervalType -> map.put(intervalType.type, intervalType));
	}
	
	public static IntervalType getIntervalByInt(Integer integer) {
		return map.get(integer);
	}
	
	public int getInteger() {
		return this.type;
	}
}
