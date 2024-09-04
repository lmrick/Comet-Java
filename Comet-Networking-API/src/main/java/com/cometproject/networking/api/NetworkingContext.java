package com.cometproject.networking.api;

public record NetworkingContext(INetworkingServerFactory serverFactory) {
	
	private static NetworkingContext currentContext;
	
	public static NetworkingContext getCurrentContext() {
		return currentContext;
	}
	
	public static void setCurrentContext(NetworkingContext networkingContext) {
		currentContext = networkingContext;
	}
	
}
