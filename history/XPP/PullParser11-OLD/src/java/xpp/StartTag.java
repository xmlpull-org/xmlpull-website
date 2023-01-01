//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: StartTag.java,v 1.15 2001/08/15 21:37:25 aslom Exp $
 */

package xpp;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate XML STag and EmptyElement
 * 
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */

public class StartTag {
  final static boolean MAPPING = false;
  
  public StartTag() {
  }
        
  /**
   * Reinitialize start tag content to none
   */
  public void clear () { 
    reset(); 
    uri = "";  
    localName = qName = null;
  }

  /** Get start tag uri (meaningful only if namespaces enabled) */
  public String getUri() { return uri; }      
  /** 
   * Get start tag localName if namespaces enabled
   * or just qName (see below) if namespaces diabled.
   */
  public String getLocalName() { return localName; } 
  /**
   * Return start tag as it is in document (qName).
   */     
  public String getQName() { return qName; }      


  /**
   * Return number of attributes.
   */
  public int getLength() { return attEnd; }

  /**
   * Get uri of attribute number index (starts from 0).
   * (meaningful only if namespaces enabled) 
   */
  public String getURI(int index) {
  	if (index >= 0 && index < attEnd) {
	    return attArr[index].uri;
    } else {
      return null;
    }
  }

  /**
   * Get localName of attribute number index (starts from 0)
   * if namespaces enabled or just attribute name if namespaces disabled.   
   */
  public String getLocalName(int index) {
  	if (index >= 0 && index < attEnd) {
	    return attArr[index].localName;
    } else {
      return null;
    }
  }

  /** Return qName of atrribute number index (starts from 0) */   
  public String getRawName(int index) {
  	if (index >= 0 && index < attEnd) {
	    return attArr[index].qName;
    } else {
      return null;
    }
  }

  /** always return "CDATA" */
  public String getType(int index) {
    return "CDATA";
  }


  /** Return value of attribute number index. */
  public String getValue (int index) {
   	if (index >= 0 && index < attEnd) {
      return attArr[index].value;
     } else {
       return null;
     }
  }


  /** always return "CDATA" */
  public String getType (String qName) {
    return "CDATA";
  }

  /** always return "CDATA" */
  public String getType (String uri, String localName) {
    return "CDATA";
  }

  /** 
   * Return value of attribute named (uri, localName) or null
   * of no such attribute found.
   * (meaningful only if namespaces enabled) 
   */
  public String getValue (String uri, String localName) {
    if(MAPPING) {
      Map mapLocal = (Map) mapUri.get(uri);
      return (String) mapLocal.get(localName);
    } else {    
      for(int i = 0; i < attEnd; ++i) {
        if(uri.equals(attArr[i].uri) 
          && localName.equals(attArr[i].localName))
          {
            return attArr[i].value;
          }
      }
      return null;
    }
  }

  /** 
   * Return value of attribute named qName or null
   * of no such attribute found.
   */
  public String getValue (String qName) {
    if(MAPPING) {
      return (String) mapQName.get(qName);
    } else {    
      for(int i = 0; i < attEnd; ++i) {
        if(qName.equals(attArr[i].qName))
          {
            return attArr[i].value;
          }
      }
      return null;
    }
  }  

  /**
   * Print into StringBuffer element name 
   */
  public void printFields(StringBuffer buf) {
    buf.append(" '" + qName + "'");
    if(uri != null && !"".equals(uri))
      buf.append("('" + uri +"','" + localName + "') ");
  }

  /** 
   * Return string representation of start tag including name
   * and list of attributes.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer("StartTag={");
    printFields(buf);
    if(attEnd > 0) {
      buf.append(" attArr=[ ");    
      for(int i = 0; i < attEnd; ++i) {
         buf.append(attArr[i]+" ");    
      }
      buf.append(" ]");    
    }
    buf.append(" }");
    return buf.toString();
  }

  // ==== utility method
  
  /**
   * Make sure that there is enough space to keep size attributes.
   */
  void ensureCapacity(int size) {
    int newSize = 2 * size;
    if(newSize == 0)
      newSize = 25;
    if(attSize < newSize) {
      Attribute[] newAttArr = new Attribute[newSize];
      if(attArr != null)
        System.arraycopy(attArr, 0, newAttArr, 0, attEnd);
      for(int i = attEnd; i < newSize; ++i) {
        newAttArr[i] = new Attribute();
      }
      attArr = newAttArr;
      attSize = newSize;
    }
  }
     
  /**
   * Reset intenrla valriables but do not clear previous values
   * (it is more efficient)
   */      
  private void reset() {
    attEnd = 0;
    if(MAPPING) {
      mapQName.clear();
      mapUri.clear();
    }
  }

  // ===== internals
  
  
  int attEnd;
  int attSize;
  Attribute[] attArr;


  /**
   * very internal - do not use unless you know what u r doing...
   */
  void addAttribute() {
    Attribute att = attArr[attEnd++];
    if(MAPPING) {
      mapQName.put(att.qName, att.value);
      Map mapLocal = (Map) mapUri.get(att.uri);
      if(mapLocal == null) {
        mapLocal = new HashMap();
        mapUri.put(att.uri, mapLocal);
      }
      mapLocal.put(att.localName, att.value);
    }      
  }

  // ===== internals
  
  String uri;
  String localName;
  String qName;
  Map mapQName = new HashMap();
  Map mapUri = new HashMap();
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
