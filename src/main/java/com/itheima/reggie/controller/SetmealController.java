package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 套餐 前端控制器
 * </p>
 *
 * @author author
 * @since 2022-09-04
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //清理setmealCache分类下的所有数据
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐:{}", setmealDto);

        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");

    }


    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //由于Page<Setmeal>没有套餐分类名称categoryName，使用 Page<SetmealDto> setmealDtoPage
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //查询套餐分类
        setmealService.page(pageInfo, queryWrapper);
        //扩展分类
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> setmealDtoList = records.stream().map(setmeal -> {

            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            Category category = categoryService.getById(setmeal.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);

    }

    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //清理setmealCache分类下的所有数据
    public R<String> delete(@RequestParam List<Long> ids) {

        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询套餐数据
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.id+'_'+#setmeal.status")//在分类setmealCache下存数据
    public R<List<Setmeal>> list(Setmeal setmeal) {

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);


    }


    /**
     * 根据Id查询套餐信息
     */

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {

        SetmealDto setmealDto = setmealService.getDto(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     */
    @ApiOperation("修改套餐")
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {

        if (setmealDto == null) return R.error("错误");
        if (setmealDto.getSetmealDishes() == null) return R.error("无菜品数据");

        setmealService.removeWithDish(List.of(setmealDto.getId()));
        setmealService.saveWithDish(setmealDto);


        return R.success("修改成功");
    }


    //停售停售套餐
    @ApiOperation("停售套餐")
    @PostMapping("/status/{status}")
    @Transactional
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {

        log.info("status{}", status);
        log.info("ids{}", ids);

        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids).set(Setmeal::getStatus, status);

        setmealService.update(updateWrapper);

        return status == 0 ? R.success("已停售") : R.success("已起售");
    }

    /**
     * 套餐相关菜品查询查询
     */

    @Autowired
    private DishService dishService;

    @ApiOperation("套餐相关菜品查询查询")
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable String id) { //数据库SetmealId是varchar,用Long会查询出所有

        log.info("setmealId:{}",id);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtoList = setmealDishList.stream().map(setmealDish -> {

            DishDto dishDto = new DishDto();
            Dish dish = dishService.getById(setmealDish.getDishId());
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setCopies(setmealDish.getCopies());
            return dishDto;

        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

}
