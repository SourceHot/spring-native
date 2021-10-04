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

package org.springframework.context.annotation.samples.scope;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.aot.context.bootstrap.generator.sample.factory.NumberHolder;
import org.springframework.aot.context.bootstrap.generator.sample.factory.StringHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Configuration(proxyBeanMethods = false)
public class ScopeConfiguration {

	private static final AtomicInteger counter = new AtomicInteger();

	@Bean
	@Scope("prototype")
	public NumberHolder<Integer> counterBean() {
		return new NumberHolder<>(counter.getAndIncrement());
	}

	@Bean
	@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringHolder timeBean() {
		return new StringHolder(Instant.now().toString());
	}

}
