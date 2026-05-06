package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询分类,参数为{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类，参数为{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }
    /**
     * 根据ID删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据ID删除分类")
    public Result deleteById(@RequestParam long id){
        log.info("根据ID删除分类，参数为{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }
    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result startOrStop(@PathVariable Integer status, @RequestParam long id){
        log.info("启用禁用分类，参数为{}", id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }
    //修改分类
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类，参数为{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }
    //根据类型查询分类
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result list(@RequestParam Integer type){
        log.info("根据类型查询分类，参数为{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
