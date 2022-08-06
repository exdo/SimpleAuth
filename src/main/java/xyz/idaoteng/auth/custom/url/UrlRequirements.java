package xyz.idaoteng.auth.custom.url;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
//记录URL所需的权限或角色
public class UrlRequirements {
    private UrlType type;
    private List<String> requirements;
}
