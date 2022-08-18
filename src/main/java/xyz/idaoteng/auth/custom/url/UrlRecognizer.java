package xyz.idaoteng.auth.custom.url;

import org.springframework.util.AntPathMatcher;

import java.util.*;

public class UrlRecognizer {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private String forLogin = "";
    private final ArrayList<String> publicUrl = new ArrayList<>();
    private final ArrayList<String> loginBoundUrl = new ArrayList<>();
    private final HashMap<String, List<String>> roleBoundUrl = new HashMap<>();
    private final HashMap<String, List<String>> permissionBoundUrl = new HashMap<>();
    private Boolean allowTheRest = false;

    //判断URL的类型，返回URL的要求
    public UrlRequirements judgement(String url) {
        if (pathMatcher.match(forLogin, url)) {
            return new UrlRequirements().setType(UrlType.FOR_LOGIN);
        }

        for (String pattern : publicUrl) {
            if (pathMatcher.match(pattern, url)) {
                return new UrlRequirements().setType(UrlType.PUBLIC);
            }
        }

        for (String pattern : loginBoundUrl) {
            if (pathMatcher.match(pattern, url)) {
                return new UrlRequirements().setType(UrlType.LOGIN_BOUND);
            }
        }

        Set<String> patterns = roleBoundUrl.keySet();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, url)) {
                return new UrlRequirements().setType(UrlType.ROLE_BOUND)
                        .setRequirements(roleBoundUrl.get(pattern));
            }
        }

        patterns = permissionBoundUrl.keySet();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, url)) {
                return new UrlRequirements().setType(UrlType.PERMISSION_BOUND)
                        .setRequirements(permissionBoundUrl.get(pattern));
            }
        }

        if (allowTheRest) {
            return new UrlRequirements().setType(UrlType.SAFE_FOR_NOW);
        }

        return new UrlRequirements().setType(UrlType.LOGIN_BOUND);
    }

    //设置请求登入的URL
    public UrlRecognizer setForLoginUrl(String url) {
        this.forLogin = url;
        return this;
    }

    //是否放行未配置的URL。如果为FALSE，则未配置的URL必须先登入才能被访问
    public UrlRecognizer allowTheRest(boolean allow) {
        this.allowTheRest = allow;
        return this;
    }

    //添加公共URL
    public UrlRecognizer addPublicUrl(List<String> publicUrl) {
        this.publicUrl.addAll(publicUrl);
        return this;
    }

    public UrlRecognizer addPublicUrl(String[] urlPatterns) {
        this.publicUrl.addAll(Arrays.asList(urlPatterns));
        return this;
    }

    public UrlRecognizer addPublicUrl(String urlPattern) {
        this.publicUrl.add(urlPattern);
        return this;
    }
    //----------------------------------------------------

    //添加已登入用户才可访问的URL
    public UrlRecognizer addLoginBoundUrl(List<String> loginBoundUrl) {
        this.loginBoundUrl.addAll(loginBoundUrl);
        return this;
    }

    public UrlRecognizer addLoginBoundUrl(String[] urlPatterns) {
        this.loginBoundUrl.addAll(Arrays.asList(urlPatterns));
        return this;
    }

    public UrlRecognizer addLoginBoundUrl(String urlPattern) {
        this.loginBoundUrl.add(urlPattern);
        return this;
    }
    //----------------------------------------------------

    //添加绑定了角色要求的URL
    private UrlRecognizer addRoleBoundUrl(Map<String, List<String>> roleBoundUrl) {
        this.roleBoundUrl.putAll(roleBoundUrl);
        return this;
    }

    private UrlRecognizer addRoleBoundUrl(String urlPattern, String[] roles, boolean needClone) {
        if (needClone) {
            this.roleBoundUrl.put(urlPattern, Arrays.asList(roles.clone()));
        } else {
            this.roleBoundUrl.put(urlPattern, Arrays.asList(roles));
        }
        return this;
    }

    public UrlRecognizer addRoleBoundUrl(String urlPattern, List<String> roles) {
        if (roles == null || roles.isEmpty()) return this;
        return addRoleBoundUrl(urlPattern, roles.toArray(new String[0]), false);
    }

    public UrlRecognizer addRoleBoundUrl(String urlPattern, String[] roles) {
        if (roles.length == 0) return this;
        return addRoleBoundUrl(urlPattern, roles, true);
    }

    //多个角色使用“,”隔开
    public UrlRecognizer addRoleBoundUrl(String urlPattern, String roles) {
        if (roles.length() == 0) return this;
        String[] roleArray = roles.split(",");
        return addRoleBoundUrl(urlPattern, roleArray, false);
    }
    //----------------------------------------------------

    //添加绑定了权限要求的URL
    private UrlRecognizer addPermissionBoundUrl(Map<String, List<String>> permissionBoundUrl) {
        this.permissionBoundUrl.putAll(permissionBoundUrl);
        return this;
    }

    private UrlRecognizer addPermissionBoundUrl(String urlPattern, String[] permissions, boolean needClone) {
        if (needClone) {
            this.permissionBoundUrl.put(urlPattern, Arrays.asList(permissions.clone()));
        } else {
            this.permissionBoundUrl.put(urlPattern, Arrays.asList(permissions));
        }
        return this;
    }

    public UrlRecognizer addPermissionBoundUrl(String urlPattern, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) return this;
        return addPermissionBoundUrl(urlPattern, permissions.toArray(new String[0]), false);
    }
    public UrlRecognizer addPermissionBoundUrl(String urlPattern, String[] permissions) {
        if (permissions.length == 0) return this;
        return addPermissionBoundUrl(urlPattern, permissions, true);
    }

    //多个权限使用“,”隔开
    public UrlRecognizer addPermissionBoundUrl(String urlPattern, String permissions) {
        if (permissions.length() == 0) return this;
        String[] permissionsArray = permissions.split(",");
        return addPermissionBoundUrl(urlPattern, permissionsArray, false);
    }

    public UrlRecognizer copy() {
        UrlRecognizer copy = new UrlRecognizer();
        return copy.setForLoginUrl(forLogin)
                .allowTheRest(allowTheRest)
                .addPermissionBoundUrl((Map<String, List<String>>) permissionBoundUrl.clone())
                .addRoleBoundUrl((Map<String, List<String>>) roleBoundUrl.clone())
                .addLoginBoundUrl((List<String>) loginBoundUrl.clone())
                .addPublicUrl((List<String>) publicUrl.clone());
    }
}
