/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package sample;

import org.apache.xerces.xni.parser.XMLInputSource;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLPullParser;
import org.cyberneko.pull.event.CharactersEvent;
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.parsers.Xerces2;

/**
 * Sample program to test the pull parser.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class TestPullParser {

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String[] argv) throws Exception {
        
        // create parser and set input source
        XMLPullParser parser = new Xerces2();
        XMLInputSource source = new XMLInputSource(null, "data/pull/test03.xml", null);
        parser.setInputSource(source);
        
        // iterate document events
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            if (event.type == XMLEvent.ELEMENT) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    System.out.println("("+elementEvent.element.rawname);
                }
                else {
                    System.out.println(")"+elementEvent.element.rawname);
                }
            }
            else if (event.type == XMLEvent.CHARACTERS) {
                CharactersEvent charsEvent = (CharactersEvent)event;
                System.out.println("\""+charsEvent.text);
            }
        }
        
        // free resources
        parser.cleanup();

    } // main(String[])

} // class TestPullParser
