package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    //分页查询
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    //新增套餐
    void save(SetmealDTO setmealDTO);

    //根据ID批量删除套餐
    void deleteByIds(List<Long> ids);

    //根据ID查询套餐
    SetmealVO getById(Long id);

    //套餐起售停售
    void startOrStop(Integer status, Long id);

    //修改套餐
    void update(SetmealDTO setmealDTO);

    //条件查询
    List<Setmeal> list(Setmeal setmeal);

    //根据套餐id查询包含的菜品
    List<DishItemVO> getDishItemById(Long id);
}
