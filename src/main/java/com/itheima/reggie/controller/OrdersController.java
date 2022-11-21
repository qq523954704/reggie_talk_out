package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrdersService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2022-09-23
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        ordersService.submit(orders);

        return R.success("下单成功");

    }

    /**
     * 订单明细查询
     */

    @GetMapping("page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime,String endTime){

        log.info("{},{},{},{},{}", page,pageSize,number,beginTime,endTime);

        //分页构造
        Page<Orders> pageInfo = new Page<>(page,pageSize);

        //查询条件构造
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(number),Orders::getNumber,number);

        queryWrapper.ge(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .le(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        ordersService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 订单状态修改
     */


    @PutMapping
    public R<String> send(@RequestBody Orders orders){


        ordersService.updateById(orders);

        return R.success("已派送");
    }

}
