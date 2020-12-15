package com.alibaba.arthas.tunnel.server.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alibaba.arthas.tunnel.server.app.entity.SysUserValidation;


public interface SysUserValidationRepository extends JpaRepository<SysUserValidation, Long>{

	SysUserValidation findByUserName(String userName);
}
