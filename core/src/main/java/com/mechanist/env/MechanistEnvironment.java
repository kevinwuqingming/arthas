package com.mechanist.env;

import com.mechanist.configs.ArthasConfig;
import com.mechanist.configs.Config;
import com.taobao.arthas.core.config.Configure;

import java.io.File;

public class MechanistEnvironment {
    private EnvironmentEnum environmentEnum;
    private File workSpace;
    private File arthasConfigDir;

    private static class SingletonHolder{
        protected static final MechanistEnvironment instance = new MechanistEnvironment();
    }
    public static MechanistEnvironment getInstance(){
        return SingletonHolder.instance;
    }
    public void  init(Configure configure){
        if(File.separator.equals("/")){
            environmentEnum = EnvironmentEnum.Linux;
        }else if(File.separator.equals("\\")){
            environmentEnum = EnvironmentEnum.Windows;
        }else {
            environmentEnum = EnvironmentEnum.None;
        }

        String workSpacePath = System.getProperty("arthas.work.dir", "arthas-work");
        workSpace = new File(workSpacePath);
        workSpace.mkdirs();

        String configPath = System.getProperty("arthas.config.dir", "arthas-config");
        arthasConfigDir = new File(configPath);
        if(arthasConfigDir.exists() && arthasConfigDir.isDirectory()){
            Config.load(arthasConfigDir.getPath());
            configure.setArthasAgent(ArthasConfig.ARTHAS_CORE_JAR_PATH);
            configure.setAgentId(ArthasConfig.ARTHAS_CORE_AGENT_ID);
            configure.setIp(ArthasConfig.ARTHAS_CORE_LISTEN_IP);
            configure.setHttpPort(ArthasConfig.ARTHAS_CORE_LISTEN_HTTP_PORT);
            configure.setTelnetPort(ArthasConfig.ARTHAS_CORE_LISTEN_TELNET_PORT);
            configure.setTunnelServer(ArthasConfig.ARTHAS_CORE_TUNNEL_SERVER_ADDR);
            configure.setSessionTimeout(ArthasConfig.ARTHAS_CORE_SESSION_TIMEOUT);
        }

    }

    public EnvironmentEnum getEnvironmentEnum() {
        return environmentEnum;
    }

    public void setEnvironmentEnum(EnvironmentEnum environmentEnum) {
        this.environmentEnum = environmentEnum;
    }

    public File getWorkSpace() {
        return workSpace;
    }

    public void setWorkSpace(File workSpace) {
        this.workSpace = workSpace;
    }
}
