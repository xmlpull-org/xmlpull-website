/* 
 * (C) Copyright 2002, Andy Clark.  All rights reserved.
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

import org.apache.xerces.xni.XNIException;

/**
 * 
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class DefaultHandler {

    //
    // Public methods
    //

    public void handleEvent(XMLEvent event) throws XNIException {
        switch (event.type) {
            case XMLEvent.CDATA: {
                CDATAEvent cdataEvent = (CDATAEvent)event;
                if (cdataEvent.start) {
                    handleStartCDATA(cdataEvent);
                }
                else {
                    handleEndCDATA(cdataEvent);
                }
                break;
            }
            case XMLEvent.CHARACTERS: {
                CharactersEvent charsEvent = (CharactersEvent)event;
                if (charsEvent.ignorable) {
                    handleIgnorableWhitespace(charsEvent);
                }
                else {
                    handleCharacters(charsEvent);
                }
                break;
            }
            case XMLEvent.COMMENT: {
                CommentEvent commentEvent = (CommentEvent)event;
                handleComment(commentEvent);
                break;
            }
            case XMLEvent.DOCTYPE_DECL: {
                DoctypeDeclEvent doctypeEvent = (DoctypeDeclEvent)event;
                handleDoctypeDecl(doctypeEvent);
                break;
            }
            case XMLEvent.DOCUMENT: {
                DocumentEvent documentEvent = (DocumentEvent)event;
                if (documentEvent.start) {
                    handleStartDocument(documentEvent);
                }
                else {
                    handleEndDocument(documentEvent);
                }
                break;
            }
            case XMLEvent.ELEMENT: {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    handleStartElement(elementEvent);
                }
                else {
                    handleEndElement(elementEvent);
                }
                break;
            }
            case XMLEvent.GENERAL_ENTITY: {
                GeneralEntityEvent geEvent = (GeneralEntityEvent)event;
                if (geEvent.start) {
                    handleStartGeneralEntity(geEvent);
                }
                else {
                    handleEndGeneralEntity(geEvent);
                }
                break;
            }
            default: {
                handleUnknownEvent(event);
            }
        }
    } // handleEvent(XMLEvent)

    public void handleStartCDATA(CDATAEvent event) throws XNIException {}
    public void handleEndCDATA(CDATAEvent event) throws XNIException {}

    public void handleIgnorableWhitespace(CharactersEvent event) throws XNIException {}
    public void handleCharacters(CharactersEvent event) throws XNIException {}

    public void handleComment(CommentEvent event) throws XNIException {}

    public void handleDoctypeDecl(DoctypeDeclEvent event) throws XNIException {}

    public void handleStartDocument(DocumentEvent event) throws XNIException {}
    public void handleEndDocument(DocumentEvent event) throws XNIException {}

    public void handleStartElement(ElementEvent event) throws XNIException {}
    public void handleEndElement(ElementEvent event) throws XNIException {}

    public void handleStartGeneralEntity(GeneralEntityEvent event) throws XNIException {}
    public void handleEndGeneralEntity(GeneralEntityEvent event) throws XNIException {}

    public void handleUnknownEvent(XMLEvent event) throws XNIException {
        throw new XNIException("unknown event type ("+event.type+')');
    } // handleUnknownEvent(XMLEvent)

} // class DefaultHandler
