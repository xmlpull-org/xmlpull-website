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
 * A processing instruction event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class ProcessingInstructionEvent
    extends XMLEvent {

    //
    // Data
    //

    /** The target. */
    public String target;

    /** The processing instruction data, or null if no data was specified. */
    public XMLString data;

    //
    // Constructors
    //

    /** Default constructor. */
    public ProcessingInstructionEvent() {
        super(XMLEvent.PROCESSING_INSTRUCTION);
    } // <init>()

} // class ProcessingInstructionEvent
