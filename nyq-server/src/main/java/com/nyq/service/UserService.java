package com.nyq.service;

import com.nyq.dto.UserLoginDTO;
import com.nyq.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    //微信登录
    User login(UserLoginDTO userLoginDTO);
}
