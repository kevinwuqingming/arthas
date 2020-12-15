package com.alibaba.arthas.tunnel.server.model;

import java.util.ArrayList;
import java.util.List;

public class SearchClassResultModel {
    private String agentId;
    private String classPath;
    private String codeSource;
    private List<String> classLoaderList = new ArrayList<>();
    private List<String> classLoaderHashCodeList = new ArrayList<>();
    private String selectedClassLoaderHash;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public List<String> getClassLoaderList() {
        return classLoaderList;
    }

    public void setClassLoaderList(List<String> classLoaderList) {
        this.classLoaderList = classLoaderList;
    }

    public List<String> getClassLoaderHashCodeList() {
        return classLoaderHashCodeList;
    }

    public void setClassLoaderHashCodeList(List<String> classLoaderHashCodeList) {
        this.classLoaderHashCodeList = classLoaderHashCodeList;
    }

    public String getCodeSource() {
        return codeSource;
    }

    public void setCodeSource(String codeSource) {
        this.codeSource = codeSource;
    }

    public String getSelectedClassLoaderHash() {
        return selectedClassLoaderHash;
    }

    public void setSelectedClassLoaderHash(String selectedClassLoaderHash) {
        this.selectedClassLoaderHash = selectedClassLoaderHash;
    }
}
