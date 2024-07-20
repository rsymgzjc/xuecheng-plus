package com.xuecheng.ucenter.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AccountPasswordService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AccountPasswordServiceImpl implements AccountPasswordService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    AccountPasswordServiceImpl currentProxy;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Override
    @Transactional
    public RestResponse findPassword(FindPasswordDTO findPasswordDTO) {
        //校验验证码
        checkCode(findPasswordDTO);
        //校验密码是否一致
        checkPassword(findPasswordDTO);
        //校验用户是否存在
        XcUser xcUser = checkUser(findPasswordDTO);
        //修改用户的密码为新密码(加密)
        String password = findPasswordDTO.getPassword();
        String encode = encode(password);
        xcUser.setPassword(encode);
        xcUserMapper.updateById(xcUser);
        return RestResponse.success("重置密码成功！");
    }

    @Override
    public RestResponse register(RegisterDTO dto) {
        //校验用户是否存在
        String nickname = dto.getNickname();
        String cellphone = dto.getCellphone();
        String email = dto.getEmail();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getNickname, nickname).eq(XcUser::getCellphone, cellphone)
                .eq(XcUser::getEmail, email));
        if (null != user){
            throw new XueChengPlusException("该用户已存在，请勿重复注册！");
        }
        //校验验证码是否正确
        FindPasswordDTO findPasswordDTO = new FindPasswordDTO();
        BeanUtils.copyProperties(dto,findPasswordDTO);
        checkCode(findPasswordDTO);
        //校验两次输入密码是否一致
        this.checkPassword(findPasswordDTO);
        //向用户表插入数据，设置角色为学生
        currentProxy.initUserDBData(dto);
        return RestResponse.success("注册成功！");
    }

    private void initUserDBData(RegisterDTO dto){
        XcUser xcUser = new XcUser();
        xcUser.setId(UUID.randomUUID().toString());
        //记录从微信得到的昵称
        BeanUtils.copyProperties(dto,xcUser);
        xcUser.setPassword(this.encode(dto.getPassword()));
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setUsername(dto.getNickname());
        xcUser.setName(dto.getNickname());
        xcUserMapper.insert(xcUser);

        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(xcUser.getId());
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
    }

    private String encode(String password){
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    private void checkCode(FindPasswordDTO findPasswordDTO){
        //验证码code
        String checkcode = findPasswordDTO.getCheckcode();
        //验证码key
        String checkcodekey = findPasswordDTO.getCheckcodekey();
        //redis查询
        String code = redisTemplate.opsForValue().get(checkcodekey);
        Optional.ofNullable(code).orElseThrow(()->new XueChengPlusException("验证码不存在，请检查！"));
        if (!checkcode.equals(code)){
            log.error("{}:您输入的验证码错误！",checkcode);
            throw new XueChengPlusException("验证码不一致，请检查");
        }
    }

    private void checkPassword(FindPasswordDTO findPasswordDTO){
        String password = findPasswordDTO.getPassword();
        String confirmpwd = findPasswordDTO.getConfirmpwd();
        if (!password.equals(confirmpwd)){
            throw new XueChengPlusException("密码不一致，请检查");
        }
    }

    private XcUser checkUser(FindPasswordDTO findPasswordDTO){
        String email = findPasswordDTO.getEmail();
        String cellphone = findPasswordDTO.getCellphone();
        XcUser xcUser=new XcUser();
        try {
            xcUser=xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail,email).eq(XcUser::getCellphone,cellphone));

        }catch (Exception e){
            log.error("查询用户异常，原因：{}",e.getMessage());
            throw new XueChengPlusException("查询用户异常");
        }
        if (xcUser==null){
            log.error("根据邮箱：{}，手机号：{}查询到的用户信息为空！",email,cellphone);
            throw new XueChengPlusException("查询到的用户信息为空!");
        }
        return xcUser;
    }
}
