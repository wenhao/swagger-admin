package com.gitee.swaggeradmin.controller;

import com.gitee.swaggeradmin.common.Action;
import com.gitee.swaggeradmin.common.Result;
import com.gitee.swaggeradmin.entity.SwaggerInfo;
import com.gitee.swaggeradmin.mapper.SwaggerInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("swagger")
public class SwaggerInfoController {

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @GetMapping("/list/{projectId}")
    public Result list(@PathVariable("projectId") int projectId) {
        List<SwaggerInfo> serverConfigs = swaggerInfoMapper.listByProjectId(projectId);
        return Action.ok(serverConfigs);
    }

    @GetMapping("/group/{projectId}")
    public Result group(@PathVariable("projectId") int projectId) {
        List<SwaggerInfo> serverConfigs = swaggerInfoMapper.listGroupByProjectId(projectId);
        return Action.ok(serverConfigs);
    }

    @GetMapping("/get/first")
    public Result first() {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getFirst();
        return Action.ok(swaggerInfo);
    }

    @PostMapping("/add")
    public Result add(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.insertIgnoreNull(docInfo);
        return Action.ok(docInfo);
    }

    @PostMapping("/update")
    public Result update(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.update(docInfo);
        return Action.ok();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody SwaggerInfo docInfo) {
        swaggerInfoMapper.delete(docInfo);
        return Action.ok();
    }

}
