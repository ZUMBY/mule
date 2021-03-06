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

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.config.i18n.MessageFactory;
import org.mule.security.oauth.OAuth2Adapter;
import org.mule.security.oauth.OAuth2Manager;

public class OAuth2FetchAccessTokenMessageProcessor extends FetchAccessTokenMessageProcessor
{

    
    private OAuth2Manager<OAuth2Adapter> oauthManager;

    public OAuth2FetchAccessTokenMessageProcessor(OAuth2Manager<OAuth2Adapter> oauthManager)
    {
        this.oauthManager = oauthManager;
    }

    public MuleEvent process(MuleEvent event) throws MuleException
    {
        try
        {
            OAuth2Adapter oauthAdapter = this.oauthManager.createAdapter(((String) event.getMessage()
                .getInvocationProperty("_oauthVerifier")));
            if (oauthAdapter.getAccessTokenUrl() == null)
            {
                oauthAdapter.setAccessTokenUrl(this.getAccessTokenUrl());
            }
            oauthAdapter.fetchAccessToken(this.getRedirectUri());
            oauthManager.getAccessTokenPoolFactory().passivateObject(oauthAdapter.getAccessTokenId(),
                oauthAdapter);
            event.getMessage().setInvocationProperty("OAuthAccessTokenId", oauthAdapter.getAccessTokenId());
        }
        catch (Exception e)
        {
            throw new MessagingException(MessageFactory.createStaticMessage("Unable to fetch access token"),
                event, e);
        }
        return event;
    }

}
