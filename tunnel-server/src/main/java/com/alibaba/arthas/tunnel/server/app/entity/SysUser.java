package com.alibaba.arthas.tunnel.server.app.entity;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "s_user")
public class SysUser implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@JsonView(DataTablesOutput.View.class)
	private long id;

	@Column(name = "userName", length = 120)
	@JsonView(DataTablesOutput.View.class)
	private String userName; // 用户名

	@Column(name = "password", length = 120)
	@JsonView(DataTablesOutput.View.class)
	private String password;// 用户密码

	@Column(name = "name", length = 50)
	@JsonView(DataTablesOutput.View.class)
	private String name; // 姓名

	@Column(name = "email", length = 50)
	@JsonView(DataTablesOutput.View.class)
	private String email;// 用户邮箱

	@Column(name = "phone", length = 16)
	@JsonView(DataTablesOutput.View.class)
	private String phone;// 电话
	
	@Column(name = "userType")
	@JsonView(DataTablesOutput.View.class)
	private int userType;// 1=管理用户,2=渠道

	@Temporal(TemporalType.DATE)
	@Column(name = "registerDatetime", length = 10)
	@JsonView(DataTablesOutput.View.class)
	private Date registerDatetime;// 时间

	@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinTable(name = "s_user_role", joinColumns = {
			@JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", referencedColumnName = "id") })
	@JsonView(DataTablesOutput.View.class)
	private Set<SysRole> sysRoles = new HashSet<SysRole>(0);// 所对应的角色集合

	public SysUser() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getRegisterDatetime() {
		return registerDatetime;
	}

	public void setRegisterDatetime(Date registerDatetime) {
		this.registerDatetime = registerDatetime;
	}

	public Set<SysRole> getSysRoles() {
		return sysRoles;
	}

	public void setSysRoles(Set<SysRole> sysRoles) {
		this.sysRoles = sysRoles;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}
	
	

}
