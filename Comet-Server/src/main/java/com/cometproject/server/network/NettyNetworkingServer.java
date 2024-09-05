package com.cometproject.server.network;

import com.cometproject.networking.api.INetworkingServer;
import com.cometproject.networking.api.config.NetworkingServerConfig;
import com.cometproject.networking.api.sessions.INetSessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.text.MessageFormat;

public class NettyNetworkingServer implements INetworkingServer {
    private static final Logger log = Logger.getLogger(NettyNetworkingServer.class);

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
        for (short port : this.serverConfig.getPorts()) {
            try {
                this.serverBootstrap.bind(new InetSocketAddress(this.serverConfig.getHost(), port)).addListener(objectFuture -> {
                    if (!objectFuture.isSuccess()) {
                        log.error(MessageFormat.format("Failed to start sockets on {0}, port: {1}", this.serverConfig.getHost(), port));
                    }
                });

                log.info(MessageFormat.format("Comet Server listening on port: {0}", port));
            } catch (Exception e) {
                log.error(MessageFormat.format("Failed to start sockets on {0}, port: {1}", this.serverConfig.getHost(), port), e);
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
