/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

import org.apache.xerces.xni.XMLLocator;

/**
 * A document event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class DocumentEvent
    extends BoundedEvent {

    //
    // Data
    //

    /** 
     * The document locator. Unlike other data values, the application is
     * allowed to keep a reference to this object in order to query document
     * locations of events.
     */
    public XMLLocator locator;

    /** The automatically detected encoding of the document. */
    public String encoding;

    //
    // Constructors
    //

    /** Default constructor. */
    public DocumentEvent() {
        super(XMLEvent.DOCUMENT);
    } // <init>()

} // class DocumentEvent
