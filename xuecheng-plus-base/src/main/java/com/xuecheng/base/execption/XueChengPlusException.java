package com.xuecheng.base.execption;

import lombok.Getter;

/**
 * 异常类
 */
@Getter
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException(){
    }
    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

}
