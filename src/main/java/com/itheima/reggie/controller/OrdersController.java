package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {

        ordersService.submit(orders);

        return R.success("下单成功");

    }

    /**
     * 管理员订单明细查询
     */

    @GetMapping("page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime) {

        log.info("{},{},{},{},{}", page, pageSize, number, beginTime, endTime);

        //分页构造
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        //查询条件构造
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number);

        queryWrapper.ge(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime)
                .le(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);

        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 订单状态修改
     */


    @PutMapping
    public R<String> send(@RequestBody Orders orders) {


        ordersService.updateById(orders);

        return R.success("已派送");
    }


    @Resource
    private OrderDetailService orderDetailService;

    /**
     * 用户查看订单
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userOrder(int page, int pageSize) {

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //用户Id
        Long userId = BaseContext.getCurrentId();
        //查询订单表
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        Page<Orders> ordersPage = ordersService.page(pageInfo, queryWrapper);



        //封装订单详情
        List<OrdersDto> ordersDtos = ordersPage.getRecords().stream().map(orders -> {

            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(orders, ordersDto);

            //查询订单明细表
            List<OrderDetail> orderDetails = orderDetailService.getOrderDetails(Long.valueOf(orders.getNumber()));
            ordersDto.setOrderDetails(orderDetails);

            return ordersDto;

        }).collect(Collectors.toList());
        //返回封装
        BeanUtils.copyProperties(ordersPage, ordersDtoPage, "records");
        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }




}
