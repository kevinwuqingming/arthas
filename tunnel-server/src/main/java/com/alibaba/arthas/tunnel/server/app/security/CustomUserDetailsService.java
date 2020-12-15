package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.entity.SysUser;
import com.alibaba.arthas.tunnel.server.app.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService{

	@Autowired  //业务服务类
	private SysUserRepository sysUserRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

		SysUser user = sysUserRepository.findByUserName(userName);
		if (user == null) {
			throw new UsernameNotFoundException("UserName " + userName + " not found");
		}
		// SecurityUser实现UserDetails并将SysUser的name映射为username
		SecurityUser seu = new SecurityUser(user);
		return  seu;
	}
}
