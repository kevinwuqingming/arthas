
package com.alibaba.arthas.tunnel.server;

import com.alibaba.arthas.tunnel.server.app.AgentConnectPassListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author hengyunabc 2019-08-27
 *
 */
public class WebTunnelSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final static Logger logger = LoggerFactory.getLogger(WebTunnelSocketFrameHandler.class);

    private TunnelServer tunnelServer;

    public WebTunnelSocketFrameHandler(TunnelServer tunnelServer) {
        this.tunnelServer = tunnelServer;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HandshakeComplete) {
            HandshakeComplete handshake = (HandshakeComplete) evt;
            // http request uri
            String uri = handshake.requestUri();
            logger.info("websocket handshake complete, uri: {}", uri);

            MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
            String method = parameters.getFirst("method");

            if ("connectArthas".equals(method)) { // form browser
                connectArthas(ctx, parameters);
            }
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    private void connectArthas(ChannelHandlerContext tunnelSocketCtx, MultiValueMap<String, String> parameters)
            throws URISyntaxException {

        List<String> agentId = parameters.getOrDefault("id", Collections.emptyList());

        if (agentId.isEmpty()) {
            logger.error("arthas agent id can not be null, parameters: ", parameters);
            throw new IllegalArgumentException("arthas agent id can not be null");
        }

        List<String> password = parameters.getOrDefault("cp", Collections.emptyList());

        if(password.isEmpty()){
            throw new IllegalArgumentException("password can not be null.");
        }

        if(!AgentConnectPassListener.passExist(password.get(0))){
            throw new IllegalArgumentException("password invalid.");
        }

        logger.info("try to connect to arthas agent, id: " + agentId.get(0));

        Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId.get(0));

        if (findAgent.isPresent()) {
            ChannelHandlerContext agentCtx = findAgent.get().getChannelHandlerContext();

            String clientConnectionId = RandomStringUtils.random(20, true, true).toUpperCase();

            logger.info("random clientConnectionId: " + clientConnectionId);
            URI uri = new URI("response", null, "/",
                    "method=startTunnel" + "&id=" + agentId.get(0) + "&clientConnectionId=" + clientConnectionId, null);

            logger.info("startTunnel response: " + uri);

            ClientConnectionInfo clientConnectionInfo = new ClientConnectionInfo();
            SocketAddress remoteAddress = tunnelSocketCtx.channel().remoteAddress();
            if (remoteAddress instanceof InetSocketAddress) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
                clientConnectionInfo.setHost(inetSocketAddress.getHostString());
                clientConnectionInfo.setPort(inetSocketAddress.getPort());
            }
            clientConnectionInfo.setChannelHandlerContext(tunnelSocketCtx);

            // when the agent open tunnel success, will set result into the promise
            Promise<Channel> promise = agentCtx.executor().newPromise();
            promise.addListener(new FutureListener<Channel>() {
                @Override
                public void operationComplete(final Future<Channel> future) throws Exception {
                    final Channel outboundChannel = future.getNow();
                    if (future.isSuccess()) {
                        tunnelSocketCtx.pipeline().remove(WebTunnelSocketFrameHandler.this);

                        // outboundChannel is form arthas agent
                        outboundChannel.pipeline().removeLast();

                        outboundChannel.pipeline().addLast(new RelayHandler(tunnelSocketCtx.channel()));
                        tunnelSocketCtx.pipeline().addLast(new RelayHandler(outboundChannel));
                    } else {
                        logger.error("wait for agent connect error. agentId: {}, clientConnectionId: {}", agentId,
                                clientConnectionId);
                        ChannelUtils.closeOnFlush(agentCtx.channel());
                    }
                }
            });

            clientConnectionInfo.setPromise(promise);
            this.tunnelServer.addClientConnectionInfo(clientConnectionId, clientConnectionInfo);
            tunnelSocketCtx.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    tunnelServer.removeClientConnectionInfo(clientConnectionId);
                }
            });

            agentCtx.channel().writeAndFlush(new TextWebSocketFrame(uri.toString()));

            logger.info("browser connect waitting for arthas agent open tunnel");
            boolean watiResult = promise.awaitUninterruptibly(20, TimeUnit.SECONDS);
            if (watiResult) {
                logger.info(
                        "browser connect wait for arthas agent open tunnel success, agentId: {}, clientConnectionId: {}",
                        agentId, clientConnectionId);
            } else {
                logger.error(
                        "browser connect wait for arthas agent open tunnel timeout, agentId: {}, clientConnectionId: {}",
                        agentId, clientConnectionId);
                tunnelSocketCtx.close();
            }
        } else {
            tunnelSocketCtx.channel().writeAndFlush(new CloseWebSocketFrame(2000, "Can not find arthas agent by id: "+ agentId));
            logger.error("Can not find arthas agent by id: {}", agentId);
            throw new IllegalArgumentException("Can not find arthas agent by id: " + agentId);
        }
    }

}
