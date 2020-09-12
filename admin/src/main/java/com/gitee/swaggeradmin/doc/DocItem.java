package com.gitee.swaggeradmin.doc;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author tanghc
 */
public class DocItem {
    /**
     * path+method
     */
    private String id;
    private String module;
    private String path;
    private String summary;
    private String description;
    /** http method */
    private String method;

    private String host;
    private String basePath;

    private Collection<String> consumes;
    private Collection<String> produces;

    List<DocParameter> requestParameters;
    List<DocParameter> responseParameters;

    /** 是否多文件上传 */
    private boolean multiple;

    /**
     * 是否是上传文件请求
     * @return
     */
    public boolean isUploadRequest() {
        boolean upload = false;
        if (requestParameters != null) {
            for (DocParameter requestParameter : requestParameters) {
                String type = requestParameter.getType();
                if ("file".equalsIgnoreCase(type)) {
                    upload = true;
                    break;
                }
            }
        }
        return multiple || upload;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(Collection<String> consumes) {
        this.consumes = consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }

    public void setProduces(Collection<String> produces) {
        this.produces = produces;
    }

    public List<DocParameter> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(List<DocParameter> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public List<DocParameter> getResponseParameters() {
        return responseParameters;
    }

    public void setResponseParameters(List<DocParameter> responseParameters) {
        this.responseParameters = responseParameters;
    }

    public boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }


}
