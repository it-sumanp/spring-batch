/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.batch.core.jsr.configuration.xml;

import java.util.HashMap;

import org.springframework.batch.core.jsr.configuration.support.BatchPropertyBeanPostProcessor;
import org.springframework.batch.core.jsr.configuration.support.JsrAutowiredAnnotationBeanPostProcessor;
import org.springframework.batch.core.jsr.configuration.support.JsrBeanScopeBeanFactoryPostProcessor;
import org.springframework.batch.core.jsr.configuration.support.ThreadLocalClassloaderBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.AnnotationConfigUtils;

/**
 * Utility methods used in parsing of the JSR-352 batch namespace and related helpers.
 *
 * @author Michael Minella
 * @author Chris Schaefer
 * @since 3.0
 */
class JsrNamespaceUtils {
	private static final String JOB_PROPERTIES_BEAN_NAME = "jobProperties";
	private static final String BATCH_PROPERTY_POST_PROCESSOR_BEAN_NAME = "batchPropertyPostProcessor";
	private static final String THREAD_LOCAL_CLASS_LOADER_BEAN_POST_PROCESSOR_BEAN_NAME = "threadLocalClassloaderBeanPostProcessor";
	private static final String BEAN_SCOPE_POST_PROCESSOR_BEAN_NAME = "beanScopeBeanPostProcessor";

	static void autoregisterJsrBeansForNamespace(ParserContext parserContext) {
		autoRegisterJobProperties(parserContext);
		autoRegisterBatchPostProcessor(parserContext);
		autoRegisterJsrAutowiredAnnotationBeanPostProcessor(parserContext);
		autoRegisterThreadLocalClassloaderBeanPostProcessor(parserContext);
		autoRegisterBeanScopeBeanFactoryPostProcessor(parserContext);
	}

	private static void autoRegisterBeanScopeBeanFactoryPostProcessor(
			ParserContext parserContext) {
		registerPostProcessor(parserContext, JsrBeanScopeBeanFactoryPostProcessor.class, BeanDefinition.ROLE_INFRASTRUCTURE, BEAN_SCOPE_POST_PROCESSOR_BEAN_NAME);
	}

	private static void autoRegisterBatchPostProcessor(ParserContext parserContext) {
		registerPostProcessor(parserContext, BatchPropertyBeanPostProcessor.class, BeanDefinition.ROLE_INFRASTRUCTURE, BATCH_PROPERTY_POST_PROCESSOR_BEAN_NAME);
	}

	private static void autoRegisterJsrAutowiredAnnotationBeanPostProcessor(ParserContext parserContext) {
		registerPostProcessor(parserContext, JsrAutowiredAnnotationBeanPostProcessor.class, BeanDefinition.ROLE_INFRASTRUCTURE, AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME);
	}

	private static void autoRegisterThreadLocalClassloaderBeanPostProcessor(ParserContext parserContext) {
		registerPostProcessor(parserContext, ThreadLocalClassloaderBeanPostProcessor.class, BeanDefinition.ROLE_INFRASTRUCTURE, THREAD_LOCAL_CLASS_LOADER_BEAN_POST_PROCESSOR_BEAN_NAME);
	}

	private static void registerPostProcessor(ParserContext parserContext, Class<?> clazz, int role, String beanName) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);

		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
		beanDefinition.setRole(role);

		parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
	}

	// Registers a bean by the name of {@link #JOB_PROPERTIES_BEAN_NAME} so job level properties can be obtained through
	// for example a SPeL expression referencing #{jobProperties['key']} similar to systemProperties resolution.
	private static void autoRegisterJobProperties(ParserContext parserContext) {
		if (!parserContext.getRegistry().containsBeanDefinition(JOB_PROPERTIES_BEAN_NAME)) {
			AbstractBeanDefinition jobPropertiesBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(HashMap.class).getBeanDefinition();
			jobPropertiesBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			parserContext.getRegistry().registerBeanDefinition(JOB_PROPERTIES_BEAN_NAME, jobPropertiesBeanDefinition);
		}
	}
}
