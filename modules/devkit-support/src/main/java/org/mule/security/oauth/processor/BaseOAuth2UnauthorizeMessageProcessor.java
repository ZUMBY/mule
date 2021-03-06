/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.security.oauth.processor;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.common.connection.exception.UnableToAcquireConnectionException;
import org.mule.config.i18n.CoreMessages;
import org.mule.security.oauth.OAuth2Adapter;
import org.mule.security.oauth.OAuth2Manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseOAuth2UnauthorizeMessageProcessor<T extends OAuth2Manager<OAuth2Adapter>> extends
    AbstractDevkitBasedMessageProcessor implements MessageProcessor
{

    private static Logger logger = LoggerFactory.getLogger(BaseOAuth2UnauthorizeMessageProcessor.class);

    protected abstract Class<T> getOAuthManagerClass();

    /**
     * Unauthorize the connector
     * 
     * @param event MuleEvent to be processed
     * @throws MuleException
     */
    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        OAuth2Manager<OAuth2Adapter> manager = this.getOAuthManager();
        try
        {
            String transformedToken = ((String) evaluateAndTransform(getMuleContext(), event, String.class,
                null, getAccessTokenId()));

            if (logger.isDebugEnabled())
            {
                logger.debug("Attempting to acquire access token using from store for user "
                             + transformedToken);
            }

            OAuth2Adapter connector = manager.acquireAccessToken(transformedToken);

            if (connector == null)
            {
                throw new UnableToAcquireConnectionException();
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(String.format("Access Token has been acquired for [tokenId= %s]",
                        transformedToken));
                }

                manager.destroyAccessToken(transformedToken, connector);

                if (logger.isDebugEnabled())
                {
                    logger.debug(String.format(
                        "Access token for [tokenId= %s] has been successfully destroyed", transformedToken));
                }
            }
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(
                CoreMessages.createStaticMessage("Unable to unauthorize the connector"), e);
        }
        return event;
    }

    @SuppressWarnings("unchecked")
    protected OAuth2Manager<OAuth2Adapter> getOAuthManager()
    {
        try
        {
            Object maybeAManager = this.findOrCreate(this.getOAuthManagerClass(), false, null);
            if (!(maybeAManager instanceof OAuth2Manager))
            {
                throw new IllegalStateException(String.format(
                    "Object of class %s does not implement OAuth2Manager", this.getOAuthManagerClass()
                        .getCanonicalName()));
            }

            return (OAuth2Manager<OAuth2Adapter>) maybeAManager;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
