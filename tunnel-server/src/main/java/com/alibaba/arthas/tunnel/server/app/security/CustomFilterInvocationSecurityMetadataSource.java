package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.entity.SysResource;
import com.alibaba.arthas.tunnel.server.app.entity.SysRole;
import com.alibaba.arthas.tunnel.server.app.repository.SysRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class CustomFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource{

	@Autowired
	private SysRoleRepository sysRoleRepository;
	private static Map<String, Collection<ConfigAttribute>> resourceMap = null;
	@PostConstruct
	private void loadResourceDefine() { 
		 Iterable<SysRole> sysRoleIterable = sysRoleRepository.findAll();
		 Iterator<SysRole> sysRoleIterator = sysRoleIterable.iterator();
		 resourceMap = new HashMap<String, Collection<ConfigAttribute>>();
		 while(sysRoleIterator.hasNext()) {
			 SysRole sysRole = sysRoleIterator.next();
			 ConfigAttribute configAttribute = new SecurityConfig(sysRole.getRoleName());
			 Set<SysResource> roleResources=sysRole.getSysResources();
			 for(SysResource sysResource:roleResources){
				 if(resourceMap.containsKey(sysResource.getResourceString())){
					 Collection<ConfigAttribute> configAttributes = resourceMap.get(sysResource.getResourceString());
					 configAttributes.add(configAttribute);
					 resourceMap.put(sysResource.getResourceString(), configAttributes);
				 }else{
					 Collection<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
					 configAttributes.add(configAttribute);
					 resourceMap.put(sysResource.getResourceString(), configAttributes);
				 }
			 }
		 }
	}
	
	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	/***
	 *  根据资源URL，找到相关的权限。
	 */
	@Override
	public Collection<ConfigAttribute> getAttributes(Object arg0) throws IllegalArgumentException {
		FilterInvocation fi = (FilterInvocation) arg0;
//		  如果把资源和需要的权限配置到配置文件里面就用下面这一行
//		 Map<String, Collection<ConfigAttribute>> metadataSource = CustomSecurityContext.getMetadataSource();
        for (Map.Entry<String, Collection<ConfigAttribute>> entry : resourceMap.entrySet()) {
            String uri = entry.getKey();
            RequestMatcher requestMatcher = new AntPathRequestMatcher(uri);
            if (requestMatcher.matches(fi.getHttpRequest())) {
                return entry.getValue();
            }
        }
		return null;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

	public void reloadResourceDefine() {
		synchronized (resourceMap) {
			loadResourceDefine();
		}
	}
}
