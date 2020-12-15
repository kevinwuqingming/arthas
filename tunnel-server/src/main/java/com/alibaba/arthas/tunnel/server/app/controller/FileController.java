package com.alibaba.arthas.tunnel.server.app.controller;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.model.RedefineResultModel;
import com.alibaba.arthas.tunnel.server.model.SearchClassResultModel;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.ProtocolStringList;
import com.mechanist.ResultEnum;
import com.mechanist.msg.MsgCode;
import com.mechanist.proto.MsgData;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.MsgSendUtil;
import com.mechanist.util.SyncMessageUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Controller
public class FileController {
    @Autowired
    TunnelServer tunnelServer;
    public static final String FILE2UPLOAD_KEY = "file2update";
    //key = sessionId
    private static Map<String, ChannelHandlerContext> sessionAndTunnelSocketMap = new HashMap<>();
    private final static Logger logger = LoggerFactory.getLogger(FileController.class);

    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public String fileUpload(StandardMultipartHttpServletRequest req, HttpServletResponse rsp) {
        HttpSession session = req.getSession();
        req.getMultiFileMap().values().stream().forEach(multipartFiles -> {
            multipartFiles.stream().forEach(multipartFile -> {
                String originalFilename = multipartFile.getOriginalFilename();
                try {
                    byte[] bytes = multipartFile.getBytes();
                    Object attribute = session.getAttribute(FILE2UPLOAD_KEY);
                    if (attribute == null) {
                        session.setAttribute(FILE2UPLOAD_KEY, new HashMap<String, byte[]>());
                        attribute = session.getAttribute(FILE2UPLOAD_KEY);
                    }
                    Map<String, byte[]> fileMap = (Map<String, byte[]>) attribute;
                    fileMap.put(originalFilename, bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        return "OK";
    }

    @RequestMapping(value = "/fileRemove", method = RequestMethod.POST)
    @ResponseBody
    public String fileRemove(@RequestParam(value = "fileName", required = true) String fileName,
                             HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        Object attribute = session.getAttribute(FILE2UPLOAD_KEY);
        Map<String, String> resultMap = new HashMap<>();
        if(attribute != null){
            Map<String, byte[]> filesMap = (Map<String, byte[]>)attribute;
            filesMap.remove(fileName);
            resultMap.put(fileName, "Removed");
        }
        return JSONObject.toJSONString(resultMap);
    }

    @RequestMapping(value = "/checkUpdateBatch", method = RequestMethod.POST)
    @ResponseBody
    public String checkUpdateBatch(@RequestParam(value = "selectedAgents", required = true) String[] selectedAgents,
                            @SessionAttribute(FILE2UPLOAD_KEY) Map<String, byte[]> filesMap) {
        TunnelMessage.SearchClassReq.Builder builder = TunnelMessage.SearchClassReq.newBuilder();
        filesMap.entrySet().forEach(entry -> {
            byte[] classFileContent = entry.getValue();
            builder.addClassPath(readClassName(classFileContent));
        });
        Map<String, SearchClassResultModel> searchClassResultModelMap = new HashMap<>();
        for (String selectedAgent : selectedAgents) {
            ProtocolStringList classPathList = builder.getClassPathList();
            Iterator<String> iterator = classPathList.iterator();
            while (iterator.hasNext()) {
                String classPath = iterator.next();
                SearchClassResultModel searchClassResultModel = new SearchClassResultModel();
                searchClassResultModel.setAgentId(selectedAgent);
                searchClassResultModel.setClassPath(classPath);
                searchClassResultModelMap.put(getKey(selectedAgent, classPath), searchClassResultModel);
            }
        }

        for (int i = 0; i < selectedAgents.length; i++) {
            String agentId = selectedAgents[i];
            Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId);
            if (findAgent.isPresent()) {
                long messageId = SyncMessageUtil.getExchangerMessageId();
                builder.setRequestId(messageId);
                TunnelMessage.SearchClassReq searchClassReq = builder.build();
                ChannelHandlerContext agentCtx = findAgent.get().getChannelHandlerContext();
                Channel channel = agentCtx.channel();
                SyncMessageUtil.addExchanger(messageId);
                MsgSendUtil.send((Integer) MsgCode.SearchClassReq[0], searchClassReq, channel);
                try {
                    TunnelMessage.SearchClassResp searchClassResp = (TunnelMessage.SearchClassResp) SyncMessageUtil.getExchanger(messageId).exchange(null,
                            10, TimeUnit.SECONDS);
                    if (searchClassResp.getSearchClassResultCount() > 0) {
                        List<MsgData.SearchClassResult> searchClassResultList = searchClassResp.getSearchClassResultList();
                        for (MsgData.SearchClassResult searchClassResult : searchClassResultList) {
                            String classPath = searchClassResult.getClassPath();
                            SearchClassResultModel searchClassResultModel = searchClassResultModelMap.get(getKey(agentId, classPath));
                            List<MsgData.ClazzInfo> clazzInfoList = searchClassResult.getClazzInfoList();
                            for (MsgData.ClazzInfo clazzInfo : clazzInfoList) {
                                searchClassResultModel.getClassLoaderList().add(clazzInfo.getClassLoader());
                                searchClassResultModel.getClassLoaderHashCodeList().add(clazzInfo.getClassLoaderHash());
                                searchClassResultModel.setCodeSource(clazzInfo.getCodeSource());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("FileController", "Search classLoader Interrupted, agentId = " + agentId, e);
                } catch (TimeoutException e) {
                    logger.error("Search classLoader timeout, agentId = " + agentId);
                }
            }
        }
        return JSONObject.toJSONString(searchClassResultModelMap.values().stream().collect(Collectors.toList()));
    }

    @RequestMapping(value = "/checkUpdate", method = RequestMethod.POST)
    @ResponseBody
    public String checkUpdate(@RequestParam(value = "agentId", required = true) String agentId,
                              @SessionAttribute(FILE2UPLOAD_KEY) Map<String, byte[]> filesMap) {
        TunnelMessage.SearchClassReq.Builder builder = TunnelMessage.SearchClassReq.newBuilder();
        filesMap.entrySet().forEach(entry -> {
            byte[] classFileContent = entry.getValue();
            builder.addClassPath(readClassName(classFileContent));
        });
        Map<String, SearchClassResultModel> searchClassResultModelMap = new HashMap<>();
        ProtocolStringList classPathList = builder.getClassPathList();
        Iterator<String> iterator = classPathList.iterator();
        while (iterator.hasNext()) {
            String classPath = iterator.next();
            SearchClassResultModel searchClassResultModel = new SearchClassResultModel();
            searchClassResultModel.setAgentId(agentId);
            searchClassResultModel.setClassPath(classPath);
            searchClassResultModelMap.put(getKey(agentId, classPath), searchClassResultModel);
        }
        Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId);
        if (findAgent.isPresent()) {
            long messageId = SyncMessageUtil.getExchangerMessageId();
            builder.setRequestId(messageId);
            TunnelMessage.SearchClassReq searchClassReq = builder.build();
            ChannelHandlerContext agentCtx = findAgent.get().getChannelHandlerContext();
            Channel channel = agentCtx.channel();
            SyncMessageUtil.addExchanger(messageId);
            MsgSendUtil.send((Integer) MsgCode.SearchClassReq[0], searchClassReq, channel);
            try {
                TunnelMessage.SearchClassResp searchClassResp = (TunnelMessage.SearchClassResp) SyncMessageUtil.getExchanger(messageId).exchange(null,
                        10, TimeUnit.SECONDS);
                if (searchClassResp.getSearchClassResultCount() > 0) {
                    List<MsgData.SearchClassResult> searchClassResultList = searchClassResp.getSearchClassResultList();
                    for (MsgData.SearchClassResult searchClassResult : searchClassResultList) {
                        String classPath = searchClassResult.getClassPath();
                        SearchClassResultModel searchClassResultModel = searchClassResultModelMap.get(getKey(agentId, classPath));
                        List<MsgData.ClazzInfo> clazzInfoList = searchClassResult.getClazzInfoList();
                        for (MsgData.ClazzInfo clazzInfo : clazzInfoList) {
                            String classLoader = clazzInfo.getClassLoader();
                            classLoader = classLoader.replaceAll(" ","");
                            String[] splitClassLoder = classLoader.split("\n");
                            classLoader = StringUtils.join(splitClassLoder, "<br/>&nbsp;&nbsp;&nbsp;&nbsp;");
                            searchClassResultModel.getClassLoaderList().add(classLoader);
                            searchClassResultModel.getClassLoaderHashCodeList().add(clazzInfo.getClassLoaderHash());
                            searchClassResultModel.setCodeSource(clazzInfo.getCodeSource());
                        }
                        if(clazzInfoList.size() == 1 && !searchClassResultModel.getClassLoaderHashCodeList().isEmpty()){
                            searchClassResultModel.setSelectedClassLoaderHash(searchClassResultModel.getClassLoaderHashCodeList().get(0));
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.error("FileController", "Search classLoader Interrupted, agentId = " + agentId, e);
            } catch (TimeoutException e) {
                logger.error("Search classLoader timeout, agentId = " + agentId);
            }
        }

        List<SearchClassResultModel> resultModelList = searchClassResultModelMap.values().stream().collect(Collectors.toList());
        return JSONObject.toJSONString(resultModelList);
    }

    @RequestMapping(value = "/redefine", method = RequestMethod.POST)
    @ResponseBody
    public String redefine(@RequestParam(value = "selectedAgents", required = true) String selectedAgentsStr,
                           @SessionAttribute(FILE2UPLOAD_KEY) Map<String, byte[]> filesMap,
                           @RequestParam(value = "searchClassResultModelList", required = true) String searchClassResultModelListStr,
                           @RequestParam(value = "replaceSourceCode", required = true) boolean replaceSourceCode){

        List<String> selectedAgentsList = JSONObject.parseArray(selectedAgentsStr,String.class);
        List<SearchClassResultModel> searchClassResultModelList = JSONObject.parseArray(searchClassResultModelListStr, SearchClassResultModel.class);
        Map<String, RedefineResultModel> redefineRespMap = new HashMap<>();
        Map<String, Exchanger> exchangerMap = new HashMap<>();
        for (String agentId : selectedAgentsList) {
            Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId);
            if(findAgent.isPresent()){
                AgentInfo agentInfo = findAgent.get();
                ChannelHandlerContext channelHandlerContext = agentInfo.getChannelHandlerContext();
                TunnelMessage.RedefineReq.Builder redefineReqBuilder = TunnelMessage.RedefineReq.newBuilder();
                long messageId = SyncMessageUtil.getExchangerMessageId();
                redefineReqBuilder.setRequestId(messageId);
                for (SearchClassResultModel searchClassResultModel : searchClassResultModelList) {
                    if(!searchClassResultModel.getAgentId().equals(agentId)){
                        continue;
                    }
                    MsgData.RedefineInfo.Builder redefineInfoBuilder = MsgData.RedefineInfo.newBuilder();
                    redefineReqBuilder.setRequestId(messageId);
                    redefineReqBuilder.setAgentId(agentId);
                    String classPath = searchClassResultModel.getClassPath();
                    String[] splitClassPath = classPath.split("\\.");
                    String fileName = splitClassPath[splitClassPath.length - 1] + ".class";
                    redefineInfoBuilder.setFileName(fileName);
                    redefineInfoBuilder.setFileContent(ByteString.copyFrom(filesMap.get(fileName)));
                    redefineInfoBuilder.setClassPath(classPath);
                    redefineInfoBuilder.setHashCode(searchClassResultModel.getSelectedClassLoaderHash());
                    redefineInfoBuilder.setCodeSource(searchClassResultModel.getCodeSource());
                    redefineReqBuilder.addRedefineInfoList(redefineInfoBuilder);
                }
                redefineReqBuilder.setReplaceSourceCode(replaceSourceCode);
                SyncMessageUtil.addExchanger(messageId);
                Exchanger<GeneratedMessageV3> exchanger = SyncMessageUtil.getExchanger(messageId);
                MsgSendUtil.send((Integer) MsgCode.RedefineReq[0], redefineReqBuilder.build(), channelHandlerContext.channel());
                exchangerMap.put(agentId, exchanger);
            }
        }
        Iterator<Map.Entry<String, Exchanger>> iterator = exchangerMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Exchanger> next = iterator.next();
            String agentId = next.getKey();
            Exchanger exchanger = next.getValue();
            RedefineResultModel redefineResultModel = new RedefineResultModel();
            redefineResultModel.setAgentId(agentId);
            try {
                TunnelMessage.RedefineResp redefineResp = (TunnelMessage.RedefineResp) exchanger.exchange(null,
                        10, TimeUnit.SECONDS);;
                redefineResultModel.setResultCode(redefineResp.getStatusCode());
                redefineResultModel.setResultEnumName(ResultEnum.RedefineEnum.values()[redefineResp.getStatusCode()].name());
                redefineResultModel.setMsg(redefineResp.getMsg());
                redefineRespMap.put(agentId, redefineResultModel);
            } catch (InterruptedException e) {
                String msg = "Get redefine result interrupted, agentId:" + agentId;
                redefineResultModel.setResultCode(ResultEnum.RedefineEnum.GET_RESULT_INTERRUPTED_EXCEPTION.ordinal());
                redefineResultModel.setResultEnumName(ResultEnum.RedefineEnum.GET_RESULT_INTERRUPTED_EXCEPTION.name());
                redefineResultModel.setMsg(msg);
                redefineRespMap.put(agentId, redefineResultModel);
                logger.error(msg, e);
            } catch (TimeoutException e) {
                String msg = "Get redefine result timeout, agentId:" + agentId;
                redefineResultModel.setResultCode(ResultEnum.RedefineEnum.GET_RESULT_TIMEOUT.ordinal());
                redefineResultModel.setResultEnumName(ResultEnum.RedefineEnum.GET_RESULT_TIMEOUT.name());
                redefineResultModel.setMsg(msg);
                redefineRespMap.put(agentId, redefineResultModel);
                logger.error(msg, e);
            }
        }
        return JSONObject.toJSONString(redefineRespMap);
    }

    private String getKey(String agentId, String classPath) {
        return agentId + "," + classPath;
    }

    private static String readClassName(final byte[] bytes) {
        return new ClassReader(bytes).getClassName().replace("/", ".");
    }

}
