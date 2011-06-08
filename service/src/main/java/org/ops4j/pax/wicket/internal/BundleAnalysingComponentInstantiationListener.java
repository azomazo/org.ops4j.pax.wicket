package org.ops4j.pax.wicket.internal;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import net.sf.cglib.proxy.Factory;

import org.ops4j.pax.wicket.internal.spring.SpringBeanProxyTargetLocator;
import org.ops4j.pax.wicket.util.proxy.IProxyTargetLocator;
import org.ops4j.pax.wicket.util.proxy.LazyInitProxyFactory;
import org.ops4j.pax.wicket.util.proxy.PaxWicketBean;
import org.ops4j.pax.wicket.util.proxy.PaxWicketBean.BeanResolverType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleAnalysingComponentInstantiationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleAnalysingComponentInstantiationListener.class);

    private final BundleContext bundleContext;
    private String bundleResources = "";

    private final String applicationName;

    @SuppressWarnings("unchecked")
    public BundleAnalysingComponentInstantiationListener(BundleContext bundleContext, String applicationName) {
        this.bundleContext = bundleContext;
        this.applicationName = applicationName;
        Enumeration<URL> entries = bundleContext.getBundle().findEntries("/", "*.class", true);
        while (entries.hasMoreElements()) {
            String urlRepresentation =
                entries.nextElement().toExternalForm().replace("bundle://.+?/", "").replace('/', '.');
            LOGGER.trace("Found entry {} in bundle {}", urlRepresentation, bundleContext.getBundle().getSymbolicName());
            bundleResources += urlRepresentation;
        }
    }

    public boolean injectionPossible(Class<?> component) {
        String name = component.getCanonicalName();
        LOGGER.debug("Try to find class {} in bundle {}", name, bundleContext.getBundle().getSymbolicName());
        String searchString = name.replaceAll("\\$\\$.*", "");
        if (bundleResources.matches(".*" + searchString + ".*")) {
            LOGGER.trace("Found class {} in bundle {}", name, bundleContext.getBundle().getSymbolicName());
            return true;
        }
        LOGGER.trace("Class {} not available in bundle {}", name, bundleContext.getBundle().getSymbolicName());
        return false;
    }

    public void inject(Object component) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> realClass = component.getClass();
            Map<String, String> overwrites = null;
            if (Factory.class.isInstance(component)) {
                overwrites = ((OverwriteProxy) ((Factory) component).getCallback(0)).getOverwrites();
                realClass = realClass.getSuperclass();
            }
            Thread.currentThread().setContextClassLoader(realClass.getClassLoader());

            Field[] fields = realClass.getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(PaxWicketBean.class)) {
                    continue;
                }
                Object proxy = createProxy(field, realClass, overwrites);
                setField(component, field, proxy);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private void setField(Object component, Field field, Object proxy) {
        try {
            checkAccessabilityOfField(field);
            field.set(component, proxy);
        } catch (Exception e) {
            throw new RuntimeException("Bumm", e);
        }
    }

    private void checkAccessabilityOfField(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    private Object createProxy(Field field, Class<?> page, Map<String, String> overwrites) {
        return LazyInitProxyFactory.createProxy(getBeanType(field), createProxyTargetLocator(field, page, overwrites));
    }

    private IProxyTargetLocator createProxyTargetLocator(Field field, Class<?> page, Map<String, String> overwrites) {
        PaxWicketBean annotation = field.getAnnotation(PaxWicketBean.class);
        if (annotation.beanResolverType().equals(BeanResolverType.UNCONFIGURED)
                || annotation.beanResolverType().equals(BeanResolverType.SPRING)) {
            return resolveSpringBeanTargetLocator(field, page, annotation, overwrites);
        }
        return resolveBlueprintBeanTargetLocator(field, page, annotation, overwrites);
    }

    private IProxyTargetLocator resolveSpringBeanTargetLocator(Field field, Class<?> page,
            PaxWicketBean annotation, Map<String, String> overwrites) {
        return new SpringBeanProxyTargetLocator(applicationName, bundleContext, annotation, getBeanType(field), page,
            overwrites);
    }

    private IProxyTargetLocator resolveBlueprintBeanTargetLocator(Field field, Class<?> page, PaxWicketBean annotation,
            Map<String, String> overwrites) {
        throw new NotImplementedException();
    }

    private Class<?> getBeanType(Field field) {
        Class<?> beanType = field.getType();
        return beanType;
    }
}