package com.nyq.aspect;

import com.nyq.annotation.AutoFill;
import com.nyq.constant.AutoFillConstant;
import com.nyq.context.BaseContext;
import com.nyq.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    //切入点
    @Pointcut("execution(* com.nyq.mapper.*.*(..)) && @annotation(com.nyq.annotation.AutoFill)")
    public void autoFillPointcut(){}

    //前置通知
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");

        //获取当前被拦截到的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的数据库操作类型
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法参数---实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }
        Object entry = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的数据类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT){
            //为4个公共字段赋值
            try {
                Method setCreateTime =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为对象进行赋值
                setCreateTime.invoke(entry, now);
                setCreateUser.invoke(entry, currentId);
                setUpdateTime.invoke(entry, now);
                setUpdateUser.invoke(entry, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE){
            //为两个公共字段赋值
            try {
                Method setUpdateTime =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser =entry.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象进行赋值
                setUpdateTime.invoke(entry, now);
                setUpdateUser.invoke(entry, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
