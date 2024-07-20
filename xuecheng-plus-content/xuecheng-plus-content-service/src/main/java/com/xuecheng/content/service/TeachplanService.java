package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

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
    /**
     * 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
    /**
     * 课程计划和媒资信息解除绑定
     *
     * @param teachPlanId
     * @param mediaId
     */
    void deleteTeachplanMedia(Long teachPlanId, String mediaId);
}
