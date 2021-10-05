/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.context.bootstrap.generator.bean;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultBeanRegistrationWriterSupplier}.
 *
 * @author Stephane Nicoll
 */
class DefaultBeanRegistrationWriterSupplierTests {

	@Test
	void getWithRootBeanDefinitionProvideDefaultWriter() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanRegistrationWriter writer = getBeanRegistrationWriter(beanFactory, BeanDefinitionBuilder.rootBeanDefinition(
				DefaultBeanInstanceSupplierWriterTests.class).getBeanDefinition());
		assertThat(writer).isNotNull().isInstanceOf(DefaultBeanRegistrationWriter.class);
	}

	@Test
	void getWithGenericBeanDefinitionProvideDefaultWriter() {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanRegistrationWriter writer = getBeanRegistrationWriter(beanFactory, BeanDefinitionBuilder.genericBeanDefinition(
				DefaultBeanInstanceSupplierWriterTests.class).getBeanDefinition());
		assertThat(writer).isNotNull().isInstanceOf(DefaultBeanRegistrationWriter.class);
	}

	@Test
	void getWithNoBeanDescriptorReturnNull() {
		BeanDefinition beanDefinition = new RootBeanDefinition(String.class);
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, new Object());
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanRegistrationWriter writer = getBeanRegistrationWriter(beanFactory, beanDefinition);
		assertThat(writer).isNull();
	}

	private BeanRegistrationWriter getBeanRegistrationWriter(DefaultListableBeanFactory beanFactory, BeanDefinition beanDefinition) {
		DefaultBeanRegistrationWriterSupplier supplier = new DefaultBeanRegistrationWriterSupplier();
		supplier.setBeanFactory(beanFactory);
		return supplier.get("test", beanDefinition);
	}

}
