package com.nyq.service;

import com.nyq.dto.EmployeeDTO;
import com.nyq.dto.EmployeeLoginDTO;
import com.nyq.dto.EmployeePageQueryDTO;
import com.nyq.entity.Employee;
import com.nyq.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQuery
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQuery);

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}
