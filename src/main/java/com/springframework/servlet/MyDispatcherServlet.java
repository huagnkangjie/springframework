package com.springframework.servlet;

import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRequestMapping;
import com.springframework.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author huang_kangjie
 * @date 2018-12-28 20:25
 * @since 1.0.3
 **/
public class MyDispatcherServlet extends HttpServlet {

     //必须和web.xml里面的init-param -> param-name 第一个参数相同
     private static final String LOCATION = "contextConfigLocation";

     //保存所有的配置文件
     private Properties pro = new Properties();

     //保存所有需要扫描的文件
     private List<String> classNames = new ArrayList<>();

     //核心IOC容器，保存所有初始化的bean
     private Map<String, Object> ioc = new HashMap<>();

     //保存所有的url和方法的映射关系
     private Map<String, Method> handlerMappering = new HashMap<>();

     public MyDispatcherServlet() {
          super();
     }

     @Override
     public void init(ServletConfig config) throws ServletException {

          //1、加载配置文件
          doLoadConfig(config.getInitParameter(LOCATION));

          //2、扫描所有相关文件的类（也就是满足我们需要所有的注解的类）
          doScanner(this.pro.getProperty("scanPackage"));

          //3、初始化所有相关的类（被注解标识的类）,保存在IOC容器中
          doInstance();

          //4、依赖注入，所有需要注入的类，自动赋值
          doAutowired();

          //5、初始化handerMapping
          initHandlerMapping();

          System.out.println(">>>>>>>>>>>>>>>> spring init scuccess!!!");

     }

     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
          this.doDispatch(req, resp);
     }

     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
          this.doDispatch(req, resp);
     }

     /**
      * 初始化配置文件
      * @param initParameter  web.xmlp配置加载的参数名称
      */
     private void doLoadConfig(String initParameter) {
          InputStream fis = null;
          try {
               fis = this.getClass().getClassLoader().getResourceAsStream(initParameter);
               this.pro.load(fis);
          } catch (Exception e) {
               System.err.print("加载配置文件出错......");
               e.printStackTrace();
          } finally {
               try {
                    if(null != fis) {
                         fis.close();
                    }
               }catch (Exception e) {
                    System.err.print("加载配置文件关闭流出错......");
                    e.printStackTrace();
               }
          }

     }

     /**
      * 利用递归，根据配置文件配置的包地址，扫描所有需要扫描的文件
      *
      * @param scanPackage 需要扫描的包，包含子包
      */
     private void doScanner(String scanPackage) {
          //将所有的包路径转换成文件路径
          //com.kabasiji -> com/kabasiji
          URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
          File dir = new File(url.getFile());
          for(File file : dir.listFiles()) {
               //如果是文件夹，则递归
               if(file.isDirectory()) {
                    doScanner(scanPackage + "." + file.getName());
               } else {
                    try {
                         this.classNames.add(scanPackage + "." + file.getName().replaceAll(".class", "").trim());
                    } catch (Exception e) {
                         System.err.print("扫描包文件出错");
                         e.printStackTrace();
                    }
               }
          }
     }

     /**
      * 首字母小写
      * @param str
      * @return
      */
     private String lowerFirstCase(String str) {
          if(str == null || str.trim().length() == 0) {
               return "";
          }
          return str.substring(0, 1) + str.substring(1, str.length());

          //利用asic码转换
          //char [] chars =  str.toCharArray();
          //chars[0] += 32;
          //return String.valueOf(chars);
     }

     /**
      * 初始化所有相关的类（被注解标识的类）,保存在IOC容器中
      */
     private void doInstance() {
          if(this.classNames.size() == 0) {
               return;
          }
          try {
               for(String className : this.classNames) {
                    Class<?> clazz = Class.forName(className);
                    //处理controller注解
                    if(clazz.isAnnotationPresent(MyController.class)) {
                         //默认将首字母小写作为beanName
                         String beanName = this.lowerFirstCase(clazz.getSimpleName());
                         //实例化bean，并根据beanName存放bean的实例化对象
                         this.ioc.put(beanName, clazz.newInstance());
                    }
                    //处理service注解
                    if(clazz.isAnnotationPresent(MyService.class)) {
                         MyService service = clazz.getAnnotation(MyService.class);
                         //根据service的注解获取value值
                         String beanName = service.value();
                         //如果用户设置了别名，则使用用户设置的别名
                         if(!"".equals(beanName)){
                              //检查是否重复使用beanName
                              this.checkBean(beanName);
                              this.ioc.put(beanName, clazz.newInstance());
                              continue;
                         }
                         //如果用户没设置别名，则按照该接口类型创建一个实例
                         //实例化接口所有的实现类
                         Class<?>[] interfaces = clazz.getInterfaces();
                         for(Class<?> i : interfaces) {
                              this.checkBean(beanName);
                              this.ioc.put(i.getName(), clazz.newInstance());
                         }
                    }


               }
          } catch (Exception e) {
               System.err.print("实例化被扫描文件出错");
               e.printStackTrace();
          }


     }

     private void checkBean(String beanName) throws Exception {
          if(ioc.get(beanName) != null) {
               throw new Exception(beanName + "已经被使用");
          }
     }

     /**
      * 遍历ioc容器，处理所有的实例
      * 遍历单个实例有AutoWired注解的属性，并给该属性赋值，从ioc容器里面取出对应的实例
      */
     private void doAutowired() {
          if(this.ioc.isEmpty()) {
               return;
          }

          for (Map.Entry<String, Object> entry : ioc.entrySet()) {
               //获取实例的所有属性
               Field[] fields = entry.getValue().getClass().getDeclaredFields();
               for(Field field : fields) {
                    //如果没有被Autowired注解，则跳过
                    if(!field.isAnnotationPresent(MyAutowired.class)) {
                         continue;
                    }
                    MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                    String beanName = autowired.value();
                    if("".equals(beanName)) {
                         //没有设置别名，则根据属性获取名字
                         beanName = field.getType().getName();
                    }
                    //必须设置为true，才能对属性进行赋值
                    field.setAccessible(true);
                    try {
                         field.set(entry.getValue(), ioc.get(beanName));
                    } catch (Exception e) {
                         System.err.print( entry.getValue() + "自动注入出错");
                         e.printStackTrace();
                    }
               }

          }

     }

     /**
      * 初始化url与method的适配
      */
     private void initHandlerMapping() {
          if(this.ioc.isEmpty()) {
               return;
          }
          for (Map.Entry<String, Object> entry : this.ioc.entrySet()) {
               Class<?> clazz = entry.getValue().getClass();
               //没有被Controller注解修饰
               if(!clazz.isAnnotationPresent(MyController.class)) {
                    continue;
               }
               String baseUrl = "";

               //获取controller的url
               if(clazz.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = requestMapping.value();
               }
               //获取该类的所有方法
               Method[] methods = clazz.getMethods();
               for(Method method : methods) {
                    if(!method.isAnnotationPresent(MyRequestMapping.class)) {
                         continue;
                    }
                    MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                    String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                    this.handlerMappering.put(url, method);
                    System.out.println("requestmapping handler: " + url);

               }


          }
     }

     /**
      * 根据url解析找到对应的method，通过反射执行
      * @param req
      * @param resp
      */
     private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
          if(this.handlerMappering.isEmpty()) {
               return;
          }

          String url = req.getRequestURI();
          String contextPath = req.getContextPath();
          url = url.replace(contextPath, "").replaceAll("/+", "/");

          if(!this.handlerMappering.containsKey(url)) {
               resp.getWriter().write("404 Not Found!");
               return;
          }
          Method method = this.handlerMappering.get(url);
          //获取请求的参数
          Map<String, String[]> parameterMap = req.getParameterMap();
          //获取方法的参数列表
          Class<?>[] parameterTypes = method.getParameterTypes();
          //保存参数值
          Object[] paramValues = new Object[parameterTypes.length];
          //根据参数列表遍历参数
          for( int i = 0; i < parameterTypes.length; i++) {
               Class parameterType = parameterTypes[i];
               if(parameterType == HttpServletRequest.class) {
                    paramValues[i] = req;
                    continue;
               } else if(parameterType == HttpServletResponse.class) {
                    paramValues[i] = resp;
                    continue;
               } else if(parameterType == String.class) {
                    for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                         String value = Arrays.toString(param.getValue())
                                 .replaceAll("\\[|\\]", "")
                                 .replaceAll("\\s", "");
                         paramValues[i] = value;
                    }
               }
          }
          try {
               String beanName = this.lowerFirstCase(method.getDeclaringClass().getSimpleName());
               //反射
               method.invoke(this.ioc.get(beanName), paramValues);
          } catch (Exception e) {
               System.err.println("invoke 出错");
               e.printStackTrace();
          }
     }

}
