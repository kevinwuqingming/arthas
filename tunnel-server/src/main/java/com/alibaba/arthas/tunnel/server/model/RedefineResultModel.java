package com.alibaba.arthas.tunnel.server.model;

public class RedefineResultModel {
    private String agentId;
    private Integer resultCode;
    private String resultEnumName;
    private String msg;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultEnumName() {
        return resultEnumName;
    }

    public void setResultEnumName(String resultEnumName) {
        this.resultEnumName = resultEnumName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
