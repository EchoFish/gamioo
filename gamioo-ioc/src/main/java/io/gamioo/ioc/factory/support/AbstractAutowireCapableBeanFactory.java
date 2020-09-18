/*
 * Copyright 2015-2020 Gamioo Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gamioo.ioc.factory.support;


import io.gamioo.ioc.definition.BeanDefinition;
import io.gamioo.ioc.definition.FieldDefinition;
import io.gamioo.ioc.definition.MethodDefinition;

import java.util.List;


/**
 * some description
 *
 * @author Allen Jiang
 * @since 1.0.0
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    Object createBean(BeanDefinition beanDefinition) {
        return doCreateBean(beanDefinition);
    }

    Object doCreateBean(BeanDefinition beanDefinition) {
       String beanName=beanDefinition.getName();
        Object instance = createBeanInstance(beanDefinition);
        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = isSingletonCurrentlyInCreation(beanName);
        if(earlySingletonExposure){
            addSingletonFactory(beanName, () -> getEarlyBeanReference(instance));
        }
        //填充实例，这里完成了循环引用的问题
        populateBean(instance, beanDefinition);
        //调用类的初始化方法
        initializeBean(instance, beanDefinition);
        return instance;

    }

    @Override
    protected Object createBeanInstance(BeanDefinition beanDefinition) {
        Object ret = instantiateBean(beanDefinition);
        return ret;
    }


    /**
     * 初始化BEAN
     */
    protected Object instantiateBean(BeanDefinition beanDefinition) {
        Object ret = beanDefinition.newInstance();
        return ret;
    }

    /**
     * 填充bean，并做好互相填充的动作，是否需要在这里处理autowire的扫描呢？todo....
     */
    @Override
    protected void populateBean(Object instance, BeanDefinition beanDefinition) {

            List<FieldDefinition> list = beanDefinition.getAutowiredFieldDefinition();

            for (FieldDefinition e : list) {
                Object field = this.getBean(e.getName());
                e.inject(instance, field);
            }
    }

//    @Override
//    Object doCreateBean(BeanDefinition beanDefinition) throws Exception {
//        Object bean = beanDefinition.getBeanClass().newInstance();
//        beanDefinition.setBean(bean);
//        applyPropertyValues(bean, beanDefinition);
//        return bean;
//    }

//    void applyPropertyValues(BeanWrapper beanWrapper, BeanDefinition beanDefinition) throws Exception {
//        for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValues()) {
//            Field field = beanDefinition.getBeanClass().getDeclaredField(propertyValue.getName());
//            field.setAccessible(true);
//            field.set(beanWrapper.getWrappedInstance(), propertyValue.getValue());
//        }
//    }

    @Override
    protected void initializeBean(Object object, BeanDefinition beanDefinition) {

        this.invokeInitMethods(object, beanDefinition);
    }

    /**
     * 调用PostConstruct方法
     */
    private void invokeInitMethods(Object object, BeanDefinition beanDefinition) {
            MethodDefinition method = beanDefinition.getInitMethodDefinition();
            if (method!=null) {
                method.invoke(object);
            }
    }

}