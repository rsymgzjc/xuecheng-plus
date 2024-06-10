package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程分类接口
 */

@RestController
@Slf4j
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;
    /**
     * 分类查询
     */
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDTO> queryTreeNodes(){
        return courseCategoryService.queryTreeNodes("1");
    }
    
}
