//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: EndTag.java,v 1.11 2001/08/15 21:37:25 aslom Exp $
 */

package xpp;

/**
 * Encapsulate XML ETag
 * 
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */

public class EndTag {
  
  public EndTag() {
  }

  /** Get endtag tag uri (meaningful only if namespaces enabled) */        
  public String getUri() { return uri; }      
  /** 
   * Get start tag localName if namespaces enabled
   * or just qName (see below) if namespaces diabled.
   */
  public String getLocalName() { return localName; }      
  /**
   * Return end tag name as it is in document (qName).
   */     
  public String getQName() { return qName; }      

  /**
   * Print into StringBuffer element name 
   */
  public void printFields(StringBuffer buf) {
    //if(uri != null)
    //  buf.append(" uri='" + uri + "'");    
    //if(localName != null)
    //  buf.append(" localName='" + localName + "'");
    buf.append(" '" + qName + "'");
    if(uri != null && !"".equals(uri))
      buf.append("('" + uri +"','" + localName + "') ");
  }

  /** 
   * Return string representation of end tag including name
   */
  public String toString() {
    StringBuffer buf = new StringBuffer("EndTag={");
    printFields(buf);
    buf.append(" }");
    return buf.toString();
  }

  // ===== internals
  
  String uri;
  String localName;
  String qName;
}


/*
 * Indiana University Extreme! Lab Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the Indiana
 *        University Extreme! Lab (http://www.extreme.indiana.edu/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Indiana Univeristy" and "Indiana Univeristy
 *    Extreme! Lab" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact http://www.extreme.indiana.edu/.
 *
 * 5. Products derived from this software may not use "Indiana
 *    Univeristy" name nor may "Indiana Univeristy" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
