package com.cusob.ebooks.auth;

import com.cusob.ebooks.annotations.RequireLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component

public class RequireLoginInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 检查方法或类上是否有 @RequireLogin 注解
            RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
            if (requireLogin == null) {
                requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
            }

            // 如果存在 @RequireLogin 注解，检查是否登录
            if (requireLogin != null) {
                // 检查登录状态（可以根据具体的登录实现，通常是检查session或token）
                String token = request.getHeader("token");
                Object value = redisTemplate.opsForValue().get(token);

                if (value == null) {
                    response.sendRedirect("/Ebooks/login"); // 未登录，重定向到登录页面
                    return false; // 拦截请求
                }
            }
        }
        return true; // 继续处理请求
    }



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
