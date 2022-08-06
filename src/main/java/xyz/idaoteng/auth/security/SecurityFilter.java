package xyz.idaoteng.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.custom.CustomProcessor;
import xyz.idaoteng.auth.custom.url.UrlRecognizer;
import xyz.idaoteng.auth.custom.url.UrlRequirements;
import xyz.idaoteng.auth.exceptions.NotSignedInException;
import xyz.idaoteng.auth.login.LoginManager;
import xyz.idaoteng.auth.subject.UserInfo;
import xyz.idaoteng.auth.utils.JacksonUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@WebFilter(filterName = "SecurityManager", urlPatterns = "/**")
public class SecurityFilter implements Filter {
    private static final ThreadLocal<UserInfo> CURRENT_ONLINE_USER = new ThreadLocal<>();

    private final UrlRecognizer urlRecognizer;
    private final LoginManager loginManager;
    private CustomProcessor customProcessor;
    private boolean noCustomResponse = false;

    @Autowired
    public SecurityFilter(UrlRecognizer urlRecognizer,
                           LoginManager loginManager,
                           ApplicationContext applicationContext) {
        this.urlRecognizer = urlRecognizer.copy();
        this.loginManager = loginManager;
        try {
            customProcessor = applicationContext.getBean(CustomProcessor.class);
        } catch (BeansException e) {
            customProcessor = null;
            noCustomResponse = true;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String url = req.getRequestURI();
        UrlRequirements urlRequirements = urlRecognizer.judgement(url);
        UserInfo userInfo;
        switch (urlRequirements.getType()) {
            case FOR_LOGIN:
            case PUBLIC:
                userInfo = loginManager.getCurrentUserInfo(req);
                keepFilter(req, res, chain, userInfo);
            case LOGIN_BOUND:
                try {
                    //断言用户已经登入
                    userInfo = assertOnline(req, res);
                } catch (NotSignedInException e) {
                    //没登入则直接返回
                    return;
                }
                keepFilter(req, res, chain, userInfo);
                return;
            case ROLE_BOUND:
                try {
                    //断言用户已经登入
                    userInfo = assertOnline(req, res);
                } catch (NotSignedInException e) {
                    //没登入则直接返回
                    return;
                }

                //检查用户角色是否符合要求
                boolean isMeet = meetsRequirements(urlRequirements.getRequirements(), userInfo.getRoles());
                if (isMeet) {
                    keepFilter(req, res, chain, userInfo);
                } else {
                    if (noCustomResponse) {
                        res.sendError(403, "角色不符");
                    } else {
                        customProcessor.whenUserRoleMismatch(JacksonUtil.objectMapper(), userInfo, req, res);
                    }
                }
                return;
            case PERMISSION_BOUND:
                try {
                    //断言用户已经登入
                    userInfo = assertOnline(req, res);
                } catch (NotSignedInException e) {
                    //没登入则直接返回
                    return;
                }

                //检查用户权限是否符合要求
                boolean meet = meetsRequirements(urlRequirements.getRequirements(), userInfo.getPermissions());
                if (meet) {
                    keepFilter(req, res, chain, userInfo);
                } else {
                    if (noCustomResponse) {
                        res.sendError(403, "权限不足");
                    } else {
                        customProcessor.whenUserPermissionMismatch(JacksonUtil.objectMapper(), userInfo, req, res);
                    }
                }
                return;
            case SAFE_FOR_NOW:
                userInfo = loginManager.getCurrentUserInfo(req);
                keepFilter(req, res, chain, userInfo);
        }
    }

    private UserInfo assertOnline(HttpServletRequest req, HttpServletResponse res) throws NotSignedInException, IOException {
        UserInfo userInfo = loginManager.getCurrentUserInfo(req);
        if (userInfo == null) {
            if (noCustomResponse) {
                res.sendError(403, "请先登入");
            } else {
                customProcessor.whenUserNotSignedIn(JacksonUtil.objectMapper(), req, res);
            }
            throw new NotSignedInException();
        } else {
            return userInfo;
        }
    }

    private void keepFilter(HttpServletRequest req,
                            HttpServletResponse res,
                            FilterChain chain,
                            UserInfo userInfo) throws ServletException, IOException {
        CURRENT_ONLINE_USER.set(userInfo);
        chain.doFilter(req, res);
        CURRENT_ONLINE_USER.remove();
    }

    public static boolean meetsRequirements(List<String> requirements, List<String> selfConditions) {
        if (requirements.isEmpty()) {
            return true;
        }

        if (selfConditions == null || selfConditions.isEmpty()) {
            return false;
        }

        for (String requirement : requirements) {
            if (selfConditions.contains(requirement)) {
                return true;
            }
        }
        return false;
    }

    public static UserInfo getUserInfo() {
        return CURRENT_ONLINE_USER.get();
    }
}
