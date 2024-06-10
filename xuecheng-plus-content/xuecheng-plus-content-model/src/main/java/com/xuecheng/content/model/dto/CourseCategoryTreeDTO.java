package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CourseCategoryTreeDTO extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDTO> childrenTreeNodes;
}
