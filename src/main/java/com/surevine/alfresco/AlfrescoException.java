/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco;

/**
 * Class for exceptions thrown by the package.
 */
public class AlfrescoException extends Exception {
    /**
     * Required for serialisable classes.
     */
    private static final long serialVersionUID = -5567586107070213037L;

    /**
     * Construct with a message.
     * @param message The message
     */
    public AlfrescoException(final String message) {
        super(message);
    }

    /**
     * Construct from another exception which caused the problem.
     * @param message The message
     * @param cause The exception that caused this
     */
    public AlfrescoException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
