package com.alibaba.arthas.tunnel.server.app.repository;

import com.alibaba.arthas.tunnel.server.app.entity.SysUser;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import java.util.List;

public interface SysUserRepository extends DataTablesRepository<SysUser, Long> {
	SysUser findByUserName(String name);
	SysUser findByEmail(String email);
	List<SysUser> findByUserType(int userType);
}
