/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

/**
 * A bounded event. Any event that has a start and end is "bounded".
 * For example, element events are bounded such that there exists a
 * start-element and and end-element event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public abstract class BoundedEvent 
    extends XMLEvent {

    //
    // Data
    //

    /** True if this is the start of the boundary. */
    public boolean start;

    //
    // Constructors
    //

    //
    // Constructors
    //

    /** Constructs a bounded event of the specified type. */
    protected BoundedEvent(short type) {
        super(type);
    } // <init>(short)

} // class BoundedEvent
