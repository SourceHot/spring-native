/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.nativex.buildtools.nativex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.nativex.buildtools.BootstrapContributor;
import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.buildtools.ResourceFile;
import org.springframework.nativex.support.ConfigOptions;
import org.springframework.nativex.support.ConfigurationCollector;
import org.springframework.nativex.support.Mode;
import org.springframework.nativex.support.SpringAnalyzer;
import org.springframework.nativex.type.TypeSystem;

/**
 * Contributes the configuration files for native image construction. This includes the reflection,
 * resource and proxy configuration in addition to the native-image.properties file that includes
 * special args and initialization configuration.
 * 
 * @author Andy Clement
 */
public class ConfigurationContributor implements BootstrapContributor {

	Path META_INF_NATIVE_IMAGE = Paths.get("META-INF", "native-image");
	
	@Override
	public void contribute(BuildContext context) {
		if (!isActive()) {
			return;
		}
		ConfigOptions.setMode(Mode.REFLECTION);
//		ConfigOptions.setMode(Mode.FUNCTIONAL);
		ConfigOptions.setVerbose(true);
		TypeSystem typeSystem = context.getTypeSystem();
		ConfigOptions.setBuildTimeTransformation(true);
		SpringAnalyzer springAnalyzer = new SpringAnalyzer(typeSystem);
		springAnalyzer.analyze();
		ConfigurationCollector configurationCollector = springAnalyzer.getConfigurationCollector();

		context.describeReflection(reflect -> reflect.merge(configurationCollector.getReflectionDescriptor()));
		context.describeResources(resources -> resources.merge(configurationCollector.getResourcesDescriptors()));
		context.describeProxies(proxies -> proxies.merge(configurationCollector.getProxyDescriptors()));
		// Create native-image.properties
		context.addResources(new ResourceFile() {
			@Override
			public void writeTo(Path rootPath) throws IOException {
				Path nativeConfigFolder = rootPath.resolve(ResourceFile.NATIVE_CONFIG_PATH);
				Files.createDirectories(nativeConfigFolder);
				Path nativeImagePropertiesFile = nativeConfigFolder.resolve("native-image.properties");
				try (FileOutputStream fos = new FileOutputStream(nativeImagePropertiesFile.toFile())) {
					fos.write(configurationCollector.getNativeImagePropertiesContent().getBytes());
				}
			}
		});
		// Create an indicator file that will ensure the feature switches off if it is somehow on the classpath
		context.addResources(new ResourceFile() {
			@Override
			public void writeTo(Path rootPath) throws IOException {
				Path nativeImageFolder = rootPath.resolve(ResourceFile.NATIVE_IMAGE_PATH);
				Files.createDirectories(nativeImageFolder);
				Path btcFile = nativeImageFolder.resolve("build-time-computed-config.properties");
				try (FileOutputStream fos = new FileOutputStream(btcFile.toFile())) {
					fos.write("build-time-generation=true".getBytes());
				}
			}
		});
	}

	/**
	 * Possibly a temporary measure. Check if the collector should be active by trying to resolve /reflect.json - if we
	 * can't then things aren't setup with feature+configuration modules (which is the case when this runs during the tests
	 * in the buildtools module, which only depends on feature). When collector is used alongside configuration in the plugin
	 * dependencies section of a real build attempting to use the collector properly, this will resolve OK.
	 */
	private boolean isActive() {
		try (InputStream s = this.getClass().getResourceAsStream("/reflect.json")) {
			if (s==null) {
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}


}
