package com.kabasiji.controller;

import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author huang_kangjie
 * @date 2019-01-04 9:21
 * @since 1.0.3
 **/
@MyController
@MyRequestMapping("/index")
public class IndexController {

     @MyRequestMapping("/hello")
     public void index(HttpServletRequest req, HttpServletResponse resp) throws IOException {
          resp.getWriter().write("welcome to my spring test!!!");
     }
}
