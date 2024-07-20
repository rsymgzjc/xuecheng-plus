package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null){
            return RestResponse.validfail("课程不存在");
        }

        //远程调用内容管理服务根据课程计划id去查询课程计划信息，如果is_preview的值为1表示试学
        //也可以从coursepublish对象中解析出课程计划信息去判断是否支持试学
        //如果支持试学调用媒资服务查询视频的播放地址，返回
        List<TeachplanDto> teachplanTree = contentServiceClient.findTeachplanTree(courseId);
        for (TeachplanDto teachplanDto:teachplanTree){
            checkTree(teachplanDto,mediaId);
        }

        if (StringUtils.isNotEmpty(userId)){
            //获取学习资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if (learnStatus.equals("702002")){
                return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
            }else if (learnStatus.equals("702003")){
                return RestResponse.validfail("已过期需要申请续期或重新支付");
            }else{
                //有资格学习，要返回视频的播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }
        //如果用户没有登录
        //取出课程的收费规则
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")){
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }

        return RestResponse.validfail("该课程没有选课");
    }

    private RestResponse<String> checkTree(TeachplanDto teachplanDto,String mediaId){
        if (teachplanDto.getIsPreview()=="1"){
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }
        if (teachplanDto.getTeachPlanTreeNodes()!=null){
            for (TeachplanDto childNode:teachplanDto.getTeachPlanTreeNodes()){
                checkTree(childNode,mediaId);
            }
        }
    }
}
