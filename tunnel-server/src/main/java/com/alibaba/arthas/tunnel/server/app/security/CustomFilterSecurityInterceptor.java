package com.alibaba.arthas.tunnel.server.app.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
@Component
public class CustomFilterSecurityInterceptor extends AbstractSecurityInterceptor  implements Filter{

	@Autowired
	private CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
	
	@Autowired
	private CustomAccessDecisionManager customAccessDecisionManager;
	
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		FilterInvocation fi = new FilterInvocation( request, response, chain );  
        invoke(fi);
		
	}

	@Autowired
	public void setCustomAccessDecisionManager(){
		super.setAccessDecisionManager(customAccessDecisionManager);
	}
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

	@Override
	public Class<?> getSecureObjectClass() {
		return FilterInvocation.class;
	}

	@Override
	public SecurityMetadataSource obtainSecurityMetadataSource() {
		return customFilterInvocationSecurityMetadataSource;
	}

    public void invoke( FilterInvocation fi ) throws IOException, ServletException{  
        InterceptorStatusToken  token = super.beforeInvocation(fi);  
        try{  
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());  
        }finally{  
            super.afterInvocation(token, null);  
        }  
          
    }
}
