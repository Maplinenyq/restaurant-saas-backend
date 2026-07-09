package com.nyq.service;

import com.nyq.dto.CategoryDTO;
import com.nyq.dto.CategoryPageQueryDTO;
import com.nyq.entity.Category;
import com.nyq.result.PageResult;

import java.util.List;

public interface CategoryService {

    /**
     * 分页查询分类
     * @Parm categoryPageQueryDTO
     * @return
     */
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 根据ID删除分类
     * @param id
     * @return
     */
    void deleteById(long id);

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, long id);

    //修改分类
    void update(CategoryDTO categoryDTO);

    //根据类型查询分类
    List<Category> list(Integer type);
}
