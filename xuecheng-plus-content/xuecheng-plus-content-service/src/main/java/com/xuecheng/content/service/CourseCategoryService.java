package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     *
     * @return
     */
    public List<CourseCategoryTreeDTO> queryTreeNodes(String id);

}
