/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

                        
package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

/**
 * A general entity event. This object denotes the boundaries of general
 * entity references (e.g. &amp;entity;) occurring in the document instance. 
 * This does <em>not</em> include the five pre-defined XML entities: 
 * &amp;amp;, &amp;lt;, &amp;gt;, &amp;apos;, and &amp;quot;.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class GeneralEntityEvent
    extends BoundedEvent {

    //
    // Data
    //

    /** The name of the general entity. */
    public String name;

    /** The namespace of the general entity. */
    public String namespace;

    /** 
     * The public identifier. This value will be null if the general entity
     * was declared as internal <em>or</em> it was declared as external
     * using a <code>SYSTEM</code> reference.
     */
    public String publicId;

    /** 
     * The base system identifier. This value denotes the expanded system
     * identifier of the entity where the general entity was declared.
     * This value will be null if the general entity was declared as
     * internal.
     */
    public String baseSystemId;

    /** 
     * The literal system identifier used in the general entity declaration. 
     * This value will be null if the general entity was declared as internal.
     */
    public String literalSystemId;
    
    /** 
     * The expanded system identifier of the general entity declaration.
     * This value will be null if the general entity was declared as internal.
     */
    public String expandedSystemId;

    /**
     * The auto-detected encoding of the general entity, if declared as
     * external.
     */
    public String encoding;

    //
    // Constructors
    //

    /** Default encoding. */
    public GeneralEntityEvent() {
        super(XMLEvent.GENERAL_ENTITY);
    } // <init>()

} // class GeneralEntityEvent
