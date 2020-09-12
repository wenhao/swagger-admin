package com.gitee.swaggeradmin.doc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析swagger的json内容
 *
 * @author tanghc
 */
public class SwaggerDocParser {

    private final Set<String> cycleCache = new HashSet<>(8);

    public DocInfo parseJson(String swaggerJson) {
        cycleCache.clear();
        JSONObject docRoot = JSON.parseObject(swaggerJson, Feature.OrderedField, Feature.DisableCircularReferenceDetect);
        JSONObject info = docRoot.getJSONObject("info");
        String host = docRoot.getString("host");
        String basePath = docRoot.getString("basePath");
        List<DocItem> docItems = new ArrayList<>();

        JSONObject paths = docRoot.getJSONObject("paths");
        if (paths == null) {
            paths = new JSONObject();
        }
        Set<String> pathNameSet = paths.keySet();
        for (String path : pathNameSet) {
            JSONObject pathInfo = paths.getJSONObject(path);
            // key: get,post,head...
            Set<String> httpMethodList = pathInfo.keySet();
            for (String method : httpMethodList) {
                ApiInfo apiInfo = new ApiInfo();
                apiInfo.setBasePath(basePath);
                apiInfo.setHost(host);
                apiInfo.setPath(path);
                apiInfo.setMethod(method);

                JSONObject docInfo = pathInfo.getJSONObject(method);
                DocItem docItem = buildDocItem(apiInfo, docInfo, docRoot);
                if (docItem == null) {
                    continue;
                }
                docItems.add(docItem);
            }
        }

        List<DocModule> docModuleList = docItems.stream()
                .collect(Collectors.groupingBy(DocItem::getModule))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<DocItem> docItemList = entry.getValue();
                    DocModule docModule = new DocModule();
                    docModule.setModule(entry.getKey());
                    docModule.setItems(docItemList);
                    return docModule;
                }).sorted(Comparator.comparing(DocModule::getOrder))
                .collect(Collectors.toList());

        DocInfo docInfo = new DocInfo();

        docInfo.setTitle(info.getString("title"));
        docInfo.setVersion(info.getString("version"));
        docInfo.setDescription(info.getString("description"));
        docInfo.setHost(host);
        docInfo.setBasePath(basePath);
        docInfo.setDocModules(docModuleList);
        return docInfo;
    }

    protected DocItem buildDocItem(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        DocItem docItem = new DocItem();
        docItem.setId(DigestUtils.md5DigestAsHex((apiInfo.toString()).getBytes(StandardCharsets.UTF_8)));
        docItem.setBasePath(apiInfo.basePath);
        docItem.setHost(apiInfo.host);
        docItem.setPath(apiInfo.path);
        docItem.setMethod(apiInfo.method);
        docItem.setSummary(docInfo.getString("summary"));
        docItem.setDescription(docInfo.getString("description"));
        docItem.setMultiple(docInfo.getString("multiple") != null);
        docItem.setProduces(docInfo.getJSONArray("produces").toJavaList(String.class));
        JSONArray consumes = docInfo.getJSONArray("consumes");
        if (consumes != null) {
            docItem.setConsumes(consumes.toJavaList(String.class));
        }
        String moduleName = this.buildModuleName(docInfo, docRoot);
        docItem.setModule(moduleName);
        List<DocParameter> docParameterList = this.buildRequestParameterList(apiInfo, docInfo, docRoot);
        docItem.setRequestParameters(docParameterList);

        // 清除缓存，可以再次使用
        cycleCache.clear();

        List<DocParameter> responseParameterList = this.buildResponseParameterList(apiInfo, docInfo, docRoot);
        docItem.setResponseParameters(responseParameterList);

        return docItem;
    }

    protected String buildModuleName(JSONObject docInfo, JSONObject docRoot) {
        String title = docRoot.getJSONObject("info").getString("title");
        JSONArray tags = docInfo.getJSONArray("tags");
        if (tags != null && tags.size() > 0) {
            return tags.getString(0);
        }
        return title;
    }

    protected List<DocParameter> buildRequestParameterList(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        Optional<JSONArray> parametersOptional = Optional.ofNullable(docInfo.getJSONArray("parameters"));
        JSONArray parameters = parametersOptional.orElse(new JSONArray());
        List<DocParameter> docParameterList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject fieldJson = parameters.getJSONObject(i);
            RefInfo refInfo = getRefInfo(fieldJson.getJSONObject("schema"));
            if (refInfo == null) {
                refInfo = getRefInfo(fieldJson.getJSONObject("items"));
            }
            if (refInfo != null) {
                if (refInfo.ref.contains("MultipartFile")) {
                    DocParameter docParameter = fieldJson.toJavaObject(DocParameter.class);
                    docParameter.setType("file");
                    docParameterList.add(docParameter);
                } else {
                    List<DocParameter> parameterList = this.buildDocParameters(apiInfo, refInfo.ref, docRoot, true);
                    docParameterList.addAll(parameterList);
                }
            } else {
                DocParameter docParameter = fieldJson.toJavaObject(DocParameter.class);
                this.setEnum(fieldJson, docParameter);
                docParameterList.add(docParameter);
            }
        }

        Map<String, List<DocParameter>> collect = docParameterList.stream()
                .filter(docParameter -> docParameter.getName().contains("."))
                .map(docParameter -> {
                    String name = docParameter.getName();
                    int index = name.indexOf('.');
                    String module = name.substring(0, index);
                    String newName = name.substring(index + 1);
                    DocParameter ret = new DocParameter();
                    BeanUtils.copyProperties(docParameter, ret);
                    ret.setName(newName);
                    ret.setModule(module);
                    return ret;
                })
                .collect(Collectors.groupingBy(DocParameter::getModule));

        collect.forEach((key, value) -> {
            DocParameter moduleDoc = new DocParameter();
            moduleDoc.setName(key);
            moduleDoc.setType(DataType.OBJECT);
            moduleDoc.setRefs(value);
            docParameterList.add(moduleDoc);
        });

        return docParameterList.stream()
                .filter(docParameter -> !docParameter.getName().contains("."))
                .collect(Collectors.toList());
    }

    private void setEnum(JSONObject fieldJson, DocParameter docParameter) {
        JSONArray enumsArr = fieldJson.getJSONArray("enum");
        // 如果是枚举字段
        if (enumsArr != null) {
            docParameter.setEnums(enumsArr.toJavaList(String.class));
            docParameter.setType(DataType.ENUM);
        }
    }

    protected List<DocParameter> buildResponseParameterList(ApiInfo apiInfo, JSONObject docInfo, JSONObject docRoot) {
        RefInfo refInfo = getResponseRefInfo(docInfo);
        List<DocParameter> respParameterList = Collections.emptyList();
        if (refInfo != null) {
            String responseRef = refInfo.ref;
            respParameterList = this.buildDocParameters(apiInfo, responseRef, docRoot, true);
            // 如果返回数组
            if (refInfo.isArray) {
                DocParameter docParameter = new DocParameter();
                docParameter.setName(refInfo.ref);
                docParameter.setType(DataType.ARRAY);
                docParameter.setRefs(respParameterList);
                respParameterList = Collections.singletonList(docParameter);
            }
        }
        return respParameterList;
    }

    protected List<DocParameter> buildDocParameters(ApiInfo apiInfo, String ref, JSONObject docRoot, boolean doSubRef) {
        JSONObject responseObject = docRoot.getJSONObject("definitions").getJSONObject(ref);
        if (responseObject == null) {
            return Collections.emptyList();
        }
        String className = responseObject.getString("title");
        JSONObject properties = responseObject.getJSONObject("properties");
        List<DocParameter> docParameterList = new ArrayList<>();
        if (properties == null) {
            return docParameterList;
        }
        Set<String> fieldNames = properties.keySet();
        for (String fieldName : fieldNames) {
            /*
               {
                  "description": "分类故事",
                  "$ref": "#/definitions/StoryVO"
                }
             */
            JSONObject fieldInfo = properties.getJSONObject(fieldName);
            DocParameter docParameter = fieldInfo.toJavaObject(DocParameter.class);
            docParameter.setName(fieldName);
            docParameter.setElementType(this.getElementType(fieldInfo));
            RefInfo refInfo = this.getRefInfo(fieldInfo);
            if (refInfo != null) {
                docParameter.setType(DataType.OBJECT);
            }
            if (refInfo != null && doSubRef) {
                String subRef = refInfo.ref;
                String key = apiInfo.toString() + className + fieldName + ref + subRef;
                // 避免对象相互依赖导致无限循环
                boolean nextDoRef = !isSameRef(ref, subRef) && !cycleCache.contains(key);
                if (nextDoRef) {
                    cycleCache.add(key);
                }
                List<DocParameter> refs = buildDocParameters(apiInfo, subRef, docRoot, nextDoRef);
                docParameter.setType(refInfo.isArray ? DataType.ARRAY : DataType.OBJECT);
                docParameter.setRefs(refs);
            }
            this.setEnum(fieldInfo, docParameter);
            docParameterList.add(docParameter);
        }
        return docParameterList;
    }

    private boolean isSameRef(String ref, String subRef) {
        return Objects.equals(ref, subRef);
    }


    /**
     * 返回对象
     *
     * @param docInfo
     * @return
     */
    protected RefInfo getResponseRefInfo(JSONObject docInfo) {
        return Optional.ofNullable(docInfo.getJSONObject("responses"))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("200")))
                .flatMap(jsonObject -> Optional.ofNullable(jsonObject.getJSONObject("schema")))
                .map(this::getRefInfo)
                .orElse(null);
    }

    private String getElementType(JSONObject jsonObject) {
        boolean isArray = DataType.ARRAY.equals(jsonObject.getString("type"));
        if (isArray) {
            JSONObject items = jsonObject.getJSONObject("items");
            String ref = items.getString("$ref");
            ref = getRefName(ref);
            String elementType = items.getString("type");
            return elementType == null ? ref : elementType;
        }
        return null;
    }

    private RefInfo getRefInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String ref;
        boolean isArray = DataType.ARRAY.equals(jsonObject.getString("type"));
        if (isArray) {
            JSONObject items = jsonObject.getJSONObject("items");
            ref = items.getString("$ref");
        } else {
            // #/definitions/Category
            ref = jsonObject.getString("$ref");
        }
        if (ref == null) {
            return null;
        }
        RefInfo refInfo = new RefInfo();
        refInfo.isArray = isArray;
        refInfo.ref = getRefName(ref);
        return refInfo;
    }

    private static String getRefName(String ref) {
        if (ref == null) {
            return null;
        }
        int index = ref.lastIndexOf("/");
        if (index > -1) {
            ref = ref.substring(index + 1);
        }
        return ref;
    }


    private static class ApiInfo {
        private String basePath;
        private String host;
        private String path;
        private String method;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return "ApiInfo{" +
                    "basePath='" + basePath + '\'' +
                    ", host='" + host + '\'' +
                    ", path='" + path + '\'' +
                    ", method='" + method + '\'' +
                    '}';
        }
    }

    private static class RefInfo {
        private boolean isArray;
        private String ref;

        @Override
        public String toString() {
            return "RefInfo{" +
                    "isArray=" + isArray +
                    ", ref='" + ref + '\'' +
                    '}';
        }
    }

}
