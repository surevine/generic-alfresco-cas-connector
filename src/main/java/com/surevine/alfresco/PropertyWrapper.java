/*
 * Copyright (C) 2010 Surevine Ltd.
 *
 * All rights reserved.
 */

package com.surevine.alfresco;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.MissingResourceException;


/**
 * Wrapper around ResourceBundle allowing specifying default values and cleanly handling errors.
 */
public class PropertyWrapper implements IPropertyProvider {
    /**
     * The wrapped ResourceBundle.
     */
    private ResourceBundle bundle;

    /**
     * @param filename Filename of a property file on the CLASSPATH
     * @throws PropertyException If the property file cannot be found
     */
    public PropertyWrapper(final String filename) throws PropertyException {
        try {
            bundle = ResourceBundle.getBundle(filename);
        } catch (MissingResourceException e) {
            throw new PropertyException("Cannot load properties file: "
                    + filename, e);
        }
    }

    /**
     * @param b Existing resource bundle from which to load properties 
     */
    public PropertyWrapper(final ResourceBundle b) {
        bundle = b;
    }

    /**
     * Get a property.
     * @param key The property name
     * @return The property value
     * @throws PropertyException If the property didn't exist
     */
    public String getProperty(final String key) throws PropertyException {
        String value = getPropertyIfExists(key);

        if (value == null) {
            throw new PropertyException("Missing property: " + key);
        }

        return value;
    }

    /**
     * Get an optional property.
     * @param key The property name
     * @param defaultValue A default value to use if the property wasn't found
     * @return The property value
     */
    public String getProperty(final String key, final String defaultValue) {
        String value = getPropertyIfExists(key);

        if (value == null) {
            value = defaultValue;
        }

        return value;
    }
    
    /**
     * Return all keys in the property file which match a regular expression.
     * @param regex Regular expression to filter the keys.
     * @return A list of keys
     */
    public Collection<String> getKeys(String regex) {
        
        Collection<String> keys = new LinkedList<String>();
        
        Enumeration<?> iter = bundle.getKeys();
        
        while (iter.hasMoreElements()) {
            String key = iter.nextElement().toString();
            
            if (key.matches(regex)) {
                keys.add(key);
            }
        }
        
        return keys;
    }

    /**
     * Get a property, or null if it doesn't exist.
     * @param key The property name
     * @return The property value, or null if it doesn't exist
     */
    private String getPropertyIfExists(final String key) {
        try {
            return bundle.getString(key);
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e) {
            return null;
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
