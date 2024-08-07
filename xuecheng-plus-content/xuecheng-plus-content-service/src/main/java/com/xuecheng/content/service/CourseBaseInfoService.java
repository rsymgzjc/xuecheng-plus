package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {

    //课程分页查询
    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
    //新增课程
    public CourseBaseInfoDto createCourseBase(Long companyID, AddCourseDto addCourseDto);
    //根据课程id查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);
    //修改课程信息
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);
    /**
     * 删除课程信息
     * @param courseId
     */
    void deleteCourseBase(Long courseId,Long companyId);
}
