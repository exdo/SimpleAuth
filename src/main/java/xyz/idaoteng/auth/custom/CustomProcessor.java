package xyz.idaoteng.auth.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.idaoteng.auth.subject.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CustomProcessor {
    /**
     * 在用户未满足登入要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    default void whenUserNotSignedIn(ObjectMapper objectMapper,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":403,\"message\":\"未登入\"}");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 在用户的角色不符要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    default void whenUserRoleMismatch(ObjectMapper objectMapper,
                                UserInfo userInfo,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":403,\"message\":\"角色不符\"}");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 在用户的权限不符要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    default void whenUserPermissionMismatch(ObjectMapper objectMapper,
                                      UserInfo userInfo,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 在用户的登出时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    default void whenUserSignedOut(ObjectMapper objectMapper,
                           UserInfo userInfo,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":200,\"message\":\"已登出\"}");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
