package xyz.idaoteng.auth.custom;

import xyz.idaoteng.auth.subject.UserInfo;

public interface BasicLoginProcess {
    /**
     * 检查用户是否已经注册
     * @param name 用户登入时上传的用户名
     * @return 此次登入用户的用户信息
     */
    UserInfo getUserInfo(String name);

    /**
     * 检验密码是否正确
     * @param userInfo 此次登入用户的用户信息
     * @param uploadedPassword 用户登入时上传的密码
     * @return 密码是否匹配
     */
    boolean matchPasswords(UserInfo userInfo, String uploadedPassword);
}
