/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco;

/**
 * Class for exceptions thrown by PropertyWrapper.
 */
public class PropertyException extends Exception {
    /**
     * Required for serialisable classes.
     */
    private static final long serialVersionUID = -2764984517429067835L;

    /**
     * Construct with a message.
     * @param message The message
     */
    public PropertyException(final String message) {
        super(message);
    }

    /**
     * Construct from another exception which caused the problem .
     * @param message The message
     * @param cause The exception that caused this
     */
    public PropertyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
