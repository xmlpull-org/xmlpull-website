/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.event;

import org.cyberneko.pull.XMLEvent;

/**
 * An XMLDecl or TextDecl event. This event is used to communicate both
 * the XML declaration at the beginning of XML instance documents and
 * text declarations appearing at the beginning of external parsed
 * entities.
 * <p color='red'>
 * REVISIT: [Q] Should these be separate events? -Ac
 * </p>
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class TextDeclEvent
    extends XMLEvent {

    //
    // Data
    //

    /** True if this event is used for XMLDecl; false if used for TextDecl. */
    public boolean xmldecl;
    
    /** The value of the "version" pseudo-attribute. */
    public String version;
    
    /** The value of the "encoding" pseudo-attribute. */
    public String encoding;
    
    /** 
     * The value of the "standalone" pseudo-attribute. This value will not
     * be set for TextDecl events.
     * <p color='red'>
     * REVISIT: This is why I'm thinking that the XMLDecl should be
     *          separate from the TextDecl. It just seems <em>wrong</em>
     *          to have unused fields based on the type.
     * </p>
     */
    public String standalone;

    //
    // Constructors
    //

    /** Default constructor. */
    public TextDeclEvent() {
        super(XMLEvent.TEXT_DECL);
    } // <init>()

} // class TextDeclEvent
