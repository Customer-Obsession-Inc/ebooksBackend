package com.cusob.ebooks.auth;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



public class AuthContext {

    private static ThreadLocal<Long> userId = new ThreadLocal<>();


    public static Long getUserId(){
//        if (userId.get() == null) {
//            extractUserInfoFromRequest();
//        }
        return userId.get();
    }
    private static void extractUserInfoFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = (HttpServletRequest) attributes.getRequest();

            // 从请求头中获取 UserId 和 CompanyId
            String userIdStr = request.getHeader("UserId");
            String companyIdStr = request.getHeader("CompanyId");

            // 打印调试信息
            System.out.println("HttpServletRequest: " + request);
            System.out.println("UserId from request header: " + userIdStr);
            System.out.println("CompanyId from request header: " + companyIdStr);

            // 解析并设置 UserId 和 CompanyId
            if (userIdStr != null) {
                try {
                    userId.set(Long.parseLong(userIdStr));
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse UserId: " + userIdStr);
                    e.printStackTrace();
                }
            }

            System.out.println("No request attributes available. This method is being called outside of a web request context.");
        }
    }

    public static void setUserId(Long _userId){
        userId.set(_userId);
    }

}
