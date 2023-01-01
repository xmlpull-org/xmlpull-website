/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.util;

import org.cyberneko.pull.XMLEvent;

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;

/**
 * An implementation of an XNI <code>XMLPullParserConfiguration</code>
 * that buffers the events so that one and only callback is performed
 * for each call to <code>parse(boolean):boolean</code>. In addition,
 * this class can be used to turn any XNI parser configuration, even
 * it doesn't implement <code>XMLPullParserConfiguration</code>, into
 * a fully buffered, pull parser configuration.
 * <p>
 * <strong>Note:</strong>
 * There is a performance hit to buffering the underlying XNI events.
 * While the difference is negligible for small documents, it becomes
 * more pronounced as the document size increases.
 *
 * @see EventCollector
 * @see EventDispatcher
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class BufferedPullConfiguration
    implements XMLPullParserConfiguration {

    //
    // Data
    //

    /** Parser configuration. */
    protected XMLParserConfiguration fConfiguration;

    /** Pull parser configuration. */
    protected XMLPullParserConfiguration fPullConfiguration;

    /** Input source. */
    protected XMLInputSource fInputSource;

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** Event collector. */
    protected EventCollector fEventCollector;

    /** Event dispatcher. */
    protected EventDispatcher fEventDispatcher;

    //
    // Constructors
    //

    /** Constructs a buffered pull parser from the specified configuration. */
    public BufferedPullConfiguration(XMLParserConfiguration config) {
        fConfiguration = config;
        if (config instanceof XMLPullParserConfiguration) {
            fPullConfiguration = (XMLPullParserConfiguration)config;
        }
        fEventCollector = new EventCollector();
        fEventDispatcher = new EventDispatcher();
    } // <init>(XMLPullParserConfiguration,EventQueue)

    //
    // XMLPullParserConfiguration methods
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
     * @see #parse(boolean)
     */
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        reset();
        if (fPullConfiguration != null) {
            fPullConfiguration.setInputSource(inputSource);
        }
        else {
            fInputSource = inputSource;
        }
    } // setInputSource(XMLInputSource)

    /**
     * Parses the document in a pull parsing fashion.
     *
     * @param complete True if the pull parser should parse the
     *                 remaining document completely.
     *
     * @returns True if there is more document to parse.
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     *
     * @see #setInputSource
     */
    public boolean parse(boolean complete) throws XNIException, IOException {

        // complete parse
        if (complete) {
            XMLEvent event = fEventCollector.dequeue();
            while (event != null) {
                fEventDispatcher.dispatchEvent(event);
                event = fEventCollector.dequeue();
            }
            fPullConfiguration.setDocumentHandler(fDocumentHandler);
            if (fPullConfiguration != null) {
                return fPullConfiguration.parse(true);
            }
            XMLInputSource inputSource = fInputSource;
            if (inputSource != null) {
                fInputSource = null;
            }
            fConfiguration.parse(inputSource);
            return true;
        }

        // buffered parse
        XMLEvent event = fEventCollector.dequeue();
        boolean more = true;
        if (event == null) {
            if (fPullConfiguration != null) {
                while (event == null && more) {
                    more = fPullConfiguration.parse(false);
                    event = fEventCollector.dequeue();
                }
            }
            else {
                XMLInputSource inputSource = fInputSource;
                if (inputSource != null) {
                    fInputSource = null;
                }
                fConfiguration.parse(inputSource);
                more = false;
            }
        }
        if (event != null) {
            fEventDispatcher.dispatchEvent(event);
        }
        return !fEventCollector.isEmpty() || more;

    } // parse(boolean):boolean

    /**
     * If the application decides to terminate parsing before the xml document
     * is fully parsed, the application should call this method to free any
     * resource allocated during parsing. For example, close all opened streams.
     */
    public void cleanup() {
        if (fPullConfiguration != null) {
            fPullConfiguration.cleanup();
        }
    } // cleanup()
    
    //
    // XMLParserConfiguration methods
    //

    // parsing

    /**
     * Parse an XML document.
     * <p>
     * The parser can use this method to instruct this configuration
     * to begin parsing an XML document from any valid input source
     * (a character stream, a byte stream, or a URI).
     * <p>
     * Parsers may not invoke this method while a parse is in progress.
     * Once a parse is complete, the parser may then parse another XML
     * document.
     * <p>
     * This method is synchronous: it will not return until parsing
     * has ended.  If a client application wants to terminate 
     * parsing early, it should throw an exception.
     * <p>
     * When this method returns, all characters streams and byte streams
     * opened by the parser are closed.
     * 
     * @param source The input source for the top-level of the
     *               XML document.
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     */
    public void parse(XMLInputSource inputSource) 
        throws XNIException, IOException {
        setInputSource(inputSource);
        parse(true);
    } // parse(XMLInputSource)

    // generic configuration

    /**
     * Allows a parser to add parser specific features to be recognized
     * and managed by the parser configuration.
     *
     * @param featureIds An array of the additional feature identifiers 
     *                   to be recognized.
     */
    public void addRecognizedFeatures(String[] featureIds) {
        fConfiguration.addRecognizedFeatures(featureIds);
    } // addRecognizedFeatures(String[])

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
     * Allows a parser to add parser specific properties to be recognized
     * and managed by the parser configuration.
     *
     * @param propertyIds An array of the additional property identifiers 
     *                    to be recognized.
     */
    public void addRecognizedProperties(String[] propertyIds) {
        fConfiguration.addRecognizedProperties(propertyIds);
    } // addRecognizedProperties(String[])

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

    /**
     * Sets the document handler to receive information about the document.
     * 
     * @param documentHandler The document handler.
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    /** Returns the registered document handler. */
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    /**
     * Sets the DTD handler.
     * 
     * @param dtdHandler The DTD handler.
     */
    public void setDTDHandler(XMLDTDHandler dtdHandler) {
        fConfiguration.setDTDHandler(dtdHandler);
    } // setDTDHandler(XMLDTDHandler)

    /** Returns the registered DTD handler. */
    public XMLDTDHandler getDTDHandler() {
        return fConfiguration.getDTDHandler();
    } // getDTDHandler():XMLDTDHandler

    /**
     * Sets the DTD content model handler.
     * 
     * @param dtdContentModelHandler The DTD content model handler.
     */
    public void setDTDContentModelHandler(XMLDTDContentModelHandler dtdContentModelHandler) {
        fConfiguration.setDTDContentModelHandler(dtdContentModelHandler);
    } // setDTDContentModelHandler(XMLDTDContentModelHandler)

    /** Returns the registered DTD content model handler. */
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return fConfiguration.getDTDContentModelHandler();
    } // getDTDContentModelHandler():XMLDTDContentModelHandler

    // other settings

    /**
     * Sets the entity resolver.
     *
     * @param entityResolver The new entity resolver.
     */
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        fConfiguration.setEntityResolver(entityResolver);
    } // setEntityResolver():XMLEntityResolver

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
    } // setLocale():Locale

    /** Returns the locale. */
    public Locale getLocale() {
        return fConfiguration.getLocale();
    } // getLocale():Locale

    //
    // Protected methods
    //

    protected void reset() throws XMLConfigurationException {
        fEventCollector.clear();
        fConfiguration.setDocumentHandler(fEventCollector);
        fEventDispatcher.setDocumentHandler(fDocumentHandler);
    } // reset()

} // class BufferedPullConfiguration
