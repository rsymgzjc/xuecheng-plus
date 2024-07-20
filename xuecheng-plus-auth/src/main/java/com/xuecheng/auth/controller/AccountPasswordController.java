package com.xuecheng.auth.controller;

import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;
import com.xuecheng.ucenter.service.AccountPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 账号密码相关前端控制器
 */
@RestController
public class AccountPasswordController {

    @Autowired
    AccountPasswordService accountPasswordService;


    /**
     * 找回密码
     * @param findPasswordDTO 前端传递找回密码相关
     * @return 统一返回类
     */
    @PostMapping("/findpassword")
    public RestResponse findPassword(@RequestBody @Validated FindPasswordDTO findPasswordDTO){
        Optional.ofNullable(findPasswordDTO).orElseThrow(()->new XueChengPlusException("找回密码相关信息为空！"));
        return accountPasswordService.findPassword(findPasswordDTO);
    }


    /**
     * 用户注册
     * @param dto 前端传递用户注册相关
     * @return 统一返回类
     */
    @PostMapping("/register")
    public RestResponse register(@RequestBody @Validated RegisterDTO dto){
        Optional.ofNullable(dto).orElseThrow(()->new XueChengPlusException("用户注册相关信息为空！"));
        return accountPasswordService.register(dto);
    }

}
