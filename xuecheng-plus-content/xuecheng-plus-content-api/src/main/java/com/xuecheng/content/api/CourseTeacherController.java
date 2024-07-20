package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师管理接口
 */
@RestController
@Api(value = "教师管理接口",tags = "教师管理接口")
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/courseTeacher/list/{courseId}")
    @ApiOperation("查询教师信息")
    public List<CourseTeacherDTO> queryTeacherInfo(@PathVariable Long courseId){
        return courseTeacherService.queryTeacherInfo(courseId);
    }

    @ApiOperation("添加与修改教师接口")
    @PostMapping("/courseTeacher")
    public CourseTeacherDTO saveCourseTeacher(@RequestBody CourseTeacherDTO courseTeacherDTO){
        return courseTeacherService.saveCourseTeacher(courseTeacherDTO);
    }

    @ApiOperation("删除教师接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable  Long courseId,@PathVariable Long teacherId){
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }
}
