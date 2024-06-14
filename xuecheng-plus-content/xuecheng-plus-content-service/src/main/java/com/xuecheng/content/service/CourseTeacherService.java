package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-06-03
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    /**
     * 查询教师信息
     * @param courseId
     * @return
     */
    List<CourseTeacherDTO> queryTeacherInfo(Long courseId);

    /**
     * 保存与修改课程老师
     * @param courseTeacherDTO
     * @return
     */
    CourseTeacherDTO saveCourseTeacher(CourseTeacherDTO courseTeacherDTO);

    /**
     * 删除教师信息
     * @param courseId
     * @param teacherId
     */
    void deleteCourseTeacher(Long courseId,Long teacherId);
}
