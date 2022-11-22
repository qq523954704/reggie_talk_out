package com.itheima.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.OrderDetail;

import java.util.List;

/**
 * <p>
 * 订单明细表 服务类
 * </p>
 *
 * @author author
 * @since 2022-09-23
 */
public interface OrderDetailService extends IService<OrderDetail> {

    //根据订单号找订单明细
    List<OrderDetail> getOrderDetails(Long orderId);

}
