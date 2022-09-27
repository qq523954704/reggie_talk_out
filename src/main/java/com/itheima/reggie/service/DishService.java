package com.itheima.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

/**
 * <p>
 * 菜品管理 服务类
 * </p>
 *
 * @author author
 * @since 2022-09-04
 */
public interface DishService extends IService<Dish> {

    //新增菜品，同时插入口味数据
    void saveWithFlavor(DishDto dishDto);

    //查询菜品
    DishDto getByIdWithFlavors(Long id);

    //更新
    void updateWithFlavor(DishDto dishDto);
}
