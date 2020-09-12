package com.gitee.swaggeradmin.doc;

import java.util.List;

/**
 * @author tanghc
 */
public class DocInfo {
    private Integer swaggerId;
    private String title;
    private String version;
    private String description;
    private String host;
    private String basePath;
    private List<DocModule> docModules;

    public Integer getSwaggerId() {
        return swaggerId;
    }

    public void setSwaggerId(Integer swaggerId) {
        this.swaggerId = swaggerId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocModule> getDocModules() {
        return docModules;
    }

    public void setDocModules(List<DocModule> docModules) {
        this.docModules = docModules;
    }
}
