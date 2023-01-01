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
 * A comment event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class CommentEvent
    extends XMLEvent {

    //
    // Data
    //

    /** The text content. */
    public XMLString text;

    //
    // Constructors
    //

    /** Default constructor. */
    public CommentEvent() {
        super(XMLEvent.COMMENT);
    } // <init>()

} // class CommentEvent
