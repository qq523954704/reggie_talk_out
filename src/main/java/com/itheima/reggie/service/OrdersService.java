package com.itheima.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author author
 * @since 2022-09-23
 */
public interface OrdersService extends IService<Orders> {
    /**
     * 用户下单接口
     */
    public void submit(Orders orders);

}
