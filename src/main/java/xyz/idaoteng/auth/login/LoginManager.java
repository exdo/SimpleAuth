package xyz.idaoteng.auth.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.custom.AuthTokenGetter;
import xyz.idaoteng.auth.custom.BasicLoginProcess;
import xyz.idaoteng.auth.exceptions.NoSuchUserException;
import xyz.idaoteng.auth.exceptions.NotSignedInException;
import xyz.idaoteng.auth.exceptions.SignedOutException;
import xyz.idaoteng.auth.exceptions.WrongPasswordException;
import xyz.idaoteng.auth.subject.UserInfo;
import xyz.idaoteng.auth.utils.HttpUtil;
import xyz.idaoteng.auth.utils.JwtUtil;

import javax.servlet.http.HttpServletRequest;

@Component
@Slf4j
public class LoginManager {
    private final BasicLoginProcess basicLoginProcess;
    private final BasicRepository repository;
    private AuthTokenGetter tokenGetter;
    private boolean hasTokenGetter = false;
    private int rememberMeValidTime = 0;
    private boolean enableRememberMe = false;

    @Autowired
    public LoginManager(BasicLoginProcess basicLoginProcess,
                        BasicRepository repository,
                        ApplicationContext applicationContext) {
        this.basicLoginProcess = basicLoginProcess;
        this.repository = repository;

        try {
            this.tokenGetter = applicationContext.getBean(AuthTokenGetter.class);
            this.hasTokenGetter = true;
        } catch (BeansException e) {
            this.tokenGetter = null;
        }
    }

    //设置记住我有效时间，默认0（不开启记住我），单位小时
    @Value("${rememberMe-time:0}")
    public void setRememberMeValidTime(int rememberMeValidTime) {
        if (rememberMeValidTime > 0) {
            log.info("记住我持续时间 = {} 小时", rememberMeValidTime);
            this.enableRememberMe = true;
            this.rememberMeValidTime = rememberMeValidTime;
        } else if (rememberMeValidTime < 0) {
            throw new RuntimeException("记住我持续时间不能为负数");
        } else {
            log.info("记住我功能未开启");
        }
    }

    //尝试登入
    public LoginResult tryLogin(String name, String uploadedPassword)
            throws NoSuchUserException, WrongPasswordException {
        UserInfo userInfo= basicLoginProcess.getUserInfo(name);
        if (userInfo == null) {
            throw new NoSuchUserException();
        }
        boolean passwordIsRight = basicLoginProcess.matchPasswords(userInfo, uploadedPassword);

        if (passwordIsRight) {
            String rememberMeToken = null;
            if (enableRememberMe) {
                long validMillis = rememberMeValidTime * 60 * 60 * 1000L;
                rememberMeToken = JwtUtil.rememberUserInfo(userInfo, validMillis);
            }

            String authToken = repository.add(userInfo);
            if (!hasTokenGetter) {
                repository.addTokenToResponse(authToken);
            }

            return new LoginResult().setSuccess(true)
                    .setAuthToken(authToken)
                    .setRememberMeToken(rememberMeToken)
                    .setUserInfo(userInfo);
        } else {
            throw new WrongPasswordException();
        }
    }

    //尝试获取authToken
    private String getAuthToken(HttpServletRequest req) {
        String authToken;
        if (hasTokenGetter) {
            authToken = tokenGetter.getAuthToken(req);
        } else {
            authToken = repository.tryToGetAuthToken(req);
        }
        return authToken;
    }

    //尝试登出
    public void tryLogout() throws NotSignedInException {
        HttpServletRequest req = HttpUtil.getRequest();
        String authToken = getAuthToken(req);
        if (authToken == null) {
            throw new NotSignedInException();
        } else {
            repository.remove(authToken);
            throw new SignedOutException();
        }
    }

    //记住我功能
    public LoginResult rememberMe(String rememberMeToken) throws NoSuchUserException, WrongPasswordException {
        UserInfo info = JwtUtil.fetchUserInfo(rememberMeToken);
        if (info == null) return new LoginResult().setSuccess(false);
        //如果用户被删除或更改了密码，则会再次抛出异常。
        return tryLogin(info.getName(), info.getPassword());
    }

    //获取当前请求的用户信息
    public UserInfo getCurrentUserInfo(HttpServletRequest req) {
        String authToken = getAuthToken(req);

        if (authToken == null) return null;

        return repository.get(authToken);
    }
}
