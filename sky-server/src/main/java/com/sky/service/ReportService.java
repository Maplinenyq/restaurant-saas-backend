package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ReportService {

    // 营业额统计
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    // 用户统计
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    // 订单统计
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);
}
