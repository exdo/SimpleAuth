package xyz.idaoteng.auth.exceptions;

public class NoSuchUserException extends Exception{
    public NoSuchUserException() {
        super("用户未注册");
    }
}
