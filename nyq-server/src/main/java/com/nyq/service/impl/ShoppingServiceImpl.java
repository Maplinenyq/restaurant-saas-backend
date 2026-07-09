package com.nyq.service.impl;

import com.nyq.context.BaseContext;
import com.nyq.dto.ShoppingCartDTO;
import com.nyq.entity.Dish;
import com.nyq.entity.ShoppingCart;
import com.nyq.mapper.DishMapper;
import com.nyq.mapper.SetmealMapper;
import com.nyq.mapper.ShoppingMapper;
import com.nyq.service.ShoppingService;
import com.nyq.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingServiceImpl implements ShoppingService {

    @Autowired
    private ShoppingMapper shoppingMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        //判断当前商品是否在购物车中
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> list = shoppingMapper.list(shoppingCart);

        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //如果已经存在了，只需要在数据量加一
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingMapper.updateNumById(cart);
        }else{
            //如果不存在，则添加到购物车

            //判断本次添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null){
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                //添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();

                SetmealVO setmealVO = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setAmount(setmealVO.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingMapper.insert(shoppingCart);
        }
    }

    //获取购物车
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long currentId = BaseContext.getCurrentId();
        return shoppingMapper.list(ShoppingCart.builder().userId(currentId).build());
    }

    //减少购物车的一个菜品数量
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        //设置当前用户的ID
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() == 1) {
                // 如果数量为1，则直接删除这条记录
                shoppingMapper.deleteById(cart.getId());  // 用购物车记录的ID删除
            } else {
                cart.setNumber(cart.getNumber() - 1);
                shoppingMapper.updateNumById(cart);
            }
        }
    }

    //清空购物车
    @Override
    public void clean() {
        //设置当前用户的ID
        Long currentId = BaseContext.getCurrentId();
        shoppingMapper.deleteAllById(currentId);
    }
}
