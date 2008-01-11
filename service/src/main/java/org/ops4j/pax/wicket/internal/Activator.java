/*
 * Copyright 2005 Niclas Hedhman.
 * Copyright 2006 Edward F. Yakop
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
package org.ops4j.pax.wicket.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Activator
    implements BundleActivator
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Activator.class );

    private HttpTracker m_httpTracker;
    private ServiceTracker m_appFactoryTracker;

    public final void start( BundleContext bundleContext )
        throws Exception
    {
        LOGGER.debug( "Initializing the servlet." );

        m_httpTracker = new HttpTracker( bundleContext );
        m_httpTracker.open();

        m_appFactoryTracker = new PaxWicketAppFactoryTracker( bundleContext, m_httpTracker );
        m_appFactoryTracker.open();
    }

    public final void stop( BundleContext bundleContext )
        throws Exception
    {
        m_httpTracker.close();
        m_httpTracker = null;
        m_appFactoryTracker.close();
        m_appFactoryTracker = null;
    }
}
