package com.jk.service;

import com.jk.model.UserBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Component
@RequestMapping("error")
public class UserServiceFallback implements UserServiceFeign {

    @Override
    public List<UserBean> findUserList() {
        System.out.println("熔断处理");
        return null;
    }

    @Override
    public String hello(String name) {
        System.out.println("进入hello方法 ");
        return "请求失败 请检查手机网络";
    }

    @Override
    public Object saveOrder(Integer userId, Integer productId) {
        System.out.println("订单降级 ");
        return "订单降级啦，controller进不去";
    }
}
