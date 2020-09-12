package com.gitee.swaggeradmin.controller;

import com.gitee.swaggeradmin.common.Action;
import com.gitee.swaggeradmin.common.Result;
import com.gitee.swaggeradmin.controller.param.SystemConfigParam;
import com.gitee.swaggeradmin.entity.SwaggerInfo;
import com.gitee.swaggeradmin.entity.SystemConfig;
import com.gitee.swaggeradmin.mapper.SwaggerInfoMapper;
import com.gitee.swaggeradmin.mapper.SystemConfigMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("systemconfig")
public class SystemConfigController {

    public static final int TYPE_HEADERS = 1;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @PostMapping("/globalHeader/add")
    public Result addHadder(@RequestBody SystemConfigParam systemConfigParam) {
        SystemConfig systemConfig = new SystemConfig();
        BeanUtils.copyProperties(systemConfigParam, systemConfig);
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(systemConfigParam.getSwaggerId());
        systemConfig.setProjectId(swaggerInfo.getProjectId());
        systemConfig.setType(TYPE_HEADERS);
        systemConfigMapper.insertIgnoreNull(systemConfig);
        return Action.ok();
    }

    @GetMapping("/globalHeader/list")
    public Result listHeader(@RequestParam("swaggerId") int swaggerId) {
        SwaggerInfo swaggerInfo = swaggerInfoMapper.getById(swaggerId);
        List<SystemConfig> systemConfigs = systemConfigMapper.listGlobalHeaders(swaggerInfo.getProjectId());
        return Action.ok(systemConfigs);
    }

    @PostMapping("/globalHeader/update")
    public Result updateHeader(@RequestBody SystemConfig systemConfig) {
        systemConfigMapper.update(systemConfig);
        return Action.ok();
    }

    @PostMapping("/globalHeader/delete")
    public Result deleteHeader(@RequestBody SystemConfig systemConfig) {
        systemConfigMapper.delete(systemConfig);
        return Action.ok();
    }

}
