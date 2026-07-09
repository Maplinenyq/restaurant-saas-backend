package com.nyq.mapper;

import com.github.pagehelper.Page;
import com.nyq.annotation.AutoFill;
import com.nyq.dto.DishPageQueryDTO;
import com.nyq.entity.Dish;
import com.nyq.enumeration.OperationType;
import com.nyq.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


@Mapper
public interface DishMapper {

    // 根据分类id查询菜品数量
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    // 插入菜品数据
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    // 分页查询
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    // 根据id删除菜品
    void deleteByIds(List< Long> ids);

    // 根据id查询菜品
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    // 根据id修改菜品
    @AutoFill(value = OperationType.UPDATE)
    void updateWithFlavor(Dish dish);

    //菜品启售停售
    @AutoFill(value = OperationType.UPDATE)
    @Update("update dish set status = #{status} where id = #{id}")
    void startOrStop(Dish dish);

    //根据分类id查询菜品
    List<Dish> list(Dish dish);

    // 根据条件统计菜品数量
    Integer countByMap(Map map);
}
