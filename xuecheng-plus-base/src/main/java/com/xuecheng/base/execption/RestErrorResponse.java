package com.xuecheng.base.execption;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
@Setter
@Getter
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage=errMessage;
    }

}
