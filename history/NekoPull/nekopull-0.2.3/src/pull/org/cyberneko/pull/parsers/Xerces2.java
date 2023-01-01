/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.parsers;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLPullParser;
import org.cyberneko.pull.util.EventCollector;
import org.cyberneko.pull.util.EventQueue;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.util.ObjectFactory;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;

/**
 * An implementation of a pull parser that can use any standard
 * XNI parser configuration as a driver.
 * <p>
 * <strong>Note:</strong>
 * This class is provided for convenience. However, for the best
 * performance, a parser should be implemented directly to the
 * NekoPull interfaces to provide pull parsing functionality.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class Xerces2
    implements XMLPullParser {

    //
    // Constants
    //

    // features

    /** Feature identifer: reuse buffers. */
    protected static final String REUSE_BUFFERS =
        "http://apache.org/xml/features/scanner/reuse-buffers";

    // private

    /** Standard configuration. */
    private static final String CONFIG = "org.apache.xerces.parsers.StandardParserConfiguration";

    //
    // Data
    //

    /** Finished parsing. */
    protected boolean fDone;

    /** XNI pull parser configuration. */
    protected XMLPullParserConfiguration fConfiguration;

    /** Event collector. */
    protected EventCollector fEventCollector = new EventCollector();

    //
    // Constructors
    //

    /** Constructs a pull parser with the standard configuration. */
    public Xerces2() {
        this((XMLPullParserConfiguration)ObjectFactory.createObject(CONFIG,CONFIG));
    } // <init>()

    /** Constructs a pull parser with the specified configuration. */
    public Xerces2(XMLPullParserConfiguration config) {
        fConfiguration = config;
        // REVISIT: This functionality is not implemented in the Xerces2
        //          reference implementation of XNI, yet. -Ac
        try {
            fConfiguration.setFeature(REUSE_BUFFERS, false);
        }
        catch (Exception e) {
            // ignore
            //System.err.println(">>> "+e.getClass().getName()+":"+e.getMessage());
        }
        fConfiguration.setDocumentHandler(fEventCollector);
    } // <init>(XMLParserConfiguration)

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
     */
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        fDone = false;
        fEventCollector.reset(fConfiguration);
        fConfiguration.setInputSource(inputSource);
    } // setInputSource(inputSource)

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resource allocated during parsing. For example, close all opened streams.
     */
    public void cleanup() {
        fConfiguration.cleanup();
    } // cleanup()
    
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
        throws XMLConfigurationException {
        fConfiguration.setFeature(featureId, state);
    } // setFeature(String,boolean)

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
        throws XMLConfigurationException {
        fConfiguration.setProperty(propertyId, value);
    } // setProperty(String,Object)

    //
    // XMLEventIterator methods
    //

    /**
     * Returns the next event in the document or null if there are
     * no more events. This method will return one and only one event
     * if it is available; it will never return an event chain (i.e.
     * an event with a non-null <code>next</code> field).
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    public XMLEvent nextEvent() throws XNIException, IOException {
        while (!fDone && fEventCollector.isEmpty()) {
            fDone = !fConfiguration.parse(false);
        }
        return fEventCollector.dequeue();
    } // nextEvent():XMLEvent

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
        throws XMLConfigurationException {
        return fConfiguration.getFeature(featureId);
    } // getFeature(String):boolean

    /**
     * Returns the value of a property.
     * 
     * @param propertyId The property identifier.
     * 
     * @throws XMLConfigurationException Thrown if there is a configuration
     *                                   error.
     */
    public Object getProperty(String propertyId)
        throws XMLConfigurationException {
        return fConfiguration.getProperty(propertyId);
    } // getProperty(String):Object

    // handlers

    /**
     * Sets the error handler.
     *
     * @param errorHandler The error resolver.
     */
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        fConfiguration.setErrorHandler(errorHandler);
    } // setErrorHandler(XMLErrorHandler)

    /** Returns the registered error handler. */
    public XMLErrorHandler getErrorHandler() {
        return fConfiguration.getErrorHandler();
    } // getErrorHandler():XMLErrorHandler

    // other settings

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver The new entity resolver.
     */
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        fConfiguration.setEntityResolver(entityResolver);
    } // setEntityResolver(XMLEntityResolver)

    /** Returns the registered entity resolver. */
    public XMLEntityResolver getEntityResolver() {
        return fConfiguration.getEntityResolver();
    } // getEntityResolver():XMLEntityResolver

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) throws XNIException {
        fConfiguration.setLocale(locale);
    } // setLocale(Locale)

    /** Returns the locale. */
    public Locale getLocale() {
        return fConfiguration.getLocale();
    } // getLocale():Locale

} // class Xerces2
