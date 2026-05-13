package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingService shoppingService;

    // 添加购物车
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车：{}", shoppingCartDTO);
        shoppingService.add(shoppingCartDTO);
        return Result.success();
    }

    // 查看购物车
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车");
        return Result.success(shoppingService.showShoppingCart());
    }

    // 购物车菜品减少
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("购物车菜品减少：{}", shoppingCartDTO);
        shoppingService.sub(shoppingCartDTO);
        return Result.success();
    }

    // 清空购物车
    @DeleteMapping("/clean")
    public Result clean(){
        log.info("清空购物车");
        shoppingService.clean();
        return Result.success();
    }
}
