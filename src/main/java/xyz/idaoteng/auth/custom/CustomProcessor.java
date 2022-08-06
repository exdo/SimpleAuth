package xyz.idaoteng.auth.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.idaoteng.auth.subject.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CustomProcessor {
    /**
     * 在用户未满足登入要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    void whenUserNotSignedIn(ObjectMapper objectMapper,
                             HttpServletRequest request,
                             HttpServletResponse response);

    /**
     * 在用户的角色不符要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    void whenUserRoleMismatch(ObjectMapper objectMapper,
                                UserInfo userInfo,
                                HttpServletRequest request,
                                HttpServletResponse response);

    /**
     * 在用户的权限不符要求时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    void whenUserPermissionMismatch(ObjectMapper objectMapper,
                                      UserInfo userInfo,
                                      HttpServletRequest request,
                                      HttpServletResponse response);

    /**
     * 在用户的登出时的自定义处理方式
     * @param objectMapper 供使用的JSON工具
     * @param userInfo 用户信息
     * @param request 用户提交的请求
     * @param response 返回给用户的响应
     */
    void whenUserSignedOut(ObjectMapper objectMapper,
                           UserInfo userInfo,
                           HttpServletRequest request,
                           HttpServletResponse response);
}
