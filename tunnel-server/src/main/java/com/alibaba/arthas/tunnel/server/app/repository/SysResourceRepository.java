package com.alibaba.arthas.tunnel.server.app.repository;

import com.alibaba.arthas.tunnel.server.app.entity.SysResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysResourceRepository extends JpaRepository<SysResource, Long>{
	List<SysResource> findByIdIn(List<Long> ids);
}
