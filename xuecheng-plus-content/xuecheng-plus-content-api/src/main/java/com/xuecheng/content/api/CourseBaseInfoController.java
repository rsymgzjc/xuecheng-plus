package com.xuecheng.content.api;


import com.xuecheng.base.execption.ValidationGroups;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Validation;


/**
 * 课程编辑类接口
 */
@RestController
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") //指定权限标识符
    @ApiOperation("课程查询接口")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto){
        SecurityUtil.XcUser user=SecurityUtil.getUser();
        //用户所属机构id
        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        PageResult<CourseBase> pageResult=courseBaseInfoService.queryCourseBaseList(companyId,pageParams,queryCourseParamsDto);
        return pageResult;
    }
    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto addCourseDto){
        //获取用户所属机构ID
        Long companyId=1L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }
    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        //获取当前用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }
    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated({ValidationGroups.Update.class}) EditCourseDto editCourseDto){
        //机构id，由于认证系统没有上线暂时硬编码
        SecurityUtil.XcUser user=SecurityUtil.getUser();
        if (user==null){
            XueChengPlusException.cast("请登陆后再修改");
        }
        String companyId = user.getCompanyId();
        return courseBaseInfoService.updateCourseBase(Long.valueOf(companyId),editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId){
        SecurityUtil.XcUser user=SecurityUtil.getUser();
        if (user==null){
            XueChengPlusException.cast("请登陆后再修改");
        }
        String companyId = user.getCompanyId();
        courseBaseInfoService.deleteCourseBase(courseId,Long.valueOf(companyId));
    }
}
