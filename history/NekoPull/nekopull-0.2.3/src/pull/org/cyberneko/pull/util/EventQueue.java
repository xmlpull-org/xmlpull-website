/* 
 * (C) Copyright 2002-2003, Andy Clark.  All rights reserved.
 *
 * This file is distributed under an Apache style license. Please
 * refer to the LICENSE file for specific details.
 */

package org.cyberneko.pull.util;

import org.cyberneko.pull.XMLEvent;

/**
 * A general purpose queue for pull parser event objects.
 *
 * @author Andy Clark
 *
 * @version $Id$
 */
public class EventQueue {

    //
    // Data
    //

    /** The size of the queue. */
    protected int fSize;

    /** The head of the queue. */
    protected XMLEvent fHead;

    /** The tail of the queue. */
    protected XMLEvent fTail;

    //
    // Public methods
    //

    /** Returns the size of the queue. */
    public int size() {
        return fSize;
    } // size():int

    /** Clears the queue. */
    public void clear() {
        fSize = 0;
        fHead = null;
        fTail = null;
    } // clear()

    /** Returns true if the queue is empty. */
    public boolean isEmpty() {
        return fHead == null;
    } // isEmpty():boolean

    /** Adds an event to the queue. */
    public void enqueue(XMLEvent event) {
        if (fHead == null) {
            fHead = event;
        }
        else {
            fTail.next = event;
        }
        fTail = event;
        event.next = null;
        fSize++;
    } // enqueue(XMLEvent)

    /** 
     * Removes and returns an event from the queue, or null if the queue 
     * is empty. 
     */
    public XMLEvent dequeue() {
        XMLEvent event = fHead;
        if (fHead != null) {
            fSize--;
            fHead = fHead.next;
            event.next = null;
        }
        return event;
    } // dequeue():XMLEvent

    //
    // Object methods
    //

    /** Returns a String representation of this object. */
    public String toString() {
        StringBuffer str = new StringBuffer();
        XMLEvent event = fHead;
        while (event != null) {
            str.append(event);
            event = event.next;
            if (event != null) {
                str.append(", ");
            }
        }
        return str.toString();
    } // toString():String

} // class EventQueue
