package xyz.idaoteng.auth.subject;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserInfo {
    protected String id;//用户ID
    protected String name;//用户名
    protected String password;//用户密码
    protected String salt;//盐
    protected List<String> roles;//用户的角色
    protected List<String> permissions;//用户的权限
}
