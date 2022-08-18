package xyz.idaoteng.auth.exceptions.handler;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.idaoteng.auth.custom.CustomProcessor;
import xyz.idaoteng.auth.exceptions.NotSignedInException;
import xyz.idaoteng.auth.exceptions.PermissionMismatchException;
import xyz.idaoteng.auth.exceptions.RoleMismatchException;
import xyz.idaoteng.auth.exceptions.SignedOutException;
import xyz.idaoteng.auth.security.SecurityFilter;
import xyz.idaoteng.auth.subject.UserInfo;
import xyz.idaoteng.auth.utils.HttpUtil;
import xyz.idaoteng.auth.utils.JacksonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class NotAuthExceptionHandler {
    private CustomProcessor customProcessor;
    private boolean noCustomResponse = false;

    @Autowired
    public NotAuthExceptionHandler(ApplicationContext applicationContext) {
        try {
            customProcessor = applicationContext.getBean(CustomProcessor.class);
        } catch (BeansException e) {
            customProcessor = null;
            noCustomResponse = true;
        }
    }

    @SneakyThrows
    @ExceptionHandler(NotSignedInException.class)
    @ResponseBody
    public void notSignedInExceptionHandler() {
        HttpServletRequest req = HttpUtil.getRequest();
        HttpServletResponse res = HttpUtil.getResponse();

        if (noCustomResponse) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":403,\"message\":\"未登入\"}");
        } else {
            customProcessor.whenUserNotSignedIn(JacksonUtil.objectMapper(), req, res);
        }
    }

    @SneakyThrows
    @ExceptionHandler(RoleMismatchException.class)
    @ResponseBody
    public void roleMismatchExceptionHandler() {
        HttpServletRequest req = HttpUtil.getRequest();
        HttpServletResponse res = HttpUtil.getResponse();
        UserInfo userInfo = SecurityFilter.getUserInfo();

        if (noCustomResponse) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":403,\"message\":\"角色不符\"}");
        } else {
            customProcessor.whenUserRoleMismatch(JacksonUtil.objectMapper(), userInfo, req, res);
        }
    }

    @SneakyThrows
    @ExceptionHandler(PermissionMismatchException.class)
    @ResponseBody
    public void permissionMismatchExceptionHandler() {
        HttpServletRequest req = HttpUtil.getRequest();
        HttpServletResponse res = HttpUtil.getResponse();
        UserInfo userInfo = SecurityFilter.getUserInfo();

        if (noCustomResponse) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
        } else {
            customProcessor.whenUserRoleMismatch(JacksonUtil.objectMapper(), userInfo, req, res);
        }
    }

    @SneakyThrows
    @ExceptionHandler(SignedOutException.class)
    @ResponseBody
    public void signedOutExceptionHandler() {
        HttpServletRequest req = HttpUtil.getRequest();
        HttpServletResponse res = HttpUtil.getResponse();
        UserInfo userInfo = SecurityFilter.getUserInfo();

        if (noCustomResponse) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":200,\"message\":\"已登出\"}");
        } else {
            customProcessor.whenUserSignedOut(JacksonUtil.objectMapper(), userInfo, req, res);
        }
    }
}
