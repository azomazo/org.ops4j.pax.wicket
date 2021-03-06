/*
 * Copyright OPS4J
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.wicket.spi.springdm.injection.spring;

import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.spi.springdm.injection.InjectionParserUtil;
import org.ops4j.pax.wicket.spi.springdm.injection.PageFactoryDecorator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

public class SpringPageFactoryBeanDefinitionParser extends AbstractSpringBeanDefinitionParser {

    @Override
    public Class<?> getBeanClass(Element element) {
        return PageFactoryDecorator.class;
    }

    @Override
    protected void prepareInjection(Element element, BeanDefinitionBuilder bean) {
        addPropertyValueFromElement("pageId", element, bean);
        addPropertyValueFromElement("applicationName", element, bean);
        addPropertyValueFromElement("pageName", element, bean);
        addPropertyValueFromElement("pageClass", element, bean);
        bean.addPropertyValue("overwrites", InjectionParserUtil.retrieveOverwriteElements(element));
        bean.addPropertyValue("injectionSource", PaxWicketBean.INJECTION_SOURCE_SPRING);
    }

}
