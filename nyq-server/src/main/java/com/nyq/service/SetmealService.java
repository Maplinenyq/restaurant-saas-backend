package com.nyq.service;

import com.nyq.dto.SetmealDTO;
import com.nyq.dto.SetmealPageQueryDTO;
import com.nyq.entity.Setmeal;
import com.nyq.result.PageResult;
import com.nyq.vo.DishItemVO;
import com.nyq.vo.SetmealVO;

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
