package com.mechanist.event;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.mechanist.Msg;
import com.mechanist.ResultEnum;
import com.mechanist.msg.MsgCode;
import com.mechanist.msg.MsgRedefineReq;
import com.mechanist.proto.MsgData;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.MsgSendUtil;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

/**
 * 热更逻辑
 */
public class EventRedefineReq extends MsgRedefineReq {
    private static Logger logger = LoggerFactory.getLogger(EventRedefineReq.class);

    @Override
    public Msg clone() {
        return new EventRedefineReq();
    }

    @Override
    public void handleEvent() throws Exception {
        TunnelMessage.RedefineResp.Builder redefineRespBuilder = TunnelMessage.RedefineResp.newBuilder();
        Instrumentation inst = ArthasBootstrap.getInstance().getInstrumentation();
        long requestId = redefineReq.getRequestId();
        redefineRespBuilder.setRequestId(requestId);
        redefineRespBuilder.setAgentId(redefineReq.getAgentId());
        int state = ResultEnum.RedefineEnum.SUCCESS.ordinal();
        StringBuilder resultStringBuilder = new StringBuilder();
        List<ClassDefinition> definitions = new ArrayList<ClassDefinition>();
        List<MsgData.RedefineInfo> redefineInfoListList = redefineReq.getRedefineInfoListList();
        Map<String, MsgData.RedefineInfo> redefineInfoMap = new HashMap<String, MsgData.RedefineInfo>();
        for (MsgData.RedefineInfo redefineInfo : redefineInfoListList) {
            redefineInfoMap.put(redefineInfo.getClassPath(), redefineInfo);
        }
        //查询对应的Class loader
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (redefineInfoMap.containsKey(clazz.getName())) {
                if (!Integer.toHexString(clazz.getClassLoader().hashCode()).equals(redefineInfoMap.get(clazz.getName()).getHashCode())) {
                    continue;
                }
                definitions.add(new ClassDefinition(clazz, redefineInfoMap.get(clazz.getName()).getFileContent().toByteArray()));
                logger.info("redefine", "Try redefine class name: {}, ClassLoader: {}", clazz.getName(), clazz.getClassLoader());
            }
        }
        //校验一下需要热更的class是否都找到了
        Iterator<Map.Entry<String, MsgData.RedefineInfo>> iterator = redefineInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MsgData.RedefineInfo> next = iterator.next();
            String classPath = next.getKey();
            boolean classMatch = false;
            for (ClassDefinition definition : definitions) {
                if (definition.getDefinitionClass().getName().equals(classPath)) {
                    classMatch = true;
                    break;
                }
            }
            if (!classMatch) {
                state = ResultEnum.RedefineEnum.CLASS_NOT_FOUND.ordinal();
                resultStringBuilder.append(classPath + ": no class match." + "\n");
                logger.error("EventRedefineReq", classPath + ": no class match.");
                break;
            }
        }
        //校验不通过,取消更新
        if (state != ResultEnum.RedefineEnum.SUCCESS.ordinal()) {
            resultStringBuilder.append("Redefine Canceled!");
            redefineRespBuilder.setStatusCode(state);
            redefineRespBuilder.setMsg(resultStringBuilder.toString());
            MsgSendUtil.send((Integer) MsgCode.RedefineResp[0], redefineRespBuilder.build(), channel);
            logger.error("RedefineError", resultStringBuilder.toString());
            return;
        }

        //执行热更
        try {
            inst.redefineClasses(definitions.toArray(new ClassDefinition[0]));
            resultStringBuilder.append("redefine success, size: " + definitions.size() + ";");
            logger.info("redefine success, size: " + definitions.size() + ";");
        } catch (Exception e) {
            state = ResultEnum.RedefineEnum.REDEFINE_EXCEPTION.ordinal();
            resultStringBuilder.append("redefine exception! " + e.getMessage());
            redefineRespBuilder.setStatusCode(state);
            redefineRespBuilder.setMsg(resultStringBuilder.toString());
            logger.error("RedefineException", resultStringBuilder.toString());
            MsgSendUtil.send((Integer) MsgCode.RedefineResp[0], redefineRespBuilder.build(), channel);
            return;
        }
        int replaceSuccessCount = 0;
        if (redefineReq.getReplaceSourceCode()) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            List<MsgData.RedefineInfo> redefineInfoListList1 = redefineReq.getRedefineInfoListList();
            for (MsgData.RedefineInfo redefineInfo : redefineInfoListList1) {
                try {
                    logger.info("replace class: " + redefineInfo.getFileName());
                    byte[] fileContent = redefineInfo.getFileContent().toByteArray();
                    String codeSource = redefineInfo.getCodeSource();
                    //jar
                    File codeSourceFile = new File(codeSource);
                    if (codeSource.contains("!") || (codeSourceFile.exists() && codeSourceFile.isFile())) {
                        logger.info("class is in jar");
                        String[] sourceArray = codeSource.split("!");
                        String jarUri = null;
                        if (codeSourceFile.isFile()) {
                            jarUri = "jar:file:" + sourceArray[0];
                        } else {
                            jarUri = "jar:" + sourceArray[0];
                        }
                        logger.info("jarUri: " + jarUri);
                        String classesInJarFolder = "";
                        if (sourceArray.length > 1) {
                            classesInJarFolder = sourceArray[1];
                            logger.info("classes in Jar folder: " + classesInJarFolder);
                        }
                        String classPath = redefineInfo.getClassPath().replaceAll("\\.", "/");

                        URI uri = URI.create(jarUri);
                        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                            String fullPathInJar = classesInJarFolder + "/" + classPath + ".class";
                            logger.info("full path in jar :" + fullPathInJar);
                            Path pathInJarfile = zipfs.getPath(fullPathInJar);
                            InputStream inputStream = new ByteArrayInputStream(fileContent);
                            Files.copy(inputStream, pathInJarfile,
                                    StandardCopyOption.REPLACE_EXISTING);
                            inputStream.close();
                            logger.info(redefineInfo.getFileName() + " replace success.");
                        }
                    } else {//class file
                        logger.info("class is in folder");
                        String fullClassFilePath = codeSource + "/"
                                + redefineInfo.getClassPath().replaceAll("\\.", "/") + ".class";
                        logger.info("full class file path: " + fullClassFilePath);
                        Path path = Paths.get(fullClassFilePath);
                        InputStream inputStream = new ByteArrayInputStream(fileContent);
                        Files.copy(inputStream, path,
                                StandardCopyOption.REPLACE_EXISTING);
                        inputStream.close();
                        logger.info(redefineInfo.getFileName() + " replace success.");
                    }
                } catch (IOException e) {
                    state = ResultEnum.RedefineEnum.REPLACE_FILE_EXCEPTION.ordinal();
                    resultStringBuilder.append("replace file: " + redefineInfo.getFileName() + " exception! " + e.getMessage());
                    logger.error("RedefineException", resultStringBuilder.toString(), e);
                    continue;
                }
                replaceSuccessCount += 1;
            }
        }
        resultStringBuilder.append(replaceSuccessCount + " files replaced.");
        redefineRespBuilder.setStatusCode(state);
        redefineRespBuilder.setMsg(resultStringBuilder.toString());
        MsgSendUtil.send((Integer) MsgCode.RedefineResp[0], redefineRespBuilder.build(), channel);
        logger.info("Redefine successfully finished.");
    }
}
