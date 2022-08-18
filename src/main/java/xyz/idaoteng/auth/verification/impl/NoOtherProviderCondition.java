package xyz.idaoteng.auth.verification.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import xyz.idaoteng.auth.verification.VerificationCodeProvider;

public class NoOtherProviderCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        try {
            beanFactory.getBean(VerificationCodeProvider.class);
            return false;
        } catch (BeansException e) {
            return true;
        }
    }
}
