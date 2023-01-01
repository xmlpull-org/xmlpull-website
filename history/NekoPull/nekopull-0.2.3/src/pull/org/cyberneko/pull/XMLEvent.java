/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull;

import org.apache.xerces.xni.Augmentations;

/**
 * The base XML event class. For a complete list of available events,
 * refer to the <code>org.cyberneko.pull.event</code> package.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class XMLEvent {

    //
    // Constants
    //

    /** Event type: document. */
    public static final short DOCUMENT = 0;

    /** Event type: element. */
    public static final short ELEMENT = 1;

    /** Event type: character content. */
    public static final short CHARACTERS = 2;

    /** Event type: prefix mapping. */
    public static final short PREFIX_MAPPING = 3;

    /** Event type: general entity. */
    public static final short GENERAL_ENTITY = 4;

    /** Event type: comment. */
    public static final short COMMENT = 5;

    /** Event type: processing instruction. */
    public static final short PROCESSING_INSTRUCTION = 6;

    /** Event type: CDATA section. */
    public static final short CDATA = 7;

    /** Event type: text declaration. */
    public static final short TEXT_DECL = 8;

    /** Event type: DOCTYPE declaration. */
    public static final short DOCTYPE_DECL = 9;

    //
    // Data
    //

    /** 
     * Event type. This field is final and must be set within the constructor
     * of any subclass.
     */
    public final short type;

    /** Event augmentations. */
    public Augmentations augs;

    /** 
     * Next event, if used in an event chain. Typically, only a single
     * event is returned at a time. However, this field is present to
     * enable higher-level constructs to chain events together. For
     * example, this can be useful to avoid concatenating the contents 
     * of character buffers, etc.
     */
    public XMLEvent next;

    //
    // Constructors
    //

    /** Constructs an XML event with the specified type. */
    public XMLEvent(short type) {
        this.type = type;
    } // <init>(short)

} // class XMLEvent
