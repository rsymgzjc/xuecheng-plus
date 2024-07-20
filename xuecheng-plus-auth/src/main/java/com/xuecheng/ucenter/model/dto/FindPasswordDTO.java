package com.xuecheng.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 找回密码Dto类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindPasswordDTO {

    /*
    手机号
     */
    @NotBlank(message = "手机号不能为空！")
    private String cellphone;

    /*
    电子邮箱
     */
    @NotBlank(message = "邮箱不能为空！")
    private String email;

    /*
    验证码key
     */
    @NotBlank(message = "验证码不能为空！")
    private String checkcodekey;

    /*
    验证码value
     */
    @NotBlank(message = "验证码不能为空！")
    private String checkcode;

    /*
    密码
     */
    @NotBlank(message = "密码不能为空！")
    private String password;

    /*
    确认密码
     */
    @NotBlank(message = "确认密码不能为空！")
    private String confirmpwd;
}
