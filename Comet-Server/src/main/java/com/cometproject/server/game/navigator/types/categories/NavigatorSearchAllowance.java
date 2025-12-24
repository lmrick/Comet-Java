package com.cometproject.server.game.navigator.types.categories;

public enum NavigatorSearchAllowance {
	NOTHING, SHOW_MORE, GO_BACK;
	
	public static int getIntValue(NavigatorSearchAllowance allowance) {
		switch (allowance) {
			case SHOW_MORE -> {
				return 1;
			}
			case GO_BACK -> {
				return 2;
			}
			default -> {
				return 0;
			}
		}
	}
	
}
