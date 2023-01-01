/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.util;

import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLEventIterator;
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

import java.io.IOException;

import org.apache.xerces.xni.XNIException;

/**
 * This class contains a set of utility functions to allow applications 
 * to more conveniently iterate XML events instead of having to call
 * <code>nextEvent</code> for each event in the event stream. 
 * <p color='red'>
 * <strong>Note:</strong>
 * Currently, the <code>XMLEventIterator</code> interface only contains
 * the single, low-level <code>nextToken</code> method for iterating XML
 * events. The <code>EventIterator</code> utility class is provided to
 * handle this shortcoming. As this API progresses, it is believed that
 * additional methods will be added to the <code>XMLEventIterator</code>
 * interface. However, this will be decided by actual users of the API. 
 * Please <a href='mailto:andyc@apache.org'>let me know</a> if you have
 * any suggestions or comments.
 * </p>
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class EventIterator 
    implements XMLEventIterator {

    //
    // Data
    //

    // iterator

    /** XML event iterator. */
    protected XMLEventIterator fEventIterator;

    // state

    /** Pushback event. */
    protected XMLEvent fPushbackEvent;

    /** Element depth. */
    protected int fElementDepth;

    //
    // Constructors
    //

    /** Constructs an event iterator from the specified event iterator. */
    public EventIterator(XMLEventIterator iterator) {
        fEventIterator = iterator;
    } // <init>(XMLEventIterator)

    //
    // XMLEventIterator methods
    //

    /** Returns the next event. */
    public XMLEvent nextEvent() throws IOException, XNIException {
        XMLEvent event;
        if (fPushbackEvent != null) {
            event = fPushbackEvent;
            fPushbackEvent = null;
        }
        else {
            event = fEventIterator.nextEvent();
        }
        if (event != null) {
            switch (event.type) {
                case XMLEvent.DOCUMENT: {
                    fElementDepth = 0;
                    break;
                }
                case XMLEvent.ELEMENT: {
                    ElementEvent elementEvent = (ElementEvent)event;
                    fElementDepth += elementEvent.start ? 1 : -1;
                    break;
                }
            }
        }
        return event;
    } // nextEvent():XMLEvent

    //
    // Public methods
    //

    /** Returns the nextEvent of the given type. */
    public XMLEvent nextEvent(short type) throws IOException, XNIException {
        do {
            XMLEvent event = nextEvent();
            if (event == null) {
                break;
            }
            if (event.type == type) {
                return event;
            }
        } while (true);
        return null;
    } // nextEvent(short):XMLEvent

    // TODO: [Q] What methods would be useful here? -Ac

} // class EventIterator
