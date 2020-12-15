package com.alibaba.arthas.tunnel.server.app.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;
//角色表
@Entity
@Table(name="s_role")
public class SysRole implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column (name="id")
	@JsonView(DataTablesOutput.View.class)
	private long id;
	
	@Column(name="role_name",length=32)
	@JsonView(DataTablesOutput.View.class)
	private String roleName;//角色名称
	
	@Column(name="discription",length=64)
	@JsonView(DataTablesOutput.View.class)
	private String discription;//角色描述
	
	@ManyToMany(cascade = CascadeType.MERGE,fetch = FetchType.EAGER)
	@JoinTable(name = "s_role_resource", 
				joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
							   inverseJoinColumns = {@JoinColumn(name = "resource_id", referencedColumnName = "id")})
	@JsonView(DataTablesOutput.View.class)
	private Set<SysResource> sysResources = new HashSet<SysResource>(0);// 所对应的资源集合
	
	@ManyToMany(mappedBy = "sysRoles")
	private Set<SysUser> sysUsers;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getDiscription() {
		return discription;
	}
	public void setDiscription(String discription) {
		this.discription = discription;
	}
	public Set<SysUser> getSysUsers() {
		return sysUsers;
	}
	public void setSysUsers(Set<SysUser> sysUsers) {
		this.sysUsers = sysUsers;
	}
	public Set<SysResource> getSysResources() {
		return sysResources;
	}
	public void setSysResources(Set<SysResource> sysResources) {
		this.sysResources = sysResources;
	}
	
	
}
