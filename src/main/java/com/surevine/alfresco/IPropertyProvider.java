package com.surevine.alfresco;

/**
 * Interface for classes which provide properties of key->value.
 */
public interface IPropertyProvider {
    /**
     * Get a property.
     * @param key The property name
     * @return The property value
     * @throws PropertyException If the property didn't exist
     */
    public String getProperty(final String key) throws PropertyException;

    /**
     * Get an optional property.
     * @param key The property name
     * @param defaultValue A default value to use if the property wasn't found
     * @return The property value
     */
    public String getProperty(final String key, final String defaultValue);
}
