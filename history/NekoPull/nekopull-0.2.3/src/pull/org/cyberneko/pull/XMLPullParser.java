/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * This interface defines an XML pull parser built on top of the Xerces
 * Native Interface (XNI). Using a pull parser, an application controls
 * the parsing of an XML stream by requesting event objects returned by
 * the <code>nextEvent</code> method of the <code>XMLEventIterator</code>
 * interface. This is different than the traditional SAX paradigm in
 * which the application registers handlers and document information is
 * <em>pushed</em> by the parser to the handlers. For specific details,
 * refer to the documentation.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public interface XMLPullParser 
    extends XMLEventIterator, XMLComponentManager {

    //
    // XMLPullParser methods
    //

    // parsing

    /**
     * Sets the input source for the document to parse.
     *
     * @param inputSource The document's input source.
     *
     * @exception XMLConfigurationException Thrown if there is a 
     *                        configuration error when initializing the
     *                        parser.
     * @exception IOException Thrown on I/O error.
     *
     * @see #nextEvent
     * @see #cleanup
     */
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException;

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resources allocated during parsing. For example, close all opened streams.
     */
    public void cleanup();
    
    // handlers

    /**
     * Sets the error handler.
     *
     * @param errorHandler The error resolver.
     */
    public void setErrorHandler(XMLErrorHandler errorHandler);

    /** Returns the registered error handler. */
    public XMLErrorHandler getErrorHandler();

    // other settings

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver The new entity resolver.
     */
    public void setEntityResolver(XMLEntityResolver entityResolver);

    /** Returns the registered entity resolver. */
    public XMLEntityResolver getEntityResolver();

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) throws XNIException;

    /** Returns the locale. */
    public Locale getLocale();

    // generic configuration

    /**
     * Sets the state of a feature. This method is called by the parser
     * and gets propagated to components in this parser configuration.
     * 
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws XMLConfigurationException Thrown if there is a configuration
     *                                   error.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException;

    /**
     * Sets the value of a property. This method is called by the parser
     * and gets propagated to components in this parser configuration.
     * 
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws XMLConfigurationException Thrown if there is a configuration
     *                                   error.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException;

    //
    // XMLEventIterator methods
    //

    /**
     * Returns the next event in the document or null if there are
     * no more events.
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    public XMLEvent nextEvent() throws XNIException, IOException;

    //
    // XMLComponentManager methods
    //

    /**
     * Returns the state of a feature.
     * 
     * @param featureId The feature identifier.
     * 
     * @throws XMLConfigurationException Thrown if there is a configuration
     *                                   error.
     */
    public boolean getFeature(String featureId)
        throws XMLConfigurationException;

    /**
     * Returns the value of a property.
     * 
     * @param propertyId The property identifier.
     * 
     * @throws XMLConfigurationException Thrown if there is a configuration
     *                                   error.
     */
    public Object getProperty(String propertyId)
        throws XMLConfigurationException;

} // interface XMLPullParser
