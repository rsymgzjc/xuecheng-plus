package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询通用参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    //当前页码
    @ApiModelProperty("页码")
    private Long pageNo=1L;
    //每页记录数默认值
    @ApiModelProperty("每页记录数")
    private Long pageSize=10L;
}
