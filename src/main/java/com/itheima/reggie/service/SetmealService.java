package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 套餐 服务类
 * </p>
 *
 * @author author
 * @since 2022-09-04
 */
@Service
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐和菜品关系表
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐和菜品关联数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);
}
