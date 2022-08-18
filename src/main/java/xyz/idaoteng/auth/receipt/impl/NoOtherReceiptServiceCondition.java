package xyz.idaoteng.auth.receipt.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import xyz.idaoteng.auth.receipt.ReceiptService;

public class NoOtherReceiptServiceCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        try {
            beanFactory.getBean(ReceiptService.class);
            return false;
        } catch (BeansException e) {
            return true;
        }
    }
}
