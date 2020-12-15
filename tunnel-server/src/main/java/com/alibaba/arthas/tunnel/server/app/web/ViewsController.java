package com.alibaba.arthas.tunnel.server.app.web;

import com.alibaba.arthas.tunnel.server.app.AgentConnectPassListener;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import com.alibaba.arthas.tunnel.server.app.controller.FileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 
 * @author polaris 2020-01-19
 *
 */
@Controller
public class ViewsController {
    private final static Logger logger = LoggerFactory.getLogger(ViewsController.class);
    @Autowired
    ArthasProperties arthasProperties;
    @Autowired
    AgentConnectPassListener agentConnectPassListener;

    @RequestMapping(value = "/console")
    public ModelAndView getWebConsolePage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("webConsole.html");
        int webPort = arthasProperties.getServer().getWebPort();
        if(webPort == 0){
            webPort = arthasProperties.getServer().getPort() + 1;
        }
        modelAndView.addObject("wsPort", webPort);
        modelAndView.addObject("isSSL",arthasProperties.getServer().isSsl());
        String connectPass = UUID.randomUUID().toString().replaceAll("-","").toUpperCase();
        modelAndView.addObject("cp", connectPass);
        agentConnectPassListener.addConnectPass(request.getSession(), connectPass);
        return modelAndView;
    }

    @RequestMapping(value = "/terminal")
    public ModelAndView getTerminalPage(@RequestParam(value = "agentId",required = true)String agentId,
                                        @RequestParam(value = "cp",required = true)String cp) {
        if(!AgentConnectPassListener.passExist(cp)){
            new ModelAndView("error.html");
        }
        ModelAndView modelAndView = new ModelAndView("terminal.html");
        int webPort = arthasProperties.getServer().getWebPort();
        if(webPort == 0){
            webPort = arthasProperties.getServer().getPort() + 1;
        }
        modelAndView.addObject("wsPort", webPort);
        modelAndView.addObject("isSSL",arthasProperties.getServer().isSsl());
        modelAndView.addObject("agentId",agentId);
        modelAndView.addObject("cp", cp);
        return modelAndView;
    }

    @RequestMapping(value = "/redefine")
    public ModelAndView getRedefinePage(HttpServletRequest req, HttpServletResponse rsp) {
        HttpSession session = req.getSession();
        session.removeAttribute(FileController.FILE2UPLOAD_KEY);
        return new ModelAndView("redefine.html");
    }


    @RequestMapping(value = "/bsh")
    public ModelAndView getBshPage() {
        return new ModelAndView("bsh.html");
    }

//    @RequestMapping(value = "/index")
//    public ModelAndView getIndexPage() {
//        return new ModelAndView("index.html");
//    }

}
