package com.kabasiji.service.impl;

import com.kabasiji.service.IDemoService;
import com.springframework.annotation.MyService;

/**
 *
 * @author huang_kangjie
 * @date 2018-12-28 19:19
 * @since 1.0.3
 **/
@MyService
public class DemoServiceImpl implements IDemoService {

     @Override
     public void print(String name) {
          System.out.println(">>>>>>>>>>>>> 参数：" + name);
     }
}
