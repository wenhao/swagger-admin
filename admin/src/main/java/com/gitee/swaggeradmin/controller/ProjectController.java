package com.gitee.swaggeradmin.controller;

import com.gitee.swaggeradmin.common.Action;
import com.gitee.swaggeradmin.common.Result;
import com.gitee.swaggeradmin.controller.vo.ProjectInfoVO;
import com.gitee.swaggeradmin.entity.ProjectInfo;
import com.gitee.swaggeradmin.entity.SwaggerInfo;
import com.gitee.swaggeradmin.mapper.ProjectInfoMapper;
import com.gitee.swaggeradmin.mapper.SwaggerInfoMapper;
import com.gitee.swaggeradmin.service.DocService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("project")
public class ProjectController {

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private SwaggerInfoMapper swaggerInfoMapper;

    @Autowired
    private DocService docService;

    @GetMapping("/list/all")
    public Result list() {
        List<ProjectInfo> projectInfos = projectInfoMapper.listAll();
        List<ProjectInfoVO> projectInfoVOS = projectInfos.stream()
                .map(projectInfo -> {
                    ProjectInfoVO projectInfoVO = new ProjectInfoVO();
                    BeanUtils.copyProperties(projectInfo, projectInfoVO);
                    List<SwaggerInfo> swaggerInfos = swaggerInfoMapper.listGroupByProjectId(projectInfo.getId());
                    projectInfoVO.setGroups(swaggerInfos);
                    return projectInfoVO;
                })
                .collect(Collectors.toList());
        return Action.ok(projectInfoVOS);
    }

    @PostMapping("/add")
    public Result add(@RequestBody ProjectInfo projectInfo) {
        docService.addProject(projectInfo.getName(), projectInfo.getHost());
        return Action.ok();
    }

    @PostMapping("/update")
    public Result update(@RequestBody ProjectInfo projectInfo) {
        projectInfoMapper.updateIgnoreNull(projectInfo);
        return Action.ok();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody ProjectInfo projectInfo) {
        swaggerInfoMapper.deleteByProjectId(projectInfo.getId());
        projectInfoMapper.delete(projectInfo);
        return Action.ok();
    }

}
