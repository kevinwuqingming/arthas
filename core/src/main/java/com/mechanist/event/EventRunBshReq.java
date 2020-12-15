package com.mechanist.event;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.mechanist.Msg;
import com.mechanist.ResultEnum;
import com.mechanist.bsh.BshRunner;
import com.mechanist.msg.MsgCode;
import com.mechanist.msg.MsgRunBshReq;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.MsgSendUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * 热更逻辑
 */
public class EventRunBshReq extends MsgRunBshReq {
    private static Logger logger = LoggerFactory.getLogger(EventRunBshReq.class);

    @Override
    public Msg clone() {
        return new EventRunBshReq();
    }

    @Override
    public void handleEvent() throws Exception {
        TunnelMessage.RunBshResp.Builder runBshRespBuilder = TunnelMessage.RunBshResp.newBuilder();
        long requestId = runBshReq.getRequestId();
        runBshRespBuilder.setRequestId(requestId);
        runBshRespBuilder.setAgentId(runBshReq.getAgentId());
        int state = ResultEnum.BshResultEnum.SUCCESS.ordinal();
        StringBuilder resultStringBuilder = new StringBuilder();

        //执行bsh代码
        try {
            BshRunner bshRunner = new BshRunner();
            Object result = bshRunner.eval(runBshReq.getBashContent());
            String ret;
            if (result == null)
            {
                ret = "调用成功,返回值为null";
            }
            else if (result instanceof String)
            {
                ret = "调用成功,返回值为:\n" + result;
            }
            else
            {
                ret = "调用成功,返回值为:\n" + reflect(result);
            }
            resultStringBuilder.append(ret);
            logger.info("bsh run result: "+ ret);
        } catch (Exception e) {
            String exceptionMsg = getExceptionMsg(e);
            state = ResultEnum.BshResultEnum.RUN_BASH_EXCEPTION.ordinal();
            resultStringBuilder.append("调用异常:\n");
            resultStringBuilder.append(exceptionMsg);
            runBshRespBuilder.setStatusCode(state);
            runBshRespBuilder.setMsg(resultStringBuilder.toString());
            logger.error("EventRunBshReq","调用异常", e);
            MsgSendUtil.send((Integer) MsgCode.RunBashResp[0], runBshRespBuilder.build(), channel);
            return;
        }
        runBshRespBuilder.setStatusCode(state);
        runBshRespBuilder.setMsg(resultStringBuilder.toString());
        MsgSendUtil.send((Integer) MsgCode.RunBashResp[0], runBshRespBuilder.build(), channel);
        logger.info("bsh run successfully finished.");
    }
    public static String reflect(Object o)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            Class cls = o.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
            {
                Field f = fields[i];
                f.setAccessible(true);
                sb.append("属性名:").append(f.getName());
                sb.append(" 属性值:").append(f.get(o)).append("\n");
            }
        }
        catch (Exception e)
        {
            return e.toString();
        }

        return sb.toString();
    }

    private String getExceptionMsg(Exception ex){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        ex.printStackTrace(printStream);

        String exceptionInformation = new String(outputStream.toByteArray());
        try {
            printStream.close();
            outputStream.close();
        } catch (IOException e) {
            logger.error("BshRunner", "流关闭失败", e);
        }
        return exceptionInformation;
    }
}
