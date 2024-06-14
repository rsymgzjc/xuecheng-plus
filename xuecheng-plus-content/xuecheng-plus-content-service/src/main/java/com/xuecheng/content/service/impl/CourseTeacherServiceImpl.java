package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacherDTO> queryTeacherInfo(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        List<CourseTeacherDTO>courseTeacherDTOS=new ArrayList<>();
        BeanUtils.copyProperties(courseTeachers,courseTeacherDTOS);
        return courseTeacherDTOS;
    }

    @Override
    public CourseTeacherDTO saveCourseTeacher(CourseTeacherDTO courseTeacherDTO) {
        //传id即为修改
        if (courseTeacherDTO.getId() == null) {
            //数据库中设计为课程id与教师姓名为唯一字段，要进行判断不然会报错
            LambdaQueryWrapper<CourseTeacher> qw = new LambdaQueryWrapper<>();
            qw.eq(CourseTeacher::getCourseId, courseTeacherDTO.getCourseId())
                    .eq(CourseTeacher::getTeacherName, courseTeacherDTO.getTeacherName());
            CourseTeacher isCourseTeacher = courseTeacherMapper.selectOne(qw);
            if(isCourseTeacher!=null){
                XueChengPlusException.cast("已有该教师!");
            }
            //插入教师信息
            int insert = courseTeacherMapper.insert(courseTeacherDTO);
            if (insert <= 0) {
                XueChengPlusException.cast("保存教师信息失败");
            }
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(courseTeacherDTO);
            CourseTeacherDTO courseTeacherDTO1=new CourseTeacherDTO();
            BeanUtils.copyProperties(courseTeacher,courseTeacherDTO1);
            return courseTeacherDTO1;
        } else {
            //更新教师信息
            int i = courseTeacherMapper.updateById(courseTeacherDTO);
            if (i <= 0) {
                XueChengPlusException.cast("更新教师失败");
            }
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(courseTeacherDTO);
            CourseTeacherDTO courseTeacherDTO1=new CourseTeacherDTO();
            BeanUtils.copyProperties(courseTeacher,courseTeacherDTO1);
            return courseTeacherDTO1;
        }
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId).eq(CourseTeacher::getId,teacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete<=0){
            XueChengPlusException.cast("删除教师失败");
        }
    }


}
