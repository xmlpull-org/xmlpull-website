/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

/**
 * A DOCTYPE declaration event.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class DoctypeDeclEvent
    extends XMLEvent {

    //
    // Data
    //

    /** The root element name. */
    public String root;

    /** 
     * The public identifier of the external subset. This value can be
     * null if there is no external subset <em>or</em> if the external
     * subset is referenced with "SYSTEM".
     */
    public String pubid;
    
    /** 
     * The system identifier of the external subset. This value can be
     * null if there is no external subset.
     */
    public String sysid;

    //
    // Constructors
    //

    /** Default constructor. */
    public DoctypeDeclEvent() {
        super(XMLEvent.DOCTYPE_DECL);
    } // <init>()

} // class DoctypeDeclEvent
