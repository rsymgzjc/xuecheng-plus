package com.xuecheng.ucenter.service;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;

public interface AccountPasswordService {

    /**
     * 找回密码
     * @param findPasswordDTO 前端传递找回密码相关
     * @return 统一返回类
     */
    RestResponse findPassword(FindPasswordDTO findPasswordDTO);

    /**
     * 用户注册
     * @param dto 前端传递用户注册相关
     * @return 统一返回类
     */
    RestResponse register(RegisterDTO dto);

}
