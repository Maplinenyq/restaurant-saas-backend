package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("admin/dish")
@Slf4j
@Api(tags = "菜品管理")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    //新增菜品
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品，参数为{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        clearCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    //分页查询菜品
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品，参数为{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    //批量删除
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids ){
        log.info("批量删除菜品，ids为{}", ids);
        dishService.deleteByIds(ids);
        //将所有的缓存数据清理掉，所有以dish_ 开头的key
        clearCache("dish_*");
        return Result.success();
    }

    //根据id查询
    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品，id为{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    //修改菜品
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品，参数为{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //清理缓存数据
        clearCache("dish_*");
        return Result.success();
    }

    //菜品起售停售
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("菜品起售停售，状态为{}，id为{}", status, id);
        dishService.startOrStop(status, id);
        //清理缓存数据
        clearCache("dish_*");
        return Result.success();
    }

    //根据分类id查询菜品
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类id查询菜品，分类id为{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void clearCache(String key){
        redisTemplate.delete(redisTemplate.keys(key));
    }
}
