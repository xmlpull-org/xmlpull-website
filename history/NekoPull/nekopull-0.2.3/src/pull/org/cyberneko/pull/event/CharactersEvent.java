/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

import org.apache.xerces.xni.XMLString;

/**
 * Character content event. This event is also used for ignorable
 * whitespace.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class CharactersEvent
    extends XMLEvent {

    //
    // Data
    //

    /** The text content. */
    public XMLString text;

    /** True if this event is ignorable whitespace. */
    public boolean ignorable;

    //
    // Constructors
    //

    /** Default constructor. */
    public CharactersEvent() {
        super(XMLEvent.CHARACTERS);
    } // <init>()

} // class CharactersEvent
