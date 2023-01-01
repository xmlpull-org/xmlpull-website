/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.util;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.event.CDATAEvent;
import org.cyberneko.pull.event.CharactersEvent;
import org.cyberneko.pull.event.CommentEvent;
import org.cyberneko.pull.event.DoctypeDeclEvent;
import org.cyberneko.pull.event.DocumentEvent;
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.event.GeneralEntityEvent;
import org.cyberneko.pull.event.PrefixMappingEvent;
import org.cyberneko.pull.event.ProcessingInstructionEvent;
import org.cyberneko.pull.event.TextDeclEvent;

import java.lang.reflect.Method;
import java.util.Enumeration;

import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * This class converts XNI document handler callbacks into pull parser
 * event objects, storing them on an event queue. In order to work with
 * any kind of XNI generator, the data passed via the XNI callbacks is
 * buffered internally when converted to event objects. Therefore, this
 * class is for general use and is <em>not</em> intended for high
 * performance pull parsing. For better performance, a parser written
 * directly to the pull parser API should be used.
 * <p>
 * <strong>Note:</strong>
 * There is a performance hit to buffering the underlying XNI events.
 * While the difference is negligible for small documents, it becomes
 * more pronounced as the document size increases.
 *
 * @see EventDispatcher
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class EventCollector
    extends EventQueue
    implements XMLComponent, XMLDocumentHandler {

    //
    // Constants
    //

    // features

    /** 
     * Feature identifer: reuse buffers. 
     * <p color='red'>
     * <strong>Note:</strong>
     * This capability is not yet implemented in the Xerces2 parser.
     * Therefore, this feature identifier is a placeholder for future
     * capability to improve performance of a pull parser impl built
     * on top of Xerces2.
     * </p>
     */
    protected static final String REUSE_BUFFERS =
        "http://apache.org/xml/features/scanner/reuse-buffers";

    /** Recognized features. */
    protected static final String[] RECOGNIZED_FEATURES = {
        REUSE_BUFFERS,
    };

    /** Feature defaults. */
    protected static final Boolean[] FEATURE_DEFAULTS = {
        null,
    };

    //
    // Data
    //

    /** Event cache. */
    protected XMLEvent[] fEventCache = new XMLEvent[10];

    /** The last event. */
    protected XMLEvent fLastEvent;

    /** Underlying buffers are re-used by the creator of the XNI events. */
    protected boolean fReuseBuffers = true;

    /** XNI document source. */
    protected XMLDocumentSource fDocumentSource;

    /** Namespace context. */
    protected NamespaceContext fNamespaceContext;

    // temp vars

    /** A qualified name. */
    private final QName fQName = new QName();

    //
    // EventQueue methods
    //

    /** Removes an event from the queue. */
    public XMLEvent dequeue() {
        if (fLastEvent != null) {
            dropEvent(fLastEvent);
        }
        return fLastEvent = super.dequeue();
    } // dequeue():XMLEvent

    //
    // XMLComponent methods
    //

    /** Reset. */
    public void reset(XMLComponentManager manager) throws XMLConfigurationException {
        clear();
        fLastEvent = null;
        // REVISIT: This functionality is not implemented in the Xerces2
        //          reference implementation of XNI, yet. -Ac
        try {
            fReuseBuffers = manager.getFeature(REUSE_BUFFERS);
        }
        catch (Exception e) {
            fReuseBuffers = true;
        }
    } // reset(XMLComponentManager)

    /** Returns recognized features. */
    public String[] getRecognizedFeatures() {
        return RECOGNIZED_FEATURES;
    } // getRecognizedFeatures():String[]

    /** Set feature. */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {} // setFeature(String,boolean)

    /** Returns recognized properties. */
    public String[] getRecognizedProperties() {
        return null;
    } // getRecognizedProperties():String[]

    /** Set property. */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {} // setProperty(String,Object)

    // since Xerces-J 2.2.0

    /** Returns feature default. */
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    /** Returns property default. */
    public Object getPropertyDefault(String propertyId) {
        return null;
    } // getPropertyDefault(String):Object

    //
    // XMLDocumentHandler methods
    //

    // since Xerces-J 2.2.0

    /** Start document. */
    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext nscontext, Augmentations augs) 
        throws XNIException {
        fNamespaceContext = nscontext;
        DocumentEvent event = (DocumentEvent)getEvent(XMLEvent.DOCUMENT);
        event.start = true;
        event.locator = locator;
        event.encoding = encoding;
        event.augs = augs(augs);
        enqueue(event);
    } // startDocument(XMLLocator,String,NamespaceContext,Augmentations)

    // old methods

    /**
     * The start of the document.
     * 
     * @param locator  The document locator, or null if the document
     *                 location cannot be reported during the parsing
     *                 of this document. However, it is <em>strongly</em>
     *                 recommended that a locator be supplied that can
     *                 at least report the system identifier of the
     *                 document.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) 
        throws XNIException {
        startDocument(locator, encoding, null, augs);
    } // startDocument(XMLLocator,String,Augmentations)

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     * 
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException {
        TextDeclEvent event = (TextDeclEvent)getEvent(XMLEvent.TEXT_DECL);
        event.xmldecl = true;
        event.version = version;
        event.encoding = encoding;
        event.standalone = standalone;
        event.augs = augs(augs);
        enqueue(event);
    } // xmlDecl(String,String,String,Augmentations)

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     * 
     * @param rootElement
     *                 The name of the root element.
     * @param publicId The public identifier if an external DTD or null
     *                 if the external DTD is specified using SYSTEM.
     * @param systemId The system identifier if an external DTD, null
     *                 otherwise.
     * @param augs     Additional information that may include infoset augmentations
     *                 
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
        throws XNIException {
        DoctypeDeclEvent event = new DoctypeDeclEvent();
        event.root = rootElement;
        event.pubid = publicId;
        event.sysid = systemId;
        event.augs = augs(augs);
        enqueue(event);
    } // doctypeDecl(String,String,String,Augmentations)

    /**
     * A comment.
     * 
     * @param text   The text in the comment.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by application to signal an error.
     */
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        CommentEvent event = (CommentEvent)getEvent(XMLEvent.COMMENT);
        event.text = string(text, false);
        event.augs = augs(augs);
        enqueue(event);
    } // comment(XMLString,Augmentations)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     * 
     * @param target The target.
     * @param data   The data or null if none specified.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException {
        ProcessingInstructionEvent event = 
            (ProcessingInstructionEvent)getEvent(XMLEvent.PROCESSING_INSTRUCTION);
        event.target = target;
        event.data = string(data, false);
        event.augs = augs(augs);
        enqueue(event);
    } // processingInstruction(String,XMLString,Augmentations)

    /**
     * The start of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     * @param uri    The URI bound to the prefix.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startPrefixMapping(String prefix, String uri, Augmentations augs)
        throws XNIException {
        PrefixMappingEvent event = (PrefixMappingEvent)getEvent(XMLEvent.PREFIX_MAPPING);
        event.start = true;
        event.prefix = prefix;
        event.uri = uri;
        event.augs = augs(augs);
        enqueue(event);
    } // startPrefixMapping(String,String,Augmentations)

    /**
     * The start of an element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        handleStartElement(element, attributes, augs, false);
    } // startElement(QName,XMLAttributes,Augmentations)

    /**
     * An empty element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        handleStartElement(element, attributes, augs, true);
        handleEndElement(element, augs, true);
    } // emptyElement(QName,XMLAttributes,Augmentations)

    /**
     * This method notifies the start of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name     The name of the general entity.
     * @param identifier The resource identifier.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     *                 
     * @exception XNIException Thrown by handler to signal an error.
     */
    public void startGeneralEntity(String name, 
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException {
        GeneralEntityEvent event = (GeneralEntityEvent)getEvent(XMLEvent.GENERAL_ENTITY);
        event.start = true;
        event.name = name;
        try {
            Method method = event.getClass().getMethod("getNamespace", null);
            event.namespace = (String)method.invoke(identifier, null);
        }
        catch (Exception e) {
            // old version of Xerces -- ignore
        }
        if (identifier != null) {
            event.publicId = identifier.getPublicId();
            event.baseSystemId = identifier.getBaseSystemId();
            event.literalSystemId = identifier.getLiteralSystemId();
            event.expandedSystemId = identifier.getExpandedSystemId();
        }
        event.encoding = encoding;
        event.augs = augs(augs);
        enqueue(event);
    } // startGeneralEntity(String,XMLResourceIdentifier,String,Augmentations)
    
    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     * @param augs     Additional information that may include infoset augmentations
     *                 
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
        TextDeclEvent event = (TextDeclEvent)getEvent(XMLEvent.TEXT_DECL);
        event.xmldecl = false;
        event.version = version;
        event.encoding = encoding;
        event.augs = augs(augs);
        enqueue(event);
    } // textDecl(String,String,Augmentations)

    /**
     * This method notifies the end of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name   The name of the entity.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endGeneralEntity(String name, Augmentations augs) throws XNIException {
        GeneralEntityEvent event = (GeneralEntityEvent)getEvent(XMLEvent.GENERAL_ENTITY);
        event.start = false;
        event.name = name;
        event.publicId = null;
        event.baseSystemId = null;
        event.literalSystemId = null;
        event.expandedSystemId = null;
        event.encoding = null;
        event.augs = augs(augs);
        enqueue(event);
    } // endGeneralEntity(String,Augmentations)

    /**
     * Character content.
     * 
     * @param text   The content.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        CharactersEvent event = (CharactersEvent)getEvent(XMLEvent.CHARACTERS);
        event.ignorable = false;
        event.text = string(text, true);
        event.augs = augs(augs);
        enqueue(event);
    } // characters(XMLString,Augmentations)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     * 
     * @param text   The ignorable whitespace.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        CharactersEvent event = (CharactersEvent)getEvent(XMLEvent.CHARACTERS);
        event.ignorable = true;
        event.text = string(text, true);
        event.augs = augs(augs);
        enqueue(event);
    } // ignorableWhitespace(XMLString,Augmentations)

    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     * @param augs    Additional information that may include infoset augmentations
     *                
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endElement(QName element, Augmentations augs) throws XNIException {
        handleEndElement(element, augs, false);
    } // endElement(QName,Augmentations)

    /**
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix, Augmentations augs) throws XNIException {
        PrefixMappingEvent event = (PrefixMappingEvent)getEvent(XMLEvent.PREFIX_MAPPING);
        event.start = false;
        event.prefix = prefix;
        event.uri = null;
        event.augs = augs(augs);
        enqueue(event);
    } // endPrefixMapping(String,Augmentations)

    /**
     * The start of a CDATA section.
     * 
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startCDATA(Augmentations augs) throws XNIException {
        CDATAEvent event = (CDATAEvent)getEvent(XMLEvent.CDATA);
        event.start = true;
        event.augs = augs(augs);
        enqueue(event);
    } // startCDATA(Augmentations)

    /**
     * The end of a CDATA section.
     * 
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endCDATA(Augmentations augs) throws XNIException {
        CDATAEvent event = (CDATAEvent)getEvent(XMLEvent.CDATA);
        event.start = false;
        event.augs = augs(augs);
        enqueue(event);
    } // endCDATA(Augmentations)

    /**
     * The end of the document.
     * 
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endDocument(Augmentations augs) throws XNIException {
        DocumentEvent event = (DocumentEvent)getEvent(XMLEvent.DOCUMENT);
        event.start = false;
        event.locator = null;
        event.encoding = null;
        event.augs = augs(augs);
        enqueue(event);
    } // endDocument(Augmentations)

    // @since Xerces 2.1.0

    /** Sets the document source. */
    public void setDocumentSource(XMLDocumentSource source) {
        fDocumentSource = source;
    } // setDocumentSource(XMLDocumentSource)

    /** Returns the document source. */
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    } // getDocumentSource():XMLDocumentSource

    //
    // Protected methods
    //

    /** Handles a start element by copying the necessary data. */
    protected void handleStartElement(QName element, XMLAttributes attributes, 
                                      Augmentations augs, boolean empty) {

        // handle namespace prefixes
        if (fNamespaceContext != null) {
            int count = fNamespaceContext.getDeclaredPrefixCount();
            for (int i = 0; i < count; i++) {
                String prefix = fNamespaceContext.getDeclaredPrefixAt(i);
                String uri = fNamespaceContext.getURI(prefix);
                startPrefixMapping(prefix, uri, null);
            }
        }

        // enqueue start element event
        ElementEvent event = (ElementEvent)getEvent(XMLEvent.ELEMENT);
        event.start = true;
        if (event.element == null) {
            event.element = new QName();
        }
        event.element.setValues(element);
        if (event.attributes == null) {
            event.attributes = new XMLAttributesImpl();
        }
        attrs(attributes, event.attributes);
        event.empty = empty;
        event.augs = augs(augs);
        enqueue(event);

    } // handleStartElement(QName,XMLAttributes,Augmentations,boolean)

    /** Handles an end element by copying the necessary data. */
    protected void handleEndElement(QName element, Augmentations augs, 
                                    boolean empty) {
        
        // enqueue end element event
        ElementEvent event = (ElementEvent)getEvent(XMLEvent.ELEMENT);
        event.start = false;
        if (event.element == null) {
            event.element = new QName();
        }
        event.element.setValues(element);
        /***
        // REVISIT: [Q] What is the best way to handle this? Should I
        //              keep a list of unused attributes objects? -Ac
        if (event.attributes != null) {
            event.attributes.removeAllAttributes();
        }
        /***/
        event.attributes = null;
        /***/
        event.empty = empty;
        event.augs = augs(augs);
        enqueue(event);

        // handle namespace prefixes
        if (fNamespaceContext != null) {
            int count = fNamespaceContext.getDeclaredPrefixCount();
            for (int i = count - 1; i >= 0; i--) {
                String prefix = fNamespaceContext.getDeclaredPrefixAt(i);
                endPrefixMapping(prefix, null);
            }
        }

    } // handleEndElement(QName,Augmentations,boolean)

    /** Returns a copy of the specified args. */
    protected Augmentations augs(Augmentations augs) {
        Augmentations newaugs = null;
        if (augs != null) {
            newaugs = new AugmentationsImpl();
            Enumeration keys = augs.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                newaugs.putItem(key, augs.getItem(key));
            }
        }
        return newaugs;
    } // augs(Augmentations):Augmentations

    /** Copies the source attributes into the specified destination. */
    protected void attrs(XMLAttributes src, XMLAttributes dest) {
        int attrCount = src != null ? src.getLength() : 0;
        for (int i = 0; i < attrCount; i++) {
            src.getName(i, fQName);
            String atype = src.getType(i);
            String avalue = src.getValue(i);
            String anonvalue = src.getNonNormalizedValue(i);
            boolean specified = src.isSpecified(i);
            dest.addAttribute(fQName, atype, avalue);
            dest.setNonNormalizedValue(i, anonvalue);
            dest.setSpecified(i, specified);
        }
    } // attrs(XMLAttributes,XMLAttributes)

    /** Returns a copy of the specified string. */
    protected XMLString string(XMLString text, boolean content) {
        return fReuseBuffers ? new XMLStringBuffer(text) : text;
    } // string(XMLString):XMLString

    /** 
     * Returns an event object for the given type. If an un-used event object
     * of this type is on the event cache, it is returned. Otherwise, a new
     * event object is created.
     */
    protected XMLEvent getEvent(short type) {
        XMLEvent event = fEventCache[type];
        if (event != null) {
            fEventCache[type] = event.next;
            event.next = null;
            return event;
        }
        switch (type) {
            case XMLEvent.ELEMENT: return new ElementEvent();
            case XMLEvent.CHARACTERS: return new CharactersEvent();
            case XMLEvent.PREFIX_MAPPING: return new PrefixMappingEvent();
            case XMLEvent.GENERAL_ENTITY: return new GeneralEntityEvent();
            case XMLEvent.DOCUMENT: return new DocumentEvent();
            case XMLEvent.COMMENT: return new CommentEvent();
            case XMLEvent.PROCESSING_INSTRUCTION: return new ProcessingInstructionEvent();
            case XMLEvent.CDATA: return new CDATAEvent();
            case XMLEvent.TEXT_DECL: return new TextDeclEvent();
            case XMLEvent.DOCTYPE_DECL: return new DoctypeDeclEvent();
        }
        throw new RuntimeException("should not happen! getEvent("+type+')');
    } // getEvent(short):Event

    /** 
     * Drops an event by putting it back on the event cache so that it
     * can be re-used when needed.
     */
    protected void dropEvent(XMLEvent event) {
        event.next = fEventCache[event.type];
        fEventCache[event.type] = event;
        if (event.type == XMLEvent.ELEMENT) {
            ElementEvent elementEvent = (ElementEvent)event;
            if (elementEvent.attributes != null) {
                elementEvent.attributes.removeAllAttributes();
            }
        }
    } // dropEvent(Event)

} // class EventCollector
