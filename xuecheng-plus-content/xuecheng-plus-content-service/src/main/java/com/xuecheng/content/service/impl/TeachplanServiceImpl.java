package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId=teachplanDto.getId();
        if (teachplanId==null){
            //新增
            Teachplan teachplan=new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1
            Long parentid=teachplanDto.getParentid();
            Long courseid=teachplanDto.getCourseId();
            LambdaQueryWrapper<Teachplan>queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper=queryWrapper.eq(Teachplan::getCourseId,courseid).eq(Teachplan::getParentid,parentid);
            Integer count=teachplanMapper.selectCount(queryWrapper);
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
        }else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long courseId) {
        Teachplan teachplan = teachplanMapper.selectById(courseId);
        if (teachplan==null){
            XueChengPlusException.cast("课程计划信息不存在！");
        }
        //判断有没有父节点
        Long parentid = teachplan.getParentid();
        if (parentid==0){
            //删除大章节
            //判断大章节下有没有小章节
            LambdaQueryWrapper<Teachplan>queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,teachplan.getId());
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count>0){
                XueChengPlusException.cast("课程计划信息还有子集信息，无法操作！");
            }
            teachplanMapper.deleteById(courseId);
        }else {
            //删除小章节
            //删除课程计划及媒资信息
            deleteTeachplanWithmedia(courseId);
        }
    }

    @Transactional
    @Override
    public void movedownTeachplan(Long courseId) {
        //1与下一位互换
        swapTeachplan(courseId,1);
    }

    @Transactional
    @Override
    public void moveupTeachplan(Long courseId) {
        //-1与上一位互换
        swapTeachplan(courseId,-1);
    }

    @Transactional
    public void deleteTeachplanWithmedia(Long courseId){
        //查询当前课程信息
        LambdaQueryWrapper<Teachplan>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getId,courseId);
        Teachplan teachplan = teachplanMapper.selectById(courseId);
        if (teachplan==null){
            XueChengPlusException.cast("课程计划信息不存在！");
        }
        //删除课程计划
        teachplanMapper.deleteById(courseId);
        //查询排序字段，在它下面课程计划全部上移一位
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId()).gt(Teachplan::getOrderby,teachplan.getOrderby());
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        teachplans.forEach(item->{
            item.setOrderby(item.getOrderby()-1);
            teachplanMapper.updateById(item);
        });
        //删除绑定媒资信息
        LambdaQueryWrapper<TeachplanMedia>queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(TeachplanMedia::getTeachplanId,courseId);
        teachplanMediaMapper.deleteById(queryWrapper1);
    }
    @Transactional
    public void swapTeachplan(Long courseId,Integer type){
        Teachplan teachplan = teachplanMapper.selectById(courseId);
        Integer orderby = teachplan.getOrderby();
        Integer orderbyNew=orderby+type;
        LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid())
                .eq(Teachplan::getOrderby,orderbyNew)
                .eq(Teachplan::getCourseId,teachplan.getCourseId());
        Teachplan teachplanNext = teachplanMapper.selectOne(queryWrapper);
        //如果没有就是出界了
        if (teachplanNext==null){
            XueChengPlusException.cast("无法移动！");
        }
        //交换排序字段
        teachplan.setOrderby(orderbyNew);
        teachplanNext.setOrderby(orderby);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanNext);
    }
}
