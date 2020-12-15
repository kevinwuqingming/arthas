package com.alibaba.arthas.tunnel.server.app.repository;

import com.alibaba.arthas.tunnel.server.app.entity.SysRole;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

import java.util.List;

public interface SysRoleRepository extends DataTablesRepository<SysRole, Long> {
	SysRole findByRoleName(String name);
	void deleteByIdIn(List<Long> ids);
	List<SysRole> findByIdIn(List<Long> idList);
}
