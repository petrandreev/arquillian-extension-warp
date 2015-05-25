/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.proxy;

import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProviderWrapper;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;

/**
 *
 * @author pan
 *
 */
public class ProxyURLWrapper implements ResourceProviderWrapper {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<TestClass> testClass;

    @Override
    public boolean canWrap(Class<?> type, Annotation... qualifiers) {
        return URL.class.isAssignableFrom(type) && WarpCommons.isWarpTest(testClass.get().getJavaClass());
    }

    @Override
    public Object wrap(Object object, ArquillianResource resource, Annotation... qualifiers) {
        if(!(object instanceof URL)) {
            return object;
        }
        URL url = (URL) object;
        if (!("http".equals(url.getProtocol()) && WarpCommons.isWarpTest(testClass.get().getJavaClass()))) {
            return object;
        }
        return toProxyURL((URL) object);
    }

    private URL toProxyURL(URL url) {
        URLMapping urlMapping = urlMapping();
        if(urlMapping.isProxyURL(url)) {
            return url;
        }
        URL baseRealURL = URLUtils.getUrlBase(url);
        URL baseProxyURL = urlMapping.getProxyURL(baseRealURL);
        URL proxyURL = URLUtils.buildUrl(baseProxyURL, url.getPath());
        return proxyURL;
    }

    private URLMapping urlMapping() {
        return serviceLoader.get().onlyOne(URLMapping.class);
    }
}
