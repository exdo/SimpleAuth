package xyz.idaoteng.auth.login;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.idaoteng.auth.subject.UserInfo;

@Data
@Accessors(chain = true)
public class LoginResult {
    private Boolean success;
    private String authToken;
    private String rememberMeToken;
    private UserInfo userInfo;
}
