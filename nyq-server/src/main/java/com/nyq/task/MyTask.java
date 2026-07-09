package com.nyq.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 自定义任务类
@Component
@Slf4j
public class MyTask {

    //定时任务
   // @Scheduled(cron = "0/5 * * * * ? ")
    public void task1() {
        log.info("task1: {}", System.currentTimeMillis());
    }

}
