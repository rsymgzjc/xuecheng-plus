package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 课程发布的任务类
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    //执行课程发布任务的逻辑，如果抛出异常说明任务执行失败
    @Override
    public boolean execute(MqMessage mqMessage) {

        //从mqMessage拿到课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);
        //向elasticsearch写索引数据
        saveCourseIndex(mqMessage,courseId);
        //向redis写缓存
        saveCourseCache(mqMessage,courseId);
        //返回true，表示任务完成
        return true;
    }

    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,Long courseId){
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne>0){
            log.debug("课程静态化任务完成，无需处理。。。");
            return;
        }
        //开始进行课程静态化
        File file=coursePublishService.generateCourseHtml(courseId);
        if (file==null){
            XueChengPlusException.cast("生成的静态页面为空");
        }
        //将html上传到minio
        coursePublishService.uploadCourseHtml(courseId,file);
        //任务处理完成写任务状态为完成
        mqMessageService.completedStageOne(taskId);
    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,Long courseId){
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree>0){
            log.debug("课程信息已缓存到Redis，无需处理。。。");
            return;
        }
        //开始缓存课程信息

        //任务处理完成写任务状态为完成
        mqMessageService.completedStageThree(taskId);
    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,Long courseId){
        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService=this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo>0){
            log.debug("课程索引信息已写入，无需处理。。。");
            return;
        }
        //查询课程信息，调用搜索服务添加索引
        //从课程发布表查询课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);

        //远程调用
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add){
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }
        //任务处理完成写任务状态为完成
        mqMessageService.completedStageTwo(taskId);
    }
}
