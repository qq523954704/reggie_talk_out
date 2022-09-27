package com.itheima.reggie.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单明细表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2022-09-23
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}
