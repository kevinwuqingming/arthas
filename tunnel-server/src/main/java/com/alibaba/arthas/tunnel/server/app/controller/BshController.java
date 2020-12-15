package com.alibaba.arthas.tunnel.server.app.controller;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.model.CommonResultModel;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessageV3;
import com.mechanist.ResultEnum;
import com.mechanist.msg.MsgCode;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.MsgSendUtil;
import com.mechanist.util.SyncMessageUtil;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
public class BshController {
    @Autowired
    TunnelServer tunnelServer;
    private final static Logger logger = LoggerFactory.getLogger(BshController.class);

    @RequestMapping(value = "/runBash", method = RequestMethod.POST)
    @ResponseBody
    public String redefine(@RequestParam(value = "selectedAgents") String selectedAgentsStr,
                           @RequestParam(value = "bashContent") String bashContent){

        List<String> selectedAgentsList = JSONObject.parseArray(selectedAgentsStr,String.class);
        Map<String, CommonResultModel> runbshRespMap = new HashMap<>();
        Map<String, Exchanger> exchangerMap = new HashMap<>();
        for (String agentId : selectedAgentsList) {
            Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId);
            if(findAgent.isPresent()){
                AgentInfo agentInfo = findAgent.get();
                ChannelHandlerContext channelHandlerContext = agentInfo.getChannelHandlerContext();
                TunnelMessage.RunBshReq.Builder runBashReqBuilder = TunnelMessage.RunBshReq.newBuilder();
                long messageId = SyncMessageUtil.getExchangerMessageId();
                runBashReqBuilder.setRequestId(messageId);
                runBashReqBuilder.setAgentId(agentId);
                runBashReqBuilder.setBashContent(bashContent);
                SyncMessageUtil.addExchanger(messageId);
                Exchanger<GeneratedMessageV3> exchanger = SyncMessageUtil.getExchanger(messageId);
                MsgSendUtil.send((Integer) MsgCode.RunBashReq[0], runBashReqBuilder.build(), channelHandlerContext.channel());
                exchangerMap.put(agentId, exchanger);
            }
        }
        Iterator<Map.Entry<String, Exchanger>> iterator = exchangerMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Exchanger> next = iterator.next();
            String agentId = next.getKey();
            Exchanger exchanger = next.getValue();
            CommonResultModel runBashResultModel = new CommonResultModel();
            runBashResultModel.setAgentId(agentId);
            try {
                TunnelMessage.RunBshResp runBashResp = (TunnelMessage.RunBshResp) exchanger.exchange(null,
                        10, TimeUnit.SECONDS);;
                runBashResultModel.setResultCode(runBashResp.getStatusCode());
                runBashResultModel.setResultEnumName(ResultEnum.BshResultEnum.values()[runBashResp.getStatusCode()].name());
                runBashResultModel.setMsg(runBashResp.getMsg().replaceAll("\n", "</br>"));
                runbshRespMap.put(agentId, runBashResultModel);
            } catch (InterruptedException e) {
                String msg = "Get runBash result interrupted, agentId:" + agentId;
                runBashResultModel.setResultCode(ResultEnum.BshResultEnum.RUN_BASH_EXCEPTION.ordinal());
                runBashResultModel.setResultEnumName(ResultEnum.BshResultEnum.RUN_BASH_EXCEPTION.name());
                runBashResultModel.setMsg(msg);
                runbshRespMap.put(agentId, runBashResultModel);
                logger.error(msg, e);
            } catch (TimeoutException e) {
                String msg = "Get runBash result timeout, agentId:" + agentId;
                runBashResultModel.setResultCode(ResultEnum.BshResultEnum.RUN_BASH_EXCEPTION.ordinal());
                runBashResultModel.setResultEnumName(ResultEnum.BshResultEnum.RUN_BASH_TIME_OUT.name());
                runBashResultModel.setMsg(msg);
                runbshRespMap.put(agentId, runBashResultModel);
                logger.error(msg, e);
            }
        }
        return JSONObject.toJSONString(runbshRespMap);
    }

}
