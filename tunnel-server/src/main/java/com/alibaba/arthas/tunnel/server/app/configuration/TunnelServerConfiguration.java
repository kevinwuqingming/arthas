package com.alibaba.arthas.tunnel.server.app.configuration;

import com.alibaba.arthas.tunnel.server.WebTunnelServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;

/**
 * 
 * @author hengyunabc 2020-10-27
 *
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class TunnelServerConfiguration {

    @Autowired
    ArthasProperties arthasProperties;

    private TunnelServer tunnelServer;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public TunnelServer tunnelServer(@Autowired(required = false) TunnelClusterStore tunnelClusterStore) {
        tunnelServer = new TunnelServer();

        tunnelServer.setHost(arthasProperties.getServer().getHost());
        tunnelServer.setPort(arthasProperties.getServer().getPort());
        tunnelServer.setSsl(arthasProperties.getServer().isSsl());
        tunnelServer.setPath(arthasProperties.getServer().getPath());
        tunnelServer.setClientConnectHost(arthasProperties.getServer().getClientConnectHost());
        if (tunnelClusterStore != null) {
            tunnelServer.setTunnelClusterStore(tunnelClusterStore);
        }
        return tunnelServer;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WebTunnelServer webTunnelServer() {
        WebTunnelServer webTunnelServer = new WebTunnelServer();

        webTunnelServer.setHost(arthasProperties.getServer().getHost());
        int webPort = arthasProperties.getServer().getWebPort();
        if(webPort == 0){
            webPort = arthasProperties.getServer().getPort() + 1;
        }
        webTunnelServer.setPort(webPort);
        webTunnelServer.setSsl(arthasProperties.getServer().isSsl());
        webTunnelServer.setTunnelServer(tunnelServer);
        return webTunnelServer;
    }
}
