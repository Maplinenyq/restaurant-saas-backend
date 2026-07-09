package com.nyq.controller.admin;

import com.nyq.dto.OrdersCancelDTO;
import com.nyq.dto.OrdersConfirmDTO;
import com.nyq.dto.OrdersPageQueryDTO;
import com.nyq.dto.OrdersRejectionDTO;
import com.nyq.result.PageResult;
import com.nyq.result.Result;
import com.nyq.service.OrderService;
import com.nyq.vo.OrderStatisticsVO;
import com.nyq.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    //条件搜索
    @GetMapping("/conditionSearch")
    @ApiOperation("条件搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("条件搜索，参数：{}", ordersPageQueryDTO);
        PageResult page = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(page);
    }

    //各个状态的订单数量统计
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        return Result.success(orderService.statistics());
    }

    //查询订单详细信息
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详细信息")
    public Result<OrderVO> details(@PathVariable Long id){
        return Result.success(orderService.orderDetail(id));
    }

    //接单
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO.getId());
        return Result.success();
    }

    //拒单
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    //取消订单
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.cancelById(ordersCancelDTO);
        return Result.success();
    }
    //派送订单
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }
    //完成订单
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
}
