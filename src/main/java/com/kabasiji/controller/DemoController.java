package com.kabasiji.controller;

import com.kabasiji.service.IDemoService;
import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRequestMapping;
import com.springframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author huang_kangjie
 * @date 2018-12-28 19:20
 * @since 1.0.3
 **/
@MyController
@MyRequestMapping("/test")
public class DemoController {

     @MyAutowired
     private IDemoService demoService;

     @MyRequestMapping("/index")
     public void test(HttpServletResponse response,
                      @MyRequestParam String name) throws IOException {
          demoService.print(name);
          response.getWriter().write("请求参数 name : " + name);
     }


}
