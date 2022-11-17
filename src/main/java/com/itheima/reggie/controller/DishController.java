package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * <p>
 * 菜品管理 前端控制器
 * </p>
 *
 * @author author
 * @since 2022-09-04
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }


    /**
     * 菜品分页查询
     *
     * @param page     1
     * @param pageSize 10
     * @param name     菜品名称
     * @return 数据
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name) {

        //分页构造
        //Page<Dish> pageInfo = new Page<>(page,pageSize);

        //条件构造，添加过滤、排序
        // LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        //queryWrapper.like(name != null,Dish::getName,name);
        //queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //菜品查询成功后，对菜品分类categoryId替换为菜品分类categoryName，应该使用DishDto
        //dishService.page(pageInfo, queryWrapper);


        //分页构造
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造，添加过滤、排序
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //菜品查询成功后，对菜品分类categoryId替换为菜品分类categoryName，应该使用DishDto
        dishService.page(pageInfo, queryWrapper);
        //拷贝除records()的数据到page<DishDto>
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records()");

        //菜单数据dish封装成DishDto
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dishDtoRecords = records.stream().map(dish -> {
            Long categoryId = dish.getCategoryId();//分类Id
            //查询分类name
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);//复制属性
            dishDto.setCategoryName(categoryName);

            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(dishDtoRecords);

        return R.success(dishDtoPage);
    }

    /**
     * 根据Id查询菜品及口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavors(id);
        return R.success(dishDto);

    }


//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //查询条件
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        //在售状态
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //排序
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> dishList= dishService.list(queryWrapper);
//        return R.success(dishList);
//    }


    //扩展
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        List<DishDto> dishDtoList = null;

        //构造key
        String key = "dish" + dish.getCategoryId() + "_" + dish.getStatus();

        //1.从redis获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            //redis存在数据返回
            return R.success(dishDtoList);

            //不存在进行数据库查询
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //在售状态
        queryWrapper.eq(Dish::getStatus, 1);

        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);


        dishDtoList = dishList.stream().map(dish1 -> {

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1, dishDto);//复制属性

            Long categoryId = dish1.getCategoryId();//分类Id
            //查询分类name
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            Long dish1Id = dish1.getId();

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dish1Id);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);

            dishDto.setFlavors(dishFlavors);
            return dishDto;

        }).collect(Collectors.toList());

        //储存数据到redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);

    }

    //停售菜品

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status, Long[] ids) {

        log.info(status.toString());
        log.info(ids.toString());

//
//            LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
//            queryWrapper.in(ids !=null,Dish::getId,ids);
//            List<Dish> list = dishService.list(queryWrapper);
//            for (Dish dish : list) {
//                dish.setStatus(status);
//            }
//            dishService.updateBatchById(list);
//            return R.success("修改成功");
//
//

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ids != null, Dish::getId, ids).set(Dish::getStatus, status);

        dishService.update(updateWrapper);

        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> status(@RequestParam("ids") List<Long> ids) {


        log.info(ids.toString());

        List<Dish> dishList = dishService.listByIds(ids);
        for (Dish dish : dishList) {
            if (dish.getStatus()==1) throw new CustomException("在售中不可删除");
        }

        dishService.removeByIds(ids);

        return R.success("删除成功");
    }


}
