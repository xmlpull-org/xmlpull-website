/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;

/**
 * A prefix mapping event.
 * <p color='red'>
 * REVISIT: [Q] Should we pass prefix mappings as separate events or should
 *              there be an XNI <code>NamespaceContext</code> passed with
 *              the event objects? -Ac
 * </p>
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class PrefixMappingEvent
    extends BoundedEvent {

    //
    // Data
    //

    /** 
     * The namespace prefix. The prefix value will be the empty
     * string, "", in the case of the default namespace.
     */
    public String prefix;

    /**
     * The URI bound to the prefix. This value will be null for end
     * prefix mapping events.
     * <p color='red'>
     * REVISIT: [Q] Should the URI be set even on the end prefix mapping
     *              events? -Ac
     * </p>
     */
    public String uri;

    //
    // Constructors
    //

    /** Default constructor. */
    public PrefixMappingEvent() {
        super(XMLEvent.PREFIX_MAPPING);
    } // <init>()

} // class PrefixMappingEvent
