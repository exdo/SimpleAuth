package xyz.idaoteng.auth.login;

import xyz.idaoteng.auth.subject.UserInfo;

public interface OnlineUserRepository {
    String add(UserInfo userInfo);
    UserInfo get(String authToken);
    void remove(String authToken);
}
