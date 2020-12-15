package com.alibaba.arthas.tunnel.server.app.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;
@Entity
@Table(name="s_resource")
public class SysResource {
		@Id
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		@Column (name="id")
		@JsonView(DataTablesOutput.View.class)
		private long id;
		
		@Column(name="resourceString",length=1000)
		private String resourceString;//url
		
		@Column(name="resourceId",length=50)
		private String resourceId;//资源ID
		
		@Column(name="remark",length=200)
		private String remark;//备注
		
		@Column(name="resourceName",length=400)
		@JsonView(DataTablesOutput.View.class)
		private String resourceName;//资源名称
		
		@Column(name="methodName",length=400)
		private String methodName;//资源所对应的方法名
		
		@Column(name="methodPath",length=1000)
		private String methodPath;//资源所对应的包路径
		
		@ManyToMany(mappedBy = "sysResources")
		private Set<SysRole> sysRoles;
		
		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getResourceString() {
			return resourceString;
		}

		public void setResourceString(String resourceString) {
			this.resourceString = resourceString;
		}

		public String getResourceId() {
			return resourceId;
		}

		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public String getMethodPath() {
			return methodPath;
		}

		public void setMethodPath(String methodPath) {
			this.methodPath = methodPath;
		}

		public Set<SysRole> getSysRoles() {
			return sysRoles;
		}

		public void setSysRoles(Set<SysRole> sysRoles) {
			this.sysRoles = sysRoles;
		}
		
}
