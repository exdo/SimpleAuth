package xyz.idaoteng.auth.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.annotation.BindPermissions;
import xyz.idaoteng.auth.annotation.BindRoles;
import xyz.idaoteng.auth.exceptions.NotSignedInException;
import xyz.idaoteng.auth.exceptions.PermissionMismatchException;
import xyz.idaoteng.auth.exceptions.RoleMismatchException;
import xyz.idaoteng.auth.subject.UserInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
@Aspect
public class AuthAspect {

    @Before("@annotation(xyz.idaoteng.auth.annotation.LoginRequired)")
    public void checkOnlineStatus() throws NotSignedInException {
        UserInfo userInfo = SecurityFilter.getUserInfo();
        if (userInfo == null) {
            throw new NotSignedInException();
        }
    }

    @Before("@annotation(xyz.idaoteng.auth.annotation.BindRoles)")
    public void checkRole(JoinPoint joinPoint) throws NotSignedInException, RoleMismatchException {
        UserInfo userInfo = SecurityFilter.getUserInfo();
        if (userInfo == null) {
            throw new NotSignedInException();
        }

        MethodSignature sign = (MethodSignature)joinPoint.getSignature();
        Method method = sign.getMethod();
        BindRoles bindRoles = method.getAnnotation(BindRoles.class);
        String[] roles = bindRoles.value();
        List<String> requirements = Arrays.asList(roles);
        boolean meet = SecurityFilter.meetsRequirements(requirements, userInfo.getRoles());
        if (!meet) {
            throw new RoleMismatchException();
        }
    }

    @Before("@annotation(xyz.idaoteng.auth.annotation.BindPermissions)")
    public void checkPermission(JoinPoint joinPoint) throws NotSignedInException, PermissionMismatchException {
        UserInfo userInfo = SecurityFilter.getUserInfo();
        if (userInfo == null) {
            throw new NotSignedInException();
        }

        MethodSignature sign = (MethodSignature)joinPoint.getSignature();
        Method method = sign.getMethod();
        BindPermissions bindPermissions = method.getAnnotation(BindPermissions.class);
        String[] permissions = bindPermissions.value();
        List<String> requirements = Arrays.asList(permissions);
        boolean meet = SecurityFilter.meetsRequirements(requirements, userInfo.getPermissions());
        if (!meet) {
            throw new PermissionMismatchException();
        }
    }
}
