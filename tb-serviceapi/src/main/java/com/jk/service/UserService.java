package com.jk.service;

import com.jk.model.UserBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public interface UserService {

    @RequestMapping("findUserList")
    List<UserBean> findUserList();

    @RequestMapping("hello")
    String hello(@RequestParam String name);

    @RequestMapping("saveOrder")
    Object saveOrder(@RequestParam Integer userId, @RequestParam Integer productId);
}
