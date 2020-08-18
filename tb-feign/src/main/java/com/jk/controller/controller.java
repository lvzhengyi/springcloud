package com.jk.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jk.model.UserBean;
import com.jk.service.UserServiceFeign;
import com.jk.utils.*;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
@RequestMapping("user")
public class controller {

    @Resource
    private UserServiceFeign userServiceFeign;

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("saveOrder")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "saveOrderFail")
    public Object saveOrder(Integer userId, Integer productId, HttpServletRequest request) throws InterruptedException {

        return userServiceFeign.saveOrder(userId,productId);
    }

    private Object saveOrderFail(Integer userId,Integer productId,HttpServletRequest request){
        System.out.println("controller中的降级方法");

        String string = (String) redisUtil.get(Constant.SAVE_ORDER);
        String remoteAddr = request.getRemoteAddr();
        //给开发处理
        new Thread( ()->{
            if (StringUtil.isEmpty(string)) {
                System.out.println("紧急短信，用户下单失败，请离开查找原因,ip地址是="+remoteAddr);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Constant.SERVER_URL);
                String curTime = String.valueOf((new Date()).getTime() / 1000L);
                /*
                 * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
                 */
                String checkSum = CheckSumBuilder.getCheckSum(Constant.APP_SECRET, Constant.NONCE, curTime);

                // 设置请求的header
                httpPost.addHeader("AppKey", Constant.APP_KEY);
                httpPost.addHeader("Nonce", Constant.NONCE);
                httpPost.addHeader("CurTime", curTime);
                httpPost.addHeader("CheckSum", checkSum);
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

                // 设置请求的的参数，requestBody参数
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                /*
                 * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
                 * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
                 * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
                 */
                nvps.add(new BasicNameValuePair("templateid", Constant.TEMPLATEID));
                nvps.add(new BasicNameValuePair("mobile", Constant.MOBILE));
                nvps.add(new BasicNameValuePair("codeLen", Constant.CODELEN));

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // 执行请求
                HttpResponse response = null;
                try {
                    response = httpClient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                 * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
                 * 2.具体的code有问题的可以参考官网的Code状态表
                 */
                try {
                    System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //发送验证码成功只存在五分钟
                redisUtil.set(Constant.MOBILE,Constant.NONCE,60);
                //将用户发送状态锁定60秒
                //发送一个http请求，调用短信服务 TODO
                redisUtil.set(Constant.SAVE_ORDER,"保存失败",60);
            }else{
                System.out.println("已经发送过短信，20秒内不重复发送");
            }
        }).start();
        //反馈给用户看的
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("code", -1);
        msg.put("msg", "抢购人数太多，您被挤出来了，稍等重试");
        return msg;
    }

    @RequestMapping("hello")
    @ResponseBody
    public String hello(String name){

        return userServiceFeign.hello(name);
    }

    @RequestMapping("findUserList")
    @ResponseBody
    public List<UserBean> findUserList(){
        List<UserBean> userBeanList = (List<UserBean>)redisUtil.get(Constant.FIND_USER_LIST);
        if(userBeanList==null || userBeanList.size()<=0 || userBeanList.isEmpty()){
            userBeanList = userServiceFeign.findUserList();
            redisUtil.set(Constant.FIND_USER_LIST,userBeanList,60);
        }
        return userBeanList;
    }

}
