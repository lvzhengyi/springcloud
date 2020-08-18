package com.jk.service;

import com.jk.mapper.UserMapper;
import com.jk.model.UserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
public class UserServiceImpl implements  UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    @RequestMapping("findUserList")
    public List<UserBean> findUserList() {
        return userMapper.findUserList();
    }

    @Override
    @RequestMapping("hello")
    public String hello(@RequestParam String name) {
        return "my name is "+name;
    }

    @Override
    @RequestMapping("saveOrder")
    public Object saveOrder(@RequestParam Integer userId, @RequestParam Integer productId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",111);
        map.put("orderName","火车");
        map.put("orderPrice","11111");
        map.put("userId",userId);
        map.put("productId",productId);
        return map;
    }
}
