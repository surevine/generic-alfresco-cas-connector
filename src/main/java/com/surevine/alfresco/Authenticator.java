/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco;

import org.apache.http.client.HttpClient;

/**
 * Interface for authenticating HTTP connections.
 */
public interface Authenticator {

    /**
     * Attempt to authenticate the client.
     * @param client The HTTP client to authenticate
     * @return True if authentication was successful, otherwise false
     */
    boolean authenticate(HttpClient client);
}
