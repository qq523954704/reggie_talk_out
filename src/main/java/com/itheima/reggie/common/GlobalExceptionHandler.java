package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理类
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class}) //通知，拦截类上加了@RestController,@Controller注解的异常
@ResponseBody//返回Json格式
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Mysql异常处理方法
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {

        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) { //sql返回的username重复错误提示
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    //分类删除相关异常捕获
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {

        log.error(ex.getMessage());
        return R.error(ex.getMessage());

    }
}
