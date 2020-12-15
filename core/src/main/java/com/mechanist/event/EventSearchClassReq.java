package com.mechanist.event;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.google.protobuf.ProtocolStringList;
import com.mechanist.Msg;
import com.mechanist.msg.MsgCode;
import com.mechanist.msg.MsgSearchClassReq;
import com.mechanist.proto.MsgData;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.MsgSendUtil;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventSearchClassReq extends MsgSearchClassReq {
    private static Logger logger = LoggerFactory.getLogger(MsgSearchClassReq.class);
    @Override
    public Msg clone() {
        return new EventSearchClassReq();
    }

    @Override
    public void handleEvent() throws Exception {
        Instrumentation inst = ArthasBootstrap.getInstance().getInstrumentation();
        TunnelMessage.SearchClassResp.Builder builder = TunnelMessage.SearchClassResp.newBuilder();
        builder.setRequestId(searchClassReq.getRequestId());
        ProtocolStringList classPathList = searchClassReq.getClassPathList();
        Iterator<String> iterator = classPathList.iterator();
        while (iterator.hasNext()){
            String classPath = iterator.next();
            logger.info("search class:"+classPath);
            if(StringUtils.isBlank(classPath)){
                continue;
            }
            MsgData.SearchClassResult.Builder searchClassResultBuilder = MsgData.SearchClassResult.newBuilder();
            searchClassResultBuilder.setClassPath(classPath);

            List<Class<?>> matchedClasses = new ArrayList<Class<?>>(SearchUtils.searchClass(inst, classPath, false, null));
            for (Class<?> clazz : matchedClasses) {
                MsgData.ClazzInfo clazzInfo = ClassUtils.renderClazzInfo(clazz);
                searchClassResultBuilder.addClazzInfo(clazzInfo);
            }
            builder.addSearchClassResult(searchClassResultBuilder.build());
        }
        TunnelMessage.SearchClassResp searchClassResp = builder.build();
        MsgSendUtil.send((Integer) MsgCode.SearchClassResp[0], searchClassResp,channel);
    }
}
