package com.nyq.task;

import com.nyq.entity.Orders;
import com.nyq.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//自定义任务类，定时处理订单状态
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    //处理支付订单超时
    @Scheduled(cron="0 * * * * *") //每分钟执行一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //调用Mapper查询订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT , time);
        if (ordersList != null && !ordersList.isEmpty()) { //订单不为空
            for (Orders orders : ordersList) {
                //修改订单状态为取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时取消");
                orders.setCancelTime(LocalDateTime.now());

                //修改订单
                orderMapper.update(orders);
            }
        }
    }

    //处理派送超时订单
    @Scheduled(cron="0 0 1 * * *") //每日1点执行一次
    //@Scheduled(cron = "0/5 * * * * ? ")
    public void processDeliveryTimeoutOrder(){
        log.info("定时处理派送超时订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        //调用Mapper查询订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS , time);
        if (ordersList != null && !ordersList.isEmpty()) { //订单不为空
            for (Orders orders : ordersList) {
                //修改订单状态为取消
                orders.setStatus(Orders.COMPLETED);

                //修改订单
                orderMapper.update(orders);
            }
        }
    }
}
