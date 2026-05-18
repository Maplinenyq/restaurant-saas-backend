package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ReportService {

    // 营业额统计
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
}
