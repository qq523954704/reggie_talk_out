package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //URL匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;

        //1.获得本次请求的URL
        String requestURI = request.getRequestURI();

        log.info("拦截请求: {}",requestURI);

        //2.检查本次请求路径是否需要处理
          //2.1 不用处理的URL
        String[] urls = {
               "/employee/login", //员工登入请求放行
               "/employee/logout",
                "/backend/**",
                "/front/**",
                "common/**",
                "/user/sendMsg",// 移动端发送短信
                "/user/login", //用户移动端登入
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"

        };

        boolean check = check(urls, requestURI);
        //3.不用处理的URL不直接放行
        if (check){
            filterChain.doFilter(request,response);
            log.info("放行资源");
            return;
        }

        //4.判断是否登入。登入放行
            //1判断员工是否登入
        Object employee = request.getSession().getAttribute("employee");

        if (employee!=null){
            BaseContext.setCurrentId((Long) employee);//当前登入用户的线程存入Id到ThreadLocal的副本中
            filterChain.doFilter(request,response);
            log.info("Id:{}已登入",employee);

            return;
        }

            //2判断用户是否登入
        Object user = request.getSession().getAttribute("user");
        if (user!=null){
            BaseContext.setCurrentId((Long) user);//当前登入用户的线程存入Id到ThreadLocal的副本中
            filterChain.doFilter(request,response);
            log.info("Id:{}已登入",user);

            return;
        }


        //5.未登入，向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("未登入拦截");

    }

    /**
     * URL检查方法
     */
    public boolean check(String[] urls,String requestURL){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match){
                return true;
            }
        }
        return false;

    }

}
