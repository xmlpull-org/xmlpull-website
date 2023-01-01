/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull;

import java.io.IOException;

import org.apache.xerces.xni.XNIException;

/**
 * An interface for iterating XML events. Even though this interface 
 * only contains a single method for iterating XML events, higher level
 * constructs can be written on top of this interface to provide more 
 * powerful iteration capability. However, the <code>nextEvent</code> 
 * method can always be used by applications for complete access to the 
 * event stream.
 * <p>
 * Typically, the application will use the iteration method(s) on the 
 * <code>XMLPullParser</code>. But this interface can also be used to 
 * construct any type of event iterator, even one not based on parsing 
 * XML streams.
 * <p color='red'>
 * <strong>Note:</strong>
 * Currently, the <code>XMLEventIterator</code> interface only contains
 * the single, low-level <code>nextToken</code> method for iterating XML
 * events. The <code>EventIterator</code> utility class is provided to
 * handle this shortcoming. As this API progresses, it is believed that
 * additional methods will be added to the <code>XMLEventIterator</code>
 * interface. However, this will be decided by actual users of the API. 
 * Please <a href='mailto:andyc@apache.org'>let me know</a> if you have
 * any suggestions or comments.
 * </p>
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public interface XMLEventIterator {

    //
    // XMLEventIterator methods
    //

    /**
     * Returns the next event in the document or null if there are
     * no more events. This method will return one and only one event
     * if it is available; it will never return an event chain (i.e.
     * an event with a non-null <code>next</code> field).
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     */
    public XMLEvent nextEvent() throws XNIException, IOException;

} // interface XMLEventIterator
