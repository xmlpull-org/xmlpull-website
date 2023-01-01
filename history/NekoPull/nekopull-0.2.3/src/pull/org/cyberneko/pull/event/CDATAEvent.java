/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

/**
 * CDATA section event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class CDATAEvent
    extends BoundedEvent {

    //
    // Constructors
    //

    /** Default constructor. */
    public CDATAEvent() {
        super(XMLEvent.CDATA);
    } // <init>(short)

} // class CDATAEvent
