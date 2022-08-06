package xyz.idaoteng.auth.login.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import xyz.idaoteng.auth.login.OnlineUserRepository;

public class NoOtherRepository implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        try {
            beanFactory.getBean(OnlineUserRepository.class);
            return false;
        } catch (BeansException e) {
            return true;
        }
    }
}
