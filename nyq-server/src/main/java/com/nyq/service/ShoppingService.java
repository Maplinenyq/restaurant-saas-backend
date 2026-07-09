package com.nyq.service;

import com.nyq.dto.ShoppingCartDTO;
import com.nyq.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingService {

    //添加购物车
    void add(ShoppingCartDTO shoppingCartDTO);

    //查看购物车
    List<ShoppingCart> showShoppingCart();

    //减少购物车的一个菜品数量
    void sub(ShoppingCartDTO shoppingCartDTO);

    //清空购物车
    void clean();
}
