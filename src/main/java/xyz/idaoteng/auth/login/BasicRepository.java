package xyz.idaoteng.auth.login;

import lombok.extern.slf4j.Slf4j;
import xyz.idaoteng.auth.utils.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public abstract class BasicRepository implements OnlineUserRepository{
    protected int maxOnlineTime = 600;

    public BasicRepository(int maxOnlineTime) {
        //设置最大在线时间，默认600分钟。单位分钟。
        if (maxOnlineTime ==  0) {
            log.warn("未主动设置最大在线时间，默认600分钟（10小时）");
        } else if (maxOnlineTime < 0) {
            throw new RuntimeException("最大在线时间不能为负数");
        } else {
            log.info("最大在线时间 = {} 分钟", maxOnlineTime);
            this.maxOnlineTime = maxOnlineTime;
        }
    }

    //将请求头添加到authToken
    public void addTokenToResponse(String authToken) {
        HttpServletResponse response = HttpUtil.getResponse();
        //使前端的axios等可以访问该请求头
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
        response.addHeader("Authorization", authToken);
    }

    //从请求头获取authToken
    public String tryToGetAuthToken(HttpServletRequest req) {
        return req.getHeader("Authorization");
    }
}
