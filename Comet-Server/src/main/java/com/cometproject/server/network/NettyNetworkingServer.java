package com.cometproject.server.network;

import com.cometproject.networking.api.INetworkingServer;
import com.cometproject.networking.api.config.NetworkingServerConfig;
import com.cometproject.networking.api.sessions.INetSessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class NettyNetworkingServer implements INetworkingServer {
	
	private static final Logger log = LogManager.getLogger(NettyNetworkingServer.class);
	
	private final NetworkingServerConfig serverConfig;
	private final INetSessionFactory sessionFactory;
	private final ServerBootstrap serverBootstrap;
	
	public NettyNetworkingServer(NetworkingServerConfig serverConfig, INetSessionFactory sessionFactory, ServerBootstrap serverBootstrap) {
		this.serverConfig = serverConfig;
		this.sessionFactory = sessionFactory;
		this.serverBootstrap = serverBootstrap;
	}
	
	@Override
	public void start() {
		for (short port : this.serverConfig.ports()) {
			try {
				this.serverBootstrap.bind(new InetSocketAddress(this.serverConfig.host(), port)).addListener(objectFuture -> {
					if (!objectFuture.isSuccess()) {
						log.error("Failed to start sockets on {}, port: {}", this.serverConfig.host(), port);
					}
				});
				
				log.info("Comet Server listening on port: {}", port);
			} catch (Exception e) {
				log.error("Failed to start sockets on {}, port: {}", this.serverConfig.host(), port, e);
			}
		}
	}
	
	@Override
	public NetworkingServerConfig getServerConfig() {
		return serverConfig;
	}
	
	@Override
	public INetSessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
}
