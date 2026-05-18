package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {


    //用户下单
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    //订单支付
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    //支付成功，修改订单状态
    void paySuccess(String outTradeNo);

    //历史订单查询
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    //查询订单详情
    OrderVO orderDetail(Long id);

    //取消订单
    void cancel(Long id) throws Exception;

    //再来一单
    void repetition(Long id);

    //管理端条件查询订单
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    //统计各种状态的订单数据
    OrderStatisticsVO statistics();

    //接单
    void confirm(Long id);

    //拒单
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    //商家取消订单
    void cancelById(OrdersCancelDTO ordersCancelDTO);

    //派送订单
    void delivery(Long id);

    //完成订单
    void complete(Long id);

    //订单催单
    void reminder(Long id);
}
