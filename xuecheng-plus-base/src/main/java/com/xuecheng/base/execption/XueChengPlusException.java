package com.xuecheng.base.execption;

/**
 * 异常类
 */
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException(){
    }
    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

}
