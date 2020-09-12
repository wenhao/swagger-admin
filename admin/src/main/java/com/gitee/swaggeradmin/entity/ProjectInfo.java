package com.gitee.swaggeradmin.entity;


/**
 * 项目信息
 */
public class ProjectInfo {
	private Integer id;
	/** 项目名称 */
	private String name;
	/** 访问地址，如：http://localhost:8080 */
	private String host;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return this.host;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) {return false;}
		ProjectInfo that = (ProjectInfo) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectInfo{" +
				"id=" + id +
				",name='" + name + "'" +
				",host='" + host + "'" +
				'}';
	}

}