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

import java.io.IOException;
import java.net.URL;
import javax.activation.MimetypesFileTypeMap;
import static javax.activation.MimetypesFileTypeMap.getDefaultFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericContext
    implements HttpContext
{

    private static final Logger LOGGER = LoggerFactory.getLogger( GenericContext.class );

    private String mountPoint;
    private MimetypesFileTypeMap typeMap;
    private Bundle bundle;

    public GenericContext( Bundle aBundle, String aMountPoint )
    {
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "GenericContext(" + aMountPoint + " )" );
        }

        bundle = aBundle;

        if( !aMountPoint.startsWith( "/" ) )
        {
            aMountPoint = "/" + aMountPoint;
        }
        mountPoint = aMountPoint;
        typeMap = (MimetypesFileTypeMap) getDefaultFileTypeMap();
        typeMap.addMimeTypes( "text/css css" );
    }

    public boolean handleSecurity( HttpServletRequest aRequest, HttpServletResponse aResponse )
        throws IOException
    {
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "handleSecurity()" );
        }

        return true;
    }

    public URL getResource( String aResourceName )
    {
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "getResource( " + aResourceName + " )" );
        }

        aResourceName = aResourceName.substring( mountPoint.length() );
        return bundle.getResource( aResourceName );
    }

    public String getMimeType( String aResourceName )
    {
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "getMimeType( " + aResourceName + " )" );
        }
        URL resource = getResource( aResourceName );
        if( resource == null )
        {
            return null;
        }
        String url = resource.toString();
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "         URL: " + url );
        }

        String contentType = typeMap.getContentType( url );
        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( " ContentType: " + contentType );
        }
        return contentType;
    }
}
