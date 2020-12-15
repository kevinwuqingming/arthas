package com.alibaba.arthas.tunnel.server.app.security;

import com.alibaba.arthas.tunnel.server.app.entity.SysRole;
import com.alibaba.arthas.tunnel.server.app.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


public class SecurityUser extends SysUser implements UserDetails {
	private static final long serialVersionUID = 1L;
	public SecurityUser(SysUser suser) {
		if(suser != null)
		{
			this.setUserName(suser.getUserName());
			this.setPassword(suser.getPassword());
			this.setSysRoles(suser.getSysRoles());
		}
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		Set<SysRole> userRoles = this.getSysRoles();
		
		if(userRoles != null)
		{
			for (SysRole role : userRoles) {
				SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.getRoleName());
				authorities.add(authority);
			}
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return super.getPassword();
	}

	@Override
	public String getUsername() {
		return super.getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
