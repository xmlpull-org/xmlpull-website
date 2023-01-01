/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package sample;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLPullParser;
import org.cyberneko.pull.event.CharactersEvent;
import org.cyberneko.pull.event.CommentEvent;
import org.cyberneko.pull.event.DoctypeDeclEvent;
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.event.ProcessingInstructionEvent;
import org.cyberneko.pull.parsers.Xerces2;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * A simple writer program to show the use of the pull parsing API. This
 * program serializes event objects back to XML syntax.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class Writer {

    //
    // Constructors
    //

    /** This class cannot be constructed. */
    private Writer() {} // <init>()

    //
    // Public static methods
    //

    /** 
     * Writes a document's content to standard output.
     *
     * @param parser The pull parser to use.
     * @param sysid  The system id of the document to read.
     */
    public static void write(XMLPullParser parser, String sysid) 
        throws XNIException, IOException {

        // variables
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, "UTF8"), true);
        boolean seenRootElement = false;
        int elementDepth = 0;

        // initialize parser
        XMLInputSource source = new XMLInputSource(null, sysid, null);
        parser.setInputSource(source);

        // parse document
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            switch (event.type) {
                case XMLEvent.ELEMENT: {
                    ElementEvent elementEvent = (ElementEvent)event;
                    seenRootElement = true;
                    if (elementEvent.start) {
                        out.print('<');
                        elementDepth++;
                        out.print(elementEvent.element.rawname);
                        XMLAttributes attrs = elementEvent.attributes;
                        int attrCount = attrs != null ? attrs.getLength() : 0;
                        for (int i = 0; i < attrCount; i++) {
                            out.print(' ');
                            out.print(attrs.getQName(i));
                            out.print("=\"");
                            String avalue = attrs.getValue(i);
                            int length = avalue.length();
                            for (int j = 0; j < length; j++) {
                                char c = avalue.charAt(j);
                                switch (c) {
                                    case '<': {
                                        out.print("&lt;");
                                        break;
                                    }
                                    case '"': {
                                        out.print("&quot;");
                                        break;
                                    }
                                    default: {
                                        out.print(c);
                                    }
                                }
                            }
                            out.print('"');
                        }
                        if (elementEvent.empty) {
                            elementDepth--;
                            out.print('/');
                        }
                        out.print('>');
                        out.flush();
                    }
                    else if (!elementEvent.empty) {
                        elementDepth--;
                        out.print("</");
                        out.print(elementEvent.element.rawname);
                        out.print('>');
                        out.flush();
                    }
                    break;
                }
                case XMLEvent.CHARACTERS: {
                    CharactersEvent charactersEvent = (CharactersEvent)event;
                    XMLString text = charactersEvent.text;
                    for (int i = 0; i < text.length; i++) {
                        char c = text.ch[text.offset + i];
                        switch (c) {
                            case '<': {
                                out.print("&lt;");
                                break;
                            }
                            case '>': {
                                out.print("&gt;");
                                break;
                            }
                            case '&': {
                                out.print("&amp;");
                                break;
                            }
                            default: {
                                out.print(c);
                            }
                        }
                    }
                    out.flush();
                    break;
                }
                case XMLEvent.COMMENT: {
                    CommentEvent commentEvent = (CommentEvent)event;
                    if (seenRootElement && elementDepth == 0) {
                        out.println();
                    }
                    out.print("<!--");
                    XMLString text = commentEvent.text;
                    for (int i = 0; i < text.length; i++) {
                        out.print(text.ch[text.offset + i]);
                    }
                    out.print("-->");
                    if (!seenRootElement) {
                        out.println();
                    }
                    out.flush();
                    break;
                }
                case XMLEvent.PROCESSING_INSTRUCTION: {
                    ProcessingInstructionEvent piEvent = (ProcessingInstructionEvent)event;
                    if (seenRootElement && elementDepth == 0) {
                        out.println();
                    }
                    out.print("<?");
                    out.print(piEvent.target);
                    if (piEvent.data != null) {
                        out.print(' ');
                        XMLString data = piEvent.data;
                        for (int i = 0; i < data.length; i++) {
                            out.print(data.ch[data.offset + i]);
                        }
                    }
                    out.print("?>");
                    if (!seenRootElement) {
                        out.println();
                    }
                    out.flush();
                    break;
                }
                case XMLEvent.DOCTYPE_DECL: {
                    DoctypeDeclEvent doctypeEvent = (DoctypeDeclEvent)event;
                    out.print("<!DOCTYPE ");
                    out.print(doctypeEvent.root);
                    out.print(' ');
                    if (doctypeEvent.pubid != null) {
                        out.print("PUBLIC \"");
                        out.print(doctypeEvent.pubid);
                        out.print("\" \"");
                    }
                    else {
                        out.print("SYSTEM \"");
                    }
                    out.print(doctypeEvent.sysid);
                    out.print('"');
                    out.print('>');
                    out.println();
                    out.flush();
                    break;
                }
            }
        }

    } // write(XMLPullParser,String)

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        XMLPullParser parser = new Xerces2();
        for (int i = 0; i < argv.length; i++) {
            Writer.write(parser, argv[i]);
        }
    } // main(String[])

} // class Writer
