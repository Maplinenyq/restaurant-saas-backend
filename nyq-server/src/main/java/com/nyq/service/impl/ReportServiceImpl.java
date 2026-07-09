package com.nyq.service.impl;

import com.nyq.dto.GoodsSalesDTO;
import com.nyq.entity.Orders;
import com.nyq.mapper.OrderMapper;
import com.nyq.mapper.UserMapper;
import com.nyq.service.ReportService;
import com.nyq.service.WorkspaceService;
import com.nyq.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    // 营业额统计
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        // 日期列表
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();

        //根据时间查询营业额
        for(LocalDate date : dateList){

            //筛选已完成的订单
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    // 用户统计
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        // 日期列表
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        // 根据时间查询新增用户数量
        for(LocalDate date : dateList){

            // 细化时间对齐格式
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);

            // 统计总用户数量
            Integer totalUsers = userMapper.countByMap(map);

            // 统计每天新增用户数量
            map.put("begin", beginTime);
            Integer newUsers = userMapper.countByMap(map);

            newUsers = newUsers == null ? 0 : newUsers;
            totalUsers = totalUsers == null ? 0 : totalUsers;
            newUserList.add(newUsers);
            totalUserList.add(totalUsers);
        }
        // 封装返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    // 订单统计
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        // 日期列表
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            // 日期计算
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        double orderCompletionRate = 0.0;

        for(LocalDate date : dateList) {
            // 获取时间对齐格式
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer orderCount = getOrderCount(beginTime, endTime, null); //获取每日订单数量
            orderCountList.add(orderCount);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED); //获取每日有效订单数量
            validOrderCountList.add(validOrderCount);

        }
        // 计算订单完成率
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    // 营业额排名
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.salesTop10Count(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).toList();
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).toList();

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据，获取营业数据 -- 最近30天的数据
        LocalDate begin = LocalDate.now().plusDays(-30);
        LocalDate end = LocalDate.now().plusDays(-1);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        //2. 通过POI 将数据写入到Excel中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {

            //基于模版创建文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+ begin + "~" + end);

            //获得第四行的行数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第五行的行数据
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数据
                workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //获得某一行
                XSSFRow row1 = sheet.getRow(i + 7);
                row1.getCell(1).setCellValue(date.toString());
                row1.getCell(2).setCellValue(businessDataVO.getTurnover());
                row1.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row1.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row1.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row1.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //释放资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取订单数量
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}
