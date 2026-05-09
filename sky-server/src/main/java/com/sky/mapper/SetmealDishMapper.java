package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    //根据菜品id查询对应的套餐id
    List< Long> getSetmealIdsByDishIds(List<Long> dishIds);

    //批量插入套餐和菜品的关联数据
    void insertBatch(List<SetmealDish> setmealDishes);

    //根据多个套餐ids删除套餐关联菜品的数据
    void deleteBySetmealIds(List<Long> ids);

    //根据套餐id查询套餐关联的菜品数据
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);

    //根据套餐id删除套餐关联的菜品数据
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteBySetmealId(Long id);
}
