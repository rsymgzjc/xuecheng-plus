package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划编辑接口
 */

@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{courseId}")
    public void deleteTeachplan(@PathVariable Long courseId){
        teachplanService.deleteTeachplan(courseId);
    }

    @ApiOperation("课程计划下移")
    @PostMapping("/teachplan/movedown/{courseId}")
    public void movedownTeachplan(@PathVariable Long courseId){
        teachplanService.movedownTeachplan(courseId);
    }

    @ApiOperation("课程计划上移")
    @PostMapping("/teachplan/moveup/{courseId}")
    public void moveupTeachplan(@PathVariable Long courseId){
        teachplanService.moveupTeachplan(courseId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    /**
     * 课程计划和媒资信息解除绑定
     *
     * @param teachPlanId 课程计划id
     * @param mediaId 媒资id
     */
    @ApiOperation(value = "课程计划和媒资信息解除绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void deleteTeachplanMedia(@PathVariable Long teachPlanId,@PathVariable String mediaId){
        teachplanService.deleteTeachplanMedia(teachPlanId,mediaId);
    }

}
