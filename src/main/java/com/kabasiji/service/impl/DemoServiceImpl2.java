package com.kabasiji.service.impl;

import com.kabasiji.service.IDemoService;
import com.springframework.annotation.MyService;

/**
 * @author huang_kangjie
 * @date 2019-01-04 9:02
 * @since 1.0.3
 **/
@MyService("myDemoServiceImpl2")
public class DemoServiceImpl2 implements IDemoService {

     @Override
     public void print(String name) {
          System.out.println(">>>>>>>>>>>>> DemoServiceImpl2 参数：" + name);
     }
}