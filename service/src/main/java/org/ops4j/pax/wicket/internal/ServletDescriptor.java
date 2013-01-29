/**
 * Copyright OPS4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.wicket.internal;

import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.wicket.util.collections.MultiMap;
import org.ops4j.pax.wicket.api.WebApplicationFactory;
import org.ops4j.pax.wicket.internal.util.MapAsDictionary;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate the registration information for a servlet
 */
public final class ServletDescriptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServletDescriptor.class);

    private final Servlet servlet;
    private final HttpContext httpContext;
    private final MultiMap<String, String> httpServiceSelector;
    private final Dictionary<?, ?> contextParams;
    private final String alias;
    private final List<HttpService> services;

    public ServletDescriptor(Servlet servlet, String alias, Bundle bundle,
                Map<?, ?> contextParams) {
        this.servlet = servlet;
        this.alias = alias;
        this.httpContext = new GenericContext(bundle, alias);
        this.contextParams = contextParams == null ? null
                : MapAsDictionary.wrap(contextParams);
        httpServiceSelector = parseHttpServiceSelector(contextParams);
        services = new ArrayList<HttpService>();
    }

    /**
     * register the service with the given {@link HttpService} if not already registered and the given service is not
     * <code>null</code>
     * 
     * @param service
     * @throws NamespaceException if the registration fails because the alias is already in use.
     * @throws ServletException if the servlet's init method throws an exception, or the given servlet object has
     *         already been registered at a different alias.
     * @throws NamespaceException when the servlet is currently registered under a different {@link HttpService}
     */
    public synchronized void register(HttpService service) throws ServletException, NamespaceException,
            IllegalStateException {
        if (service != null) {
            if (!this.services.contains(service)) {
                LOG.info("register new servlet on mountpoint {} with contextParams {}", getAlias(),
                        contextParams);
                service.registerServlet(getAlias(), servlet, contextParams, httpContext);
                this.services.add(service);
            } else {
                throw new IllegalStateException("the servlet is already registered with another HttpService");
            }
        }
    }

    /**
     * Unregister a servlet if already registered. After this call it is save to register the servlet again
     */
    public synchronized void unregister() {
        if (!this.services.isEmpty()) {
            for(Iterator<HttpService> iterator= services.iterator(); iterator.hasNext(); ) {
                HttpService service = iterator.next();
                service.unregister(getAlias());
                iterator.remove();
            }
        }
    }

    public synchronized void unregister(HttpService httpService){
        if(services.contains(httpService)){
            httpService.unregister(getAlias());
            services.remove(httpService);
        }
    }

    public String getAlias() {
        return alias;
    }

    public boolean checkHttpServiceReference(ServiceReference serviceReference) {
        boolean checked = true;
        for (Map.Entry<String, List<String>> paramEntry : httpServiceSelector.entrySet()) {
            String propertyValue = serviceReference.getProperty(paramEntry.getKey()).toString();
            if (propertyValue == null || propertyValue.isEmpty()
                    || !paramEntry.getValue().contains(propertyValue.trim())) {
                checked = false;
                break;
            }
        }
        return checked;
    }

    public static MultiMap<String, String> parseHttpServiceSelector(Map<?, ?> contextParam){
        String value = contextParam.get(org.ops4j.pax.wicket.api.Constants.HTTP_SERVICE_SELECTOR).toString();
        if (value == null || value.isEmpty()) {
            return new MultiMap<String, String>();
        }

        MultiMap<String, String> params = new MultiMap<String, String>();
        for (String val : value.split(";")) {
            String[] paramSplit = val.split("=");
            params.addValue(paramSplit[0].trim(), paramSplit[1].trim());
        }
        return params;
    }
}
