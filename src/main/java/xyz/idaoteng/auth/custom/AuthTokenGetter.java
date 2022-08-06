package xyz.idaoteng.auth.custom;

import javax.servlet.http.HttpServletRequest;

public interface AuthTokenGetter {
    //自定义authToken的获取方式
    String getAuthToken(HttpServletRequest req);
}
