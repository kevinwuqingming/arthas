package com.alibaba.arthas.tunnel.server.app.controller;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/agents")
public class AgentsController {
    @Autowired
    TunnelServer tunnelServer;

    @RequestMapping(value = "/getList",method = RequestMethod.POST)
    @ResponseBody
    public List<AgentInfo> getAgentsInfoList(){
        List<AgentInfo> agentInfoList = tunnelServer.getAgentInfoMap().values().stream().collect(Collectors.toList());
        return agentInfoList;
    }
}
