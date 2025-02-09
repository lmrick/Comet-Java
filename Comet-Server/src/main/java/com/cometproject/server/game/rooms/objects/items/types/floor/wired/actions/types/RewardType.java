package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum RewardType {
	CREDITS("credits"), ACTIVITY_POINTS("duckets"), VIP_POINTS("diamonds"), SEASONAL_POINTS("seasonal"), GO_TO_ROOM("goto"), ALERT("alert");
	
	private static final Map<String, RewardType> map = new HashMap<>();
	private final String currency;
	
	RewardType(String currency) {
		this.currency = currency;
	}
	
	static {
		Arrays.stream(RewardType.values()).forEachOrdered(rewardType -> map.put(rewardType.currency, rewardType));
	}
	
	public static RewardType getCurrencyTypeByKey(String str) {
		return map.get(str);
	}
	
	public String getCurrency() {
		return this.currency;
	}
}
