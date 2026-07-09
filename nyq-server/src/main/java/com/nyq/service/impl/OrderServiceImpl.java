package com.nyq.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nyq.constant.MessageConstant;
import com.nyq.context.BaseContext;
import com.nyq.dto.*;
import com.nyq.entity.*;
import com.nyq.exception.AddressBookBusinessException;
import com.nyq.exception.OrderBusinessException;
import com.nyq.exception.ShoppingCartBusinessException;
import com.nyq.mapper.*;
import com.nyq.result.PageResult;
import com.nyq.service.OrderService;
import com.nyq.utils.HttpClientUtil;
import com.nyq.vo.OrderPaymentVO;
import com.nyq.vo.OrderStatisticsVO;
import com.nyq.vo.OrderSubmitVO;
import com.nyq.vo.OrderVO;
import com.nyq.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingMapper shoppingMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Value("${nyq.shop.address}")
    private String shopAddress;
    @Value("${nyq.baidu.ak}")
    private String ak;

    //用户下单
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常（地址簿为空，购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出地址簿为空的异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> list = shoppingMapper.list(new ShoppingCart());
        if(list == null || list.isEmpty()){
            //抛出购物车为空的异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //检查用户的收货地址是否超出配送范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT); //待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis())); //获取当前时间戳，设置订单号
        orders.setPhone(addressBook.getPhone()); //手机号
        orders.setUserId(BaseContext.getCurrentId()); //用户id
        orders.setConsignee(addressBook.getConsignee()); //收货人
        orders.setAddress(addressBook.getDetail()); // 地址

        orderMapper.insert(orders);

        //向订单明细表插入多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(ShoppingCart cart : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置订单ID
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        //删除购物车数据
        shoppingMapper.deleteAllById(BaseContext.getCurrentId());

        //返回VO数据
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 模拟微信支付成功，直接构造返回数据
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .signType("MD5")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .packageStr("prepay_id=mock_prepay_id")
                .build();

        // 修改订单状态为已支付
        paySuccess(ordersPaymentDTO.getOrderNumber());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map = new HashMap<>();
        map.put("type", 1); //1 来单提醒  2 客户催单
        map.put("orderId", ordersDB.getId()); //订单id
        map.put("content", "订单号：" + outTradeNo);  //订单内容

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Long userId = BaseContext.getCurrentId(); //获取用户ID
        ordersPageQueryDTO.setUserId(userId); //DTO插入用户ID
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);//分页条件查询

        //遍历查询订单详细表
        List<OrderVO> list = new ArrayList<>();
        if(page != null && !page.isEmpty()){
            for(Orders orders : page){
                Long orderId = orders.getId();
                //根据订单Id查询订单详细
                List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        if (page != null) {
            return new PageResult(page.getTotal(), list);
        }
        return new PageResult(0, list);
    }

    //查询订单详情
    @Override
    public OrderVO orderDetail(Long id) {
        //根据订单ID联合订单明细表查询订单详情
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderId(id);

        //封装VO数据
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    //取消订单
    @Override
    public void cancel(Long id) throws Exception {
        //根据订单ID查询订单数据
        Orders orders = orderMapper.getById(id);

        //判断订单是否存在
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断订单状态,只有未支付和待接单状态的订单才能取消
        if(orders.getStatus()>(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersDB = new Orders();
        ordersDB.setId(orders.getId());

        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //!!由于没做微信支付功能，所以这里模拟取消订单成功
            //将订单状态修改为退款
            ordersDB.setPayStatus(Orders.REFUND);
        }

        //修改订单状态
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用户取消了订单");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);
    }

    //再来一单
    @Override
    public void repetition(Long id) {
        //获取当前用户的Id
        Long userId = BaseContext.getCurrentId();
        //根据ID查询对应的订单明细表
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(id);

        //将订单明细表数据构造成购物车数据
        List<ShoppingCart> shoppingCartList = orderDetailList.stream()
                .map(x->{
                    ShoppingCart shoppingCart = new ShoppingCart();
                    shoppingCart.setUserId(userId);
                    BeanUtils.copyProperties(x,shoppingCart);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    return shoppingCart;
                }).collect(Collectors.toList());
        //批量插入到购物车
        shoppingMapper.insertBatch(shoppingCartList);
    }

    //管理端条件订单查询
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //部分订单需要返回订单详情，所以将orders列表转为OrdersVO列表
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    //获取订单菜品信息
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        //获取订单列表
        List<Orders> ordersList = page.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        if(ordersList != null && !ordersList.isEmpty()){
            for(Orders orders : ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    //解析订单中的菜品数据
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orders.getId());
        if(orderDetailList!= null && ! orderDetailList.isEmpty()){
            List<String> orderDishes = orderDetailList.stream().
                    map(x->x.getName() + "*" + x.getNumber())
                    .collect(Collectors.toList());
            return String.join(",", orderDishes);
        }
        return "";
    }

    //统计各个状态的订单数量
    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.statistics();
    }

    //接单
    @Override
    public void confirm(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    //拒单
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO){

        //获取订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        //判断订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //只要订单处于待接单状态，才可被拒单
        if(!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //!因为微信支付功能未实现，所以这里模拟拒单成功！

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    //商家取消订单
    @Override
    public void cancelById(OrdersCancelDTO ordersCancelDTO) {

        //!由于没做微信支付功能，所以这里模拟取消订单成功!

        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    //派送订单
    @Override
    public void delivery(Long id) {
        //获取订单
        Orders ordersDB = orderMapper.getById(id);

        //判断订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态，是否为待派送状态
        if(!ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //更新订单状态，派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    //完成订单
    @Override
    public void complete(Long id) {
        //获取订单
        Orders ordersDB = orderMapper.getById(id);

        //判断订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态，是否为派送中
        if(!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //更新订单状态，完成
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    //检查客户的收货地址是否超出配送范围
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

    //订单催单
    @Override
    public void reminder(Long id) {
        //根据ID查询订单
        Orders ordersDB = orderMapper.getById(id);

        //查询订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //向浏览器推送催单提醒
        Map map = new HashMap();
        map.put("type", 2); //2表示订单催单消息
        map.put("orderId", id);
        map.put("content", "订单号：" + ordersDB.getNumber());
        String json = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(json);
    }
}

