package com.alibaba.arthas.tunnel.server.app;

import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;
import java.util.Map;

@Component
@WebListener
public class AgentConnectPassListener implements HttpSessionListener {
    private static Map<String, String> connectPassMap = new HashMap<>();
    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        connectPassMap.remove(se.getSession().getId());
    }

    public void addConnectPass(HttpSession session, String connectPass){
        connectPassMap.put(session.getId(), connectPass);
    }

    public static boolean passExist(String connectPass){
        return connectPassMap.values().stream().anyMatch(s -> s.equals(connectPass));
    }
}
