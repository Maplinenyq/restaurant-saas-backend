package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingMapper {

    // 根据用户ID查询，回显
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    // 修改
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumById(ShoppingCart cart);

    // 插入
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    // 根据ID删除购物车的一个菜品
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    // 删除所有
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteAllById(Long currentId);
}
