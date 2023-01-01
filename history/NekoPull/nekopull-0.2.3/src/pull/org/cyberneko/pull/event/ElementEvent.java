/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;

/**
 * An element event. This event communicates both the start element and
 * end element events. In addition, this event allows the application
 * to query whether the element tag represented by this event was 
 * <em>empty</em>. In other words, whether the tag appears as
 * "&lt;root&gt;&lt;/root&gt;" or "&lt;root/&gt;".
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class ElementEvent
    extends BoundedEvent {

    //
    // Data
    //

    /** The qualified name of the element. */
    public QName element;

    /** 
     * The attributes for the <em>start</em> element. This value will be
     * null for end elements.
     */
    public XMLAttributes attributes;

    /** 
     * True if this element is an empty element, for example &lt;root/&gt;.
     * <p>
     * <strong>Note:</strong>
     * The pull parser will always report both a start and end element event
     * for empty elements. This allows applications to deal with elements in
     * a consistent manner. However, both the start and end element event
     * objects of an empty element will have the <code>empty</code> field
     * set to <code>true</code>.
     */
    public boolean empty;

    //
    // Constructors
    //

    /** Default constructor. */
    public ElementEvent() {
        super(XMLEvent.ELEMENT);
    } // <init>()

} // class ElementEvent
