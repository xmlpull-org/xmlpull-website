/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package sample;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLPullParser;
import org.cyberneko.pull.event.BoundedEvent;
import org.cyberneko.pull.event.CharactersEvent;
import org.cyberneko.pull.event.CommentEvent;
import org.cyberneko.pull.event.DoctypeDeclEvent;
import org.cyberneko.pull.event.DocumentEvent;
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.event.GeneralEntityEvent;
import org.cyberneko.pull.event.PrefixMappingEvent;
import org.cyberneko.pull.event.ProcessingInstructionEvent;
import org.cyberneko.pull.event.TextDeclEvent;
import org.cyberneko.pull.parsers.Xerces2;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * A simple document tracer program to show the use of the pull parsing 
 * API. This program is useful for seeing the contents of event objects
 * returned by a pull parser implementation.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class DocumentTracer {

    //
    // Constructors
    //

    /** This class cannot be constructed. */
    private DocumentTracer() {} // <init>()

    //
    // Public static methods
    //

    /** 
     * Traces the event callbacks.
     *
     * @param parser The pull parser to use.
     * @param sysid  The system id of the document to read.
     */
    public static void trace(XMLPullParser parser, String sysid) 
        throws XNIException, IOException {

        // variables
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, "UTF8"), true);
        int indent = 0;

        // initialize parser
        XMLInputSource source = new XMLInputSource(null, sysid, null);
        parser.setInputSource(source);

        // parse document
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            BoundedEvent boundedEvent = event instanceof BoundedEvent
                                      ? (BoundedEvent)event : null;
            if (event.type != XMLEvent.PREFIX_MAPPING &&
                boundedEvent != null && !boundedEvent.start) {
                indent--;
            }
            printWithIndent(out, indent, "event.type="+toString(event));
            if (boundedEvent != null) {
                printWithIndent(out, indent, "     .start="+boundedEvent.start);
            }
            switch (event.type) {
                case XMLEvent.ELEMENT: {
                    ElementEvent elementEvent = (ElementEvent)event;
                    printWithIndent(out, indent, "     .element="+elementEvent.element);
                    printWithIndent(out, indent, "     .start="+elementEvent.start);
                    printWithIndent(out, indent, "     .empty="+elementEvent.empty);
                    XMLAttributes attrs = elementEvent.attributes;
                    int attrCount = attrs != null ? attrs.getLength() : 0;
                    for (int i = 0; i < attrCount; i++) {
                        printWithIndent(out, indent, "     .attributes["+i+"].qname="+attrs.getQName(i));
                        printWithIndent(out, indent, "     .attributes["+i+"].value=\""+attrs.getValue(i)+'"');
                    }
                    break;
                }
                case XMLEvent.CHARACTERS: {
                    CharactersEvent charactersEvent = (CharactersEvent)event;
                    printWithIndent(out, indent, "     .text=\""+escape(charactersEvent.text.toString())+'"');
                    printWithIndent(out, indent, "     .ignorable="+charactersEvent.ignorable);
                    break;
                }
                case XMLEvent.PREFIX_MAPPING: {
                    PrefixMappingEvent prefixMapEvent = (PrefixMappingEvent)event;
                    printWithIndent(out, indent, "     .prefix=\""+prefixMapEvent.prefix+'"');
                    if (prefixMapEvent.uri != null) {
                        printWithIndent(out, indent, "     .uri=\""+prefixMapEvent.uri+'"');
                    }
                    break;
                }
                case XMLEvent.COMMENT: {
                    CommentEvent commentEvent = (CommentEvent)event;
                    printWithIndent(out, indent, "     .text=\""+escape(commentEvent.text.toString())+'"');
                    break;
                }
                case XMLEvent.PROCESSING_INSTRUCTION: {
                    ProcessingInstructionEvent piEvent = (ProcessingInstructionEvent)event;
                    printWithIndent(out, indent, "     .target=\""+piEvent.target+"'");
                    if (piEvent.data != null) {
                        printWithIndent(out, indent, "     .data=\""+piEvent.data+'"');
                    }
                    break;
                }
                case XMLEvent.DOCUMENT: {
                    DocumentEvent documentEvent = (DocumentEvent)event;
                    if (documentEvent.locator != null) {
                        printWithIndent(out, indent, "     .locator="+documentEvent.locator);
                    }
                    if (documentEvent.encoding != null) {
                        printWithIndent(out, indent, "     .encoding=\""+documentEvent.encoding+'"');
                    }
                    break;
                }
                case XMLEvent.DOCTYPE_DECL: {
                    DoctypeDeclEvent doctypeEvent = (DoctypeDeclEvent)event;
                    printWithIndent(out, indent, "     .root=\""+doctypeEvent.root+'"');
                    if (doctypeEvent.pubid != null) {
                        printWithIndent(out, indent, "     .pubid=\""+doctypeEvent.pubid+'"');
                    }
                    printWithIndent(out, indent, "     .sysid=\""+doctypeEvent.sysid+'"');
                    break;
                }
                case XMLEvent.GENERAL_ENTITY: {
                    GeneralEntityEvent geEvent = (GeneralEntityEvent)event;
                    printWithIndent(out, indent, "     .name="+geEvent.name);
                    if (geEvent.publicId != null) {
                        printWithIndent(out, indent, "     .publicId=\""+geEvent.publicId+'"');
                    }
                    if (geEvent.literalSystemId != null) {
                        printWithIndent(out, indent, "     .baseSystemId=\""+geEvent.baseSystemId+'"');
                        printWithIndent(out, indent, "     .literalSystemId=\""+geEvent.literalSystemId+'"');
                        printWithIndent(out, indent, "     .expandedSystemId=\""+geEvent.expandedSystemId+'"');
                    }
                    if (geEvent.encoding != null) {
                        printWithIndent(out, indent, "     .encoding=\""+geEvent.encoding+'"');
                    }
                    break;
                }
                case XMLEvent.TEXT_DECL: {
                    TextDeclEvent textDeclEvent = (TextDeclEvent)event;
                    printWithIndent(out, indent, "     .xmldecl="+textDeclEvent.xmldecl);
                    if (textDeclEvent.version != null) {
                        printWithIndent(out, indent, "     .version=\""+textDeclEvent.version+'"');
                    }
                    if (textDeclEvent.encoding != null) {
                        printWithIndent(out, indent, "     .encoding=\""+textDeclEvent.encoding+'"');
                    }
                    if (textDeclEvent.standalone != null) {
                        printWithIndent(out, indent, "     .standalone=\""+textDeclEvent.standalone+'"');
                    }
                    break;
                }
            }
            if (event.augs != null) {
                printWithIndent(out, indent, "     .augs="+event.augs);
            }
            if (event.next != null) {
                printWithIndent(out, indent, "     .next="+event.next);
            }
            if (event.type != XMLEvent.PREFIX_MAPPING &&
                boundedEvent != null && boundedEvent.start) {
                indent++;
            }
        }

    } // trace(XMLPullParser,String)

    //
    // Protected static methods
    //

    /** Prints with the appropriate indent. */
    protected static void printWithIndent(PrintWriter out, int indent, String s) {
        for (int i = 0; i < indent; i++) {
            out.print(' ');
        }
        out.println(s);
    } // printWithIndent(PrintWriter,String)

    /** Escapes a string. */
    protected static String escape(String s) {
        StringBuffer str = new StringBuffer();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '"') {
                str.append('\\');
            }
            str.append(c);
        }
        return str.toString();
    } // escape(String):String

    /** Returns a string representation of the event type. */
    protected static String toString(XMLEvent event) {
        String name = event.getClass().getName();
        switch (event.type) {
            case XMLEvent.CDATA: return "CDATA ("+name+")";
            case XMLEvent.CHARACTERS: return "CHARACTERS ("+name+")";
            case XMLEvent.COMMENT: return "COMMENT ("+name+")";
            case XMLEvent.DOCTYPE_DECL: return "DOCTYPE_DECL ("+name+")";
            case XMLEvent.DOCUMENT: return "DOCUMENT ("+name+")";
            case XMLEvent.ELEMENT: return "ELEMENT ("+name+")";
            case XMLEvent.GENERAL_ENTITY: return "GENERAL_ENTITY ("+name+")";
            case XMLEvent.PREFIX_MAPPING: return "PREFIX_MAPPING ("+name+")";
            case XMLEvent.PROCESSING_INSTRUCTION: return "PROCESSING_INSTRUCTION ("+name+")";
            case XMLEvent.TEXT_DECL: return "TEXT_DECL ("+name+")";
        }
        return "??? ("+name+","+event.type+")";
    } // toString(short):String

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        XMLPullParser parser = new Xerces2();
        for (int i = 0; i < argv.length; i++) {
            DocumentTracer.trace(parser, argv[i]);
        }
    } // main(String[])

} // class DocumentTracer
