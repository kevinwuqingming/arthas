package com.mechanist;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class MsgHandler implements BinaryWebSocketHandler<BinaryWebSocketFrame, ChannelHandlerContext>{
    private static Logger logger = LoggerFactory.getLogger(MsgHandler.class);
    @Override
    public void handle(BinaryWebSocketFrame frame, ChannelHandlerContext context) {
        ByteBuf byteBuf = frame.content();
        int msgId = byteBuf.readInt();
        logger.info("receive msg ,msg id = " + msgId);
        Msg msg = MsgFactory.create(msgId);
        if (msg == null) {
            logger.error("MsgHandler", "Could not find message:" + msgId);
            return;
        }
        try {
            msg.read(byteBuf);
        } catch (Exception e) {
            logger.error("MsgHandler", "msg.read exception, msgid = " + msg.msgid, e);
        }

        msg.channel = context.channel();
        EventReceiverThread.getInstance().pushEvent(msg);
    }
}
