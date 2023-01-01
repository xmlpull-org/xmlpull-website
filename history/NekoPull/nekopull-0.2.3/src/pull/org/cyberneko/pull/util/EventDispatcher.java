/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * This class converts pull parser event objects into XNI document handler
 * callbacks. The user of this class is responsible for queueing of the
 * event objects and should call <code>dispatchEvent</code> for each event
 * to be delivered via the XNI document handler callbacks.
 *
 * @see EventCollector
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class EventDispatcher
    implements XMLDocumentSource {

    //
    // Data
    //

    /** The document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    // temp vars

    /** A resource identifier proxy. */
    private final ResourceIdentifierProxy fResourceIdentifierProxy = 
        new ResourceIdentifierProxy();

    //
    // XMLDocumentSource methods
    //

    /** Sets the document handler. */
    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    } // setDocumentHandler(XMLDocumentHandler)

    // @since Xerces 2.1.0

    /** Returns the document handler. */
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    //
    // Public methods
    //

    /**
     * Dispatches a pull parser event object by calling the appropriate
     *
     * @param event The pull parser event to deliver.
     *
     * @throws XNIException Thrown by the handler to signal an error.
     */
    public void dispatchEvent(XMLEvent event) throws XNIException {

        // is there anything to do?
        if (fDocumentHandler == null) {
            return;
        }

        // dispatch event
        switch (event.type) {
            
            // start/empty/endElement
            case XMLEvent.ELEMENT: {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    if (elementEvent.empty) {
                        fDocumentHandler.emptyElement(elementEvent.element,
                                                      elementEvent.attributes,
                                                      elementEvent.augs);
                    }
                    else {
                        fDocumentHandler.startElement(elementEvent.element,
                                                      elementEvent.attributes,
                                                      elementEvent.augs);
                    }
                }
                else if (!elementEvent.empty) {
                    fDocumentHandler.endElement(elementEvent.element,
                                                elementEvent.augs);
                }
                break;
            }
            
            // characters, ignorableWhitespace
            case XMLEvent.CHARACTERS: {
                CharactersEvent charactersEvent = (CharactersEvent)event;
                if (charactersEvent.ignorable) {
                    fDocumentHandler.ignorableWhitespace(charactersEvent.text,
                                                         charactersEvent.augs);
                }
                else {
                    fDocumentHandler.characters(charactersEvent.text,
                                                charactersEvent.augs);
                }
                break;
            }
            
            // start/endPrefixMapping
            case XMLEvent.PREFIX_MAPPING: {
                PrefixMappingEvent prefixMappingEvent = (PrefixMappingEvent)event;
                if (prefixMappingEvent.start) {
                    Class cls = fDocumentHandler.getClass();
                    Class[] types = { String.class, String.class, Augmentations.class };
                    try {
                        Method method = cls.getMethod("startPrefixMapping", types);
                        Object[] args = { prefixMappingEvent.prefix,
                                          prefixMappingEvent.uri,
                                          prefixMappingEvent.augs };
                        method.invoke(fDocumentHandler, args);
                    }
                    catch (NoSuchMethodException e) {
                        // ignore
                    }
                    catch (IllegalAccessException e) {
                        // ignore
                    }
                    catch (InvocationTargetException e) {
                        // ignore
                    }
                }
                else {
                    Class cls = fDocumentHandler.getClass();
                    Class[] types = { String.class, String.class, Augmentations.class };
                    try {
                        Method method = cls.getMethod("endPrefixMapping", types);
                        Object[] args = { prefixMappingEvent.prefix,
                                          prefixMappingEvent.augs };
                        method.invoke(fDocumentHandler, args);
                    }
                    catch (NoSuchMethodException e) {
                        // ignore
                    }
                    catch (IllegalAccessException e) {
                        // ignore
                    }
                    catch (InvocationTargetException e) {
                        // ignore
                    }
                }
                break;
            }
            
            // start/endCDATA
            case XMLEvent.CDATA: {
                CDATAEvent cdataEvent = (CDATAEvent)event;
                if (cdataEvent.start) {
                    fDocumentHandler.startCDATA(cdataEvent.augs);
                }
                else {
                    fDocumentHandler.endCDATA(cdataEvent.augs);
                }
                break;
            }
            
            // start/endGeneralEntity
            case XMLEvent.GENERAL_ENTITY: {
                GeneralEntityEvent generalEntityEvent = (GeneralEntityEvent)event;
                if (generalEntityEvent.start) {
                    fResourceIdentifierProxy.setGeneralEntityEvent(generalEntityEvent);
                    fDocumentHandler.startGeneralEntity(generalEntityEvent.name,
                                                        fResourceIdentifierProxy,
                                                        generalEntityEvent.encoding,
                                                        generalEntityEvent.augs);
                }
                else {
                    fDocumentHandler.endGeneralEntity(generalEntityEvent.name,
                                                      generalEntityEvent.augs);
                }
                break;
            }
            
            // comment
            case XMLEvent.COMMENT: {
                CommentEvent commentEvent = (CommentEvent)event;
                fDocumentHandler.comment(commentEvent.text, commentEvent.augs);
                break;
            }
            
            // processingInstruction
            case XMLEvent.PROCESSING_INSTRUCTION: {
                ProcessingInstructionEvent processingInstructionEvent = (ProcessingInstructionEvent)event;
                fDocumentHandler.processingInstruction(processingInstructionEvent.target, 
                                                       processingInstructionEvent.data,
                                                       processingInstructionEvent.augs);
                break;
            }
            
            // xmlDecl, textDecl
            case XMLEvent.TEXT_DECL: {
                TextDeclEvent textDeclEvent = (TextDeclEvent)event;
                if (textDeclEvent.xmldecl) {
                    fDocumentHandler.xmlDecl(textDeclEvent.version,
                                             textDeclEvent.encoding,
                                             textDeclEvent.standalone,
                                             textDeclEvent.augs);
                }
                else {
                    fDocumentHandler.textDecl(textDeclEvent.version,
                                              textDeclEvent.encoding,
                                              textDeclEvent.augs);
                }
                break;
            }
            
            // doctypeDecl
            case XMLEvent.DOCTYPE_DECL: {
                DoctypeDeclEvent doctypeDeclEvent = (DoctypeDeclEvent)event;
                fDocumentHandler.doctypeDecl(doctypeDeclEvent.root,
                                             doctypeDeclEvent.pubid,
                                             doctypeDeclEvent.sysid,
                                             doctypeDeclEvent.augs);
                break;
            }
            
            // start/endDocument
            case XMLEvent.DOCUMENT: {
                DocumentEvent documentEvent = (DocumentEvent)event;
                if (documentEvent.start) {
                    XMLLocator locator = documentEvent.locator;
                    String encoding = documentEvent.encoding;
                    NamespaceContext nscontext = null;
                    Augmentations augs = documentEvent.augs;
                    try {
                        // NOTE: Hack to allow the default filter to work with
                        //       old and new versions of the XNI document handler
                        //       interface. -Ac
                        Class cls = fDocumentHandler.getClass();
                        Class[] types = {
                            XMLLocator.class, String.class,
                            NamespaceContext.class, Augmentations.class
                        };
                        Method method = cls.getMethod("startDocument", types);
                        Object[] params = {
                            locator, encoding, 
                            nscontext, augs
                        };
                        method.invoke(fDocumentHandler, params);
                    }
                    catch (XNIException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        try {
                            // NOTE: Hack to allow the default filter to work with
                            //       old and new versions of the XNI document handler
                            //       interface. -Ac
                            Class cls = fDocumentHandler.getClass();
                            Class[] types = {
                                XMLLocator.class, String.class, Augmentations.class
                            };
                            Method method = cls.getMethod("startDocument", types);
                            Object[] params = {
                                locator, encoding, augs
                            };
                            method.invoke(fDocumentHandler, params);
                        }
                        catch (XNIException ex) {
                            throw ex;
                        }
                        catch (Exception ex) {
                            // NOTE: Should never reach here!
                            throw new XNIException(ex);
                        }
                    }
                }
                else {
                    fDocumentHandler.endDocument(documentEvent.augs);
                }
                break;
            }
            
            // error
            default: {
                throw new XNIException("unknown event type ("+event.type+')');
            }
        }

    } // dispatchEvent(XMLEvent)

    //
    // Classes
    //

    /**
     * A proxy object for resource identifier passed to the start general
     * entity method in the XNI document handler.
     *
     * @author Andy Clark
     */
    public static class ResourceIdentifierProxy 
        implements XMLResourceIdentifier {

        //
        // Data
        //

        /** The general entity event to be proxied. */
        protected GeneralEntityEvent fEvent;

        //
        // Public methods
        //

        /** Sets the general entity event. */
        public void setGeneralEntityEvent(GeneralEntityEvent event) {
            fEvent = event;
        } // setGeneralEntityEvent(GeneralEntityEvent)

        //
        // XMLResourceIdentifier methods
        //

        /** Returns the public identifier. */
        public String getPublicId() {
            return fEvent.publicId;
        } // getPublicId():String

        /** Returns the base system identifier. */
        public String getBaseSystemId() {
            return fEvent.baseSystemId;
        } // getBaseSystemId():String

        /** Returns the literal system identifier. */
        public String getLiteralSystemId() {
            return fEvent.literalSystemId;
        } // getLiteralSystemId():String

        /** Returns the expanded system identifier. */
        public String getExpandedSystemId() {
            return fEvent.expandedSystemId;
        } // getExpandedSystemId():String

        // @since Xerces-J 2.3.0

        /** Sets the public identifier. */
        public void setPublicId(String publicId) {
            fEvent.publicId = publicId;
        } // setPublicId(String)

        /** Sets the base system identifier. */
        public void setBaseSystemId(String baseSystemId) {
            fEvent.baseSystemId = baseSystemId;
        } // setBaseSystemId(String)

        /** Sets the literal system identifier. */
        public void setLiteralSystemId(String literalSystemId) {
            fEvent.literalSystemId = literalSystemId;
        } // setLiteralSystemId(String)

        /** Sets the expanded system identifier. */
        public void setExpandedSystemId(String expandedSystemId) {
            fEvent.expandedSystemId = expandedSystemId;
        } // setExpandedSystemId(String)

        // @since Xerces-J 2.4.0

        /** Returns the namespace. */
        public String getNamespace() {
            return fEvent.namespace;
        } // getNamespace():String

        /** Sets the namespace. */
        public void setNamespace(String namespace) {
            fEvent.namespace = namespace;
        } // getNamespace(String)

    } // class ResourceIdentifierProxy

} // class EventDispatcher
