package com.gitee.swaggeradmin.service;

import com.alibaba.fastjson.JSON;
import com.gitee.swaggeradmin.common.HttpTool;
import com.gitee.swaggeradmin.doc.DocInfo;
import com.gitee.swaggeradmin.doc.SwaggerDocParser;
import com.gitee.swaggeradmin.entity.ProjectInfo;
import com.gitee.swaggeradmin.entity.SwaggerInfo;
import com.gitee.swaggeradmin.mapper.ProjectInfoMapper;
import com.gitee.swaggeradmin.mapper.SwaggerInfoMapper;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Service
public class DocService {

    private static final Logger log = LoggerFactory.getLogger(DocService.class);

    private static final SwaggerDocParser SWAGGER_DOC_PARSER = new SwaggerDocParser();

    private static final HttpTool HTTP_TOOL = new HttpTool();

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Value("${swagger.resources-path:/swagger-resources}")
    private String swaggerResourcesPath;

    /**
     * 添加项目
     *
     * @param name 项目名称
     * @param host host
     */
    @Transactional
    public void addProject(String name, String host) {
        host = formatHost(host);
        if (projectInfoMapper.getByHost(host) != null) {
            throw new RuntimeException(host + "已存在");
        }
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setName(name);
        projectInfo.setHost(host);
        projectInfoMapper.insertIgnoreNull(projectInfo);
        this.refreshSwaggerDoc(projectInfo);
    }

    private static String formatHost(String host) {
        host = StringUtils.trimTrailingCharacter(host, '/');
        return host;
    }

    private List<SwaggerInfo> refreshSwaggerDoc(ProjectInfo projectInfo) {
        Integer projectId = projectInfo.getId();
        String swaggerResourceUrl = projectInfo.getHost() + swaggerResourcesPath;
        String swaggerGroupInfo;
        try {
            // [{"name":"default","url":"/v2/api-docs","swaggerVersion":"2.0","location":"/v2/api-docs"}]
            swaggerGroupInfo = HTTP_TOOL.get(swaggerResourceUrl, null);
        } catch (IOException e) {
            throw new RuntimeException("同步文档出错：" + e.getMessage(), e);
        }
        if (StringUtils.isEmpty(swaggerGroupInfo)) {
            throw new RuntimeException("同步文档出错：文档内容不存在");
        }
        List<SwaggerResource> swaggerResources = JSON.parseArray(swaggerGroupInfo, SwaggerResource.class);
        List<SwaggerInfo> swaggerInfoList = new ArrayList<>(swaggerResources.size());
        Map<String, SwaggerInfo> swaggerInfoMap = swaggerInfoMapper.listByProjectId(projectId)
                .stream()
                .collect(Collectors.toMap(SwaggerInfo::getName, Function.identity()));
        swaggerResources.forEach(swaggerResource -> {
            String name = swaggerResource.name;
            String swaggerUrl = projectInfo.getHost() + swaggerResource.url;
            SwaggerInfo swaggerInfo = swaggerInfoMap.get(name);
            // 新增
            if (swaggerInfo == null) {
                swaggerInfo = new SwaggerInfo();
                swaggerInfo.setProjectId(projectId);
                swaggerInfo.setName(name);
                swaggerInfo.setUrl(swaggerResource.url);
                String docContent = this.fetchSwaggerDoc(swaggerUrl);
                swaggerInfo.setDocContent(docContent);
                swaggerInfoMapper.insertIgnoreNull(swaggerInfo);
            } else {
                // 修改
                swaggerInfo.setUrl(swaggerResource.url);
                String docContent = this.fetchSwaggerDoc(swaggerUrl);
                swaggerInfo.setDocContent(docContent);
                swaggerInfoMapper.update(swaggerInfo);
            }
            swaggerInfoList.add(swaggerInfo);
        });

        // 删除
        List<SwaggerInfo> swaggerInfoTobeDelete = swaggerInfoMap.values()
                .stream()
                .filter(swaggerInfo -> {
                    boolean isDeleted = true;
                    for (SwaggerInfo info : swaggerInfoList) {
                        // 如果存在，表示没有删除
                        if (info.getName().equals(swaggerInfo.getName())) {
                            isDeleted = false;
                            break;
                        }
                    }
                    return isDeleted;
                })
                .collect(Collectors.toList());
        if (!swaggerInfoTobeDelete.isEmpty()) {
            swaggerInfoMapper.deleteBatch(swaggerInfoTobeDelete);
        }
        return swaggerInfoList;

    }

    /**
     * 获取文档内容
     *
     * @param swaggerId swaggerId
     * @return 返回文档对象
     */
    public DocInfo getDoc(int swaggerId) {
        SwaggerInfo docInfo = swaggerInfoMapper.getById(swaggerId);
        Objects.requireNonNull(docInfo, "文档配置不存在");
        return getDoc(docInfo);
    }

    /**
     * 同步工程文档
     *
     * @param projectId
     * @return
     */
    private List<SwaggerInfo> syncProject(int projectId) {
        ProjectInfo projectInfo = projectInfoMapper.getById(projectId);
        return this.refreshSwaggerDoc(projectInfo);
    }

    public List<SwaggerInfo> syncDoc(int swaggerId) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(swaggerId);
        return this.syncProject(swaggerInfo.getProjectId());
    }

    /**
     * 同步文档内容
     *
     * @param docInfo 文档配置
     * @return 返回最新的文档内容
     */
    private String syncDoc(SwaggerInfo docInfo) {
        Integer projectId = docInfo.getProjectId();
        ProjectInfo projectInfo = projectInfoMapper.getById(projectId);
        String swaggerDocUrl = projectInfo.getHost() + docInfo.getUrl();
        String body = this.fetchSwaggerDoc(swaggerDocUrl);
        // 设置doc内容
        docInfo.setDocContent(body);
        swaggerInfoMapper.update(docInfo);
        return body;
    }

    /**
     * 获取swagger文档内容
     *
     * @param url 请求路径
     * @return 返回文档内容
     */
    private String fetchSwaggerDoc(String url) {
        String body = null;
        Response response = null;
        try {
            response = HTTP_TOOL.requestForResponse(url, Collections.emptyMap(), Collections.emptyMap(), HttpTool.HTTPMethod.GET);
            if (response.code() != HttpStatus.OK.value()) {
                throw new RuntimeException("请求服务器错误，httpStatus:" + response.code() + ", body:" + HttpTool.parseBody(response));
            }
            body = response.body().string();
        } catch (IOException e) {
            log.error("同步文档失败, url:{}", url, e);
            throw new RuntimeException("同步文档失败:" + e.getMessage());
        } finally {
            HttpTool.close(response);
        }
        return body;
    }

    /**
     * 获取文档内容
     *
     * @param docInfo 文档配置
     * @return 返回文档对象
     */
    private DocInfo getDoc(SwaggerInfo docInfo) {
        String docContent = docInfo.getDocContent();
        if (StringUtils.isEmpty(docContent)) {
            docContent = syncDoc(docInfo);
        }
        return this.parseDoc(docContent, docInfo);
    }

    private DocInfo parseDoc(String docContent, SwaggerInfo swaggerInfo) {
        DocInfo docInfo = SWAGGER_DOC_PARSER.parseJson(docContent);
        docInfo.setSwaggerId(swaggerInfo.getId());
        docInfo.setTitle(swaggerInfo.getName());
        return docInfo;
    }

    private static class SwaggerResource {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
