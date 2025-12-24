package com.cometproject.networking.api.config;

import java.util.Set;

public record NetworkingServerConfig(String host, Set<Short> ports, boolean shouldEncrypt) {
	
	public NetworkingServerConfig(String host, Set<Short> ports) {
		this(host, ports, true);
	}
	
	
}
