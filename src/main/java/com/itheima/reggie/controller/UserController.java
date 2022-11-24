package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.Utils.ValidateCodeUtils;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author author
 * @since 2022-09-17
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机短信
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request) {

        System.out.println(1);
        //获取手机号码
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //生成4位验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4).toString();

            //调用阿里云发送短信，此处模拟
            log.info("code={}", code);

            //验证码保存到Session
            //request.getSession().setAttribute("code", code);

            //生成验证码存如reids，有效期为5min
            redisTemplate.opsForValue().set("code", code, 5, TimeUnit.MINUTES);

            return R.success("短信成功");

        }
        return R.error("发送失败");
    }

    /**
     * 登入
     *
     * @param map
     * @param request
     * @return
     */
    @PostMapping("/login")
    private R<User> login(@RequestBody Map map, HttpServletRequest request) {
        log.info(map.toString());  //map接收key，value

        //获取手机号
        String phone = (String) map.get("phone");

        //获取验证码
        String code = (String) map.get("code");

        //对比验证码
        //String ssionCode = (String) request.getSession().getAttribute("code");

        String ssionCode = (String) redisTemplate.opsForValue().get("code");
        log.info(ssionCode);

        //对比验证码
        if (ssionCode != null && ssionCode.equals(code)) {
            //判断是否为新用户，自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) { //新用户注册
                user = new User();
                user.setPhone(phone);
                userService.save(user);
                log.info(user.toString());
            }
            //比对正确登入成功
            request.getSession().setAttribute("user", user.getId());

            //登入成功删除redis数据
            redisTemplate.delete("code");
            return R.success(user);
        }


        //验证码不一致登入失败
        return R.error("登入失败");
    }

    @PostMapping("loginout")
    public R<String> loginOut(HttpSession session) {

        Object user = session.getAttribute("user");
        session.removeAttribute("user");

        return R.success("已退出"+user);
    }

}
