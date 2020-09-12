package com.gitee.swaggeradmin.controller.vo;

import com.gitee.swaggeradmin.entity.SwaggerInfo;

import java.util.List;

/**
 * @author tanghc
 */
public class ProjectInfoVO {
    private Integer id;
    /** 项目名称 */
    private String name;
    /** 访问地址，如：http://localhost:8080 */
    private String host;

    private List<SwaggerInfo> groups;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<SwaggerInfo> getGroups() {
        return groups;
    }

    public void setGroups(List<SwaggerInfo> groups) {
        this.groups = groups;
    }
}
