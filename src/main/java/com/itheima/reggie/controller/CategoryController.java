package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类表
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //分类管理新增
    @PostMapping
    public R<String> save(@RequestBody Category category){

        log.info("save{}",category.toString());
        categoryService.save(category);

        return R.success("新增分类成功");

    }

    //分类管理分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){

        //分页构建器
        Page<Category> pageInfo = new Page<>(page,pageSize);

        //分页查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.ne(Category::getIsDeleted,1); //判断是否被删除
        queryWrapper.orderByAsc(Category::getSort); //排序

        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    //分类管理删除
    @DeleteMapping
    public R<String> delete(Long ids){

        categoryService.remove(ids);
        return R.success("删除成功");

    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){

        log.info("修改分类消息: {}",category.toString());
        categoryService.updateById(category);

        return R.success("修改成功");

    }

    /**
     * 获取分类
     * @return
     */
    @GetMapping("list")
    public R<List<Category>> getList(Category category){

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null, Category::getType,category.getType());
        //添加排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }


}
