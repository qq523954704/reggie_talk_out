package com.itheima.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
* @author yi以
* @description 针对表【category(菜品及套餐分类)】的数据库操作Service
* @createDate 2022-09-04 16:30:34
*/
public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
