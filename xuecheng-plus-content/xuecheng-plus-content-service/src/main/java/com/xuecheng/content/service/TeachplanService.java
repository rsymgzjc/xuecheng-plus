package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * 课程基本信息管理业务接口
 */
public interface TeachplanService extends IService<Teachplan> {

    /**
     * 查询课程计划树形结构
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);
    /**
     * 新增/保存/修改课程计划
     */
    void saveTeachplan(SaveTeachplanDto teachplanDto);
    /**
     * 删除课程计划
     */
    void deleteTeachplan(Long courseId);
    /**
     * 课程计划下移
     * @param courseId
     */
    void movedownTeachplan(Long courseId);
    /**
     * 课程计划上移
     * @param courseId
     */
    void moveupTeachplan(Long courseId);
}
