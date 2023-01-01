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
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.parsers.Xerces2;

import java.io.IOException;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * A simple counter program to show the use of the pull parsing API.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class Counter {

    //
    // Constructors
    //

    /** This class cannot be constructed. */
    private Counter() {} // <init>()

    //
    // Public static methods
    //

    /** 
     * Counts the elements, attributes, characters, and ignorable
     * whitespace in a document.
     *
     * @param parser The pull parser to use.
     * @param sysid  The system id of the document to read.
     */
    public static void count(XMLPullParser parser, String sysid) 
        throws XNIException, IOException {

        // variables
        int elements = 0;
        int attributes = 0;
        int whitespaces = 0;
        int characters = 0;

        // initialize parser
        long timeBefore = System.currentTimeMillis();
        XMLInputSource source = new XMLInputSource(null, sysid, null);
        parser.setInputSource(source);

        // parse document
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            switch (event.type) {
                case XMLEvent.ELEMENT: {
                    ElementEvent elementEvent = (ElementEvent)event;
                    if (elementEvent.start) {
                        elements++;
                        if (elementEvent.attributes != null) {
                            attributes += elementEvent.attributes.getLength();
                        }
                    }
                    break;
                }
                case XMLEvent.CHARACTERS: {
                    CharactersEvent charactersEvent = (CharactersEvent)event;
                    if (charactersEvent.ignorable) {
                        whitespaces += charactersEvent.text.length;
                    }
                    else {
                        characters += charactersEvent.text.length;
                    }
                    break;
                }
            }
        }
        long timeAfter = System.currentTimeMillis();

        // print results
        System.out.print(sysid);
        System.out.print(": ");
        System.out.print(timeAfter - timeBefore);
        System.out.print(" ms (");
        System.out.print(elements);
        System.out.print(" elems, ");
        System.out.print(attributes);
        System.out.print(" attrs, ");
        System.out.print(whitespaces);
        System.out.print(" spaces, ");
        System.out.print(characters);
        System.out.print(" chars)");
        System.out.println();

    } // count(XMLPullParser,String)

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        XMLPullParser parser = new Xerces2();
        for (int i = 0; i < argv.length; i++) {
            Counter.count(parser, argv[i]);
        }
    } // main(String[])

} // class Counter
