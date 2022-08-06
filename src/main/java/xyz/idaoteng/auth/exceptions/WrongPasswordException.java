package xyz.idaoteng.auth.exceptions;

public class WrongPasswordException extends Exception{
    public WrongPasswordException() {
        super("用户名或密码不正确");
    }
}
