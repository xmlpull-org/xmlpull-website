//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XmlPullParser.java,v 1.39 2001/08/15 21:37:25 aslom Exp $
 */

package xpp;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

import sxt.XmlTokenizer;
import sxt.XmlTokenizerException;

/**
 * XML Pull Parser (XPP) allows to pull  XML events from input stream.
 *
 * Advantages:<ul>
 * <li>very simple pull interface 
 *     - ideal for deserializing XML objects (like SOAP)
 * <li>fast and simple (thin wrapper around XmlTokenizer class 
 *    - adds  about 10%  for big documents, 
 *      maximum 50% more processing time for small documents )       
 * <li>lightweight memory model - minimized memory allocation: 
 *    element content and attributes are only read on explicit 
 *       method calls, 
 *    both StartTag and EndTag can be reused during parsing
 * <li>small - total compiled size around 20K
 * <li>by default supports namespaces parsing 
 *    (can be switched off)
 * <li>support for mixed content can be explicitly disabled
 * </ul>
 *
 * Limitations: <ul>
 * <li>this is beta version - may have still bugs :-)
 * <li>does not parse DTD (recognizes only predefined entities)
 * </ul>
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */


public class XmlPullParser {
  /** signal logical end of xml document */
  public final static byte END_DOCUMENT = 1;
  /** start tag was just read */
  public final static byte START_TAG = 2;
  /** end tag was just read */
  public final static byte END_TAG = 3;
  /** element content was just read */
  public final static byte CONTENT = 4;
  
  /**
   * Create instance of pull parser.
   */  
  public XmlPullParser() {
  }
        
  /** 
   * Reset parser state so it can be used to parse new 
   */        
  public void reset() {  
    tokenizer.reset();
    resetState();
  }

  private void resetState() {
    tokenizer.paramNotifyDoctype = true;
    token = eventType = -1;
    elStackDepth = 0;
    prefix2Ns.clear();
    // 4. NC: Prefix Declared
    prefix2Ns.put("xml", "http://www.w3.org/XML/1998/namespace");
    //beforeAtts = false;
    emptyElement = false;
    seenRootElement = false;
    //scache = new StringCache();
  }
  
  /**
   * Reset parser and set new input.
   */
  public void setInput(char[] buf) {
    resetState();
    tokenizer.setInput(buf);
  }

  /**
   * Reset parser and set new input.
   */
  public void setInput(Reader reader) {
    resetState();
    tokenizer.setInput(reader);
  }

  /**
   * Allow for mixed element content.
   * Enabled by default.
   * When disbaled element must containt either text 
   * or other elements.
   */
  public void setMixedContent(boolean enable) { 
    tokenizer.setMixedContent(enable);
  }


  public boolean isNamespaceAware() { return supportNs; }

  /** 
   * Set support of namespaces. Enabled by default.
   */
  public void setNamespaceAware(boolean awareness) 
    throws XmlPullParserException   
  {
    if(elStackDepth > 0 || seenRootElement) {
      throw new XmlPullParserException(
         "namespace support can only be set when not parsing");
    }  
    supportNs = awareness;
  
  }

  /** 
   * Set support of namespaces. Enabled by default.
   *
   * @deprecated  replaced by {@link #setNamespaceAware(boolean)}
   */
  public void setSupportNamespaces(boolean enable) 
    throws XmlPullParserException 
  {
    setNamespaceAware(enable);
  }

  public void setHardLimit(int value) throws XmlPullParserException {
    try {
      tokenizer.setHardLimit(value);
    } catch(XmlTokenizerException ex) {
      throw new XmlPullParserException("could not set hard limit to "+value, ex);
    }
  }

  public void setSoftLimit(int value) throws XmlPullParserException {
    try {
      tokenizer.setSoftLimit(value);
    } catch(XmlTokenizerException ex) {
      throw new XmlPullParserException("could not set soft limit to "+value, ex);
    }
  }

  /**
   * Return local part of qname.
   * For example for 'xsi:type' it returns 'type'.
   */
  public String getQNameLocal(String qName) {
    int i = qName.lastIndexOf(':');
    return qName.substring(i + 1);       
  }

  /**
   * Return uri part of qname.
   * It is depending on current state of parser to find
   * what namespace uri is mapped from namespace prefix.
   * For example for 'xsi:type' if xsi namespace prefix 
   * was declared to 'urn:foo' it will return 'urn:foo'.
   */
  public String getQNameUri(String qName) 
    throws XmlPullParserException 
  {
    if(elStackDepth == 0)
      throw new XmlPullParserException(
         "parsing must be started to get uri from qname");    
    int i = qName.lastIndexOf(':');
    if(i > 0) {
      String prefix = qName.substring(0, i);
      return (String) prefix2Ns.get(prefix);
    } else {
      return elStack[elStackDepth-1].defaultNs;
    }
  }


   //How to support namespace-prefixes is true??? reporting of xmlns:xxx attributes...
   public void setNamespacePrefixes(boolean enable) {
   }

   //TODO: if called in EndTag is a bit smart to ask about previous element?
   // NOTE: this is actual parser table - DO NOT MODIFY IT
   public int prefixesLentrh() { 
     return elStack[elStackDepth-1].prefixes.length;
   }
   public String prefixes(int pos) { 
     return elStack[elStackDepth-1].prefixes[pos];
   }
   public String[] namespacePrefixes(int depth) { 
    //count size, allocate array, copy stuff... 
    return elStack[elStackDepth-1].prefixes;
   }
   public String prefix2Namespace(String prefix) { 
     return (String) prefix2Ns.get(prefix);
   }


  /**
   * If parser has just read start tag it allows to skip whoole
   * subtree contined in this element. Returns when encounters
   * end tag matching the start tag.
   */
  public byte skipSubTree() throws XmlPullParserException, IOException {
    if(eventType != START_TAG)
      throw new XmlPullParserException(
        "start tag must be read before skiping subtree"+getPosDesc(), getLineNumber(), getColumnNumber());
    int level = 1;
    byte type = XmlPullParser.END_TAG;
    while(level > 0) {
      type = next();
      switch(type) {
      case XmlPullParser.START_TAG:
        ++level;
        break;
      case XmlPullParser.END_TAG:
        --level;
        break;
      }
    }
    return type;
  }

  /**
   * Return string describing current position of parser in input stream.
   */
  public String getPosDesc() {
    String desc;
    switch(eventType) {
      case START_TAG:
         desc = "START_TAG";
         break;
      case CONTENT:
         desc = "CONTENT";
         break;
      case END_TAG:
         desc = "END_TAG";
         break;
      case END_DOCUMENT:
         desc = "END_DOCUMENT";
         break;
      default:
         desc = "UNKNONW_EVENT";
    }
    return tokenizer.getPosDesc()+" (parser state "+desc+")";
  }

  public int getLineNumber() { return tokenizer.getLineNumber(); }
  public int getColumnNumber() { return tokenizer.getColumnNumber(); }

  public int getDepth() { return elStackDepth; }
  
  /**
   * This is key method - it reads more from input stream
   * and returns next event type 
   * (such as START_TAG, END_TAG, CONTENT).
   * or END_DOCUMENT if no more input.
   *
   * <p>This is simple automata (in pseudo-code):
   * <pre>
   * byte next() {
   *    while(eventType != END_DOCUMENT) {
   *      token = tokenizer.next();  // get next XML token
   *      switch(token) {
   *
   *      case XmlTokenizer.END_DOCUMENT:
   *        return eventType = END_DOCUMENT
   *
   *      case XmlTokenizer.CONTENT:
   *        // check if content allowed - only inside element
   *        return eventType = CONTENT
   *
   *     case XmlTokenizer.ETAG_NAME:
   *        // popup element from stack - compare if matched start and end tag  
   *        // if namespaces supported restore namespaces prefix mappings
   *        return eventType = END_TAG;
   *
   *      case XmlTokenizer.STAG_NAME:
   *        // create new  element push it on stack
   *        // process attributes (including namespaces)
   *        // set emptyElement = true; if empty element
   *        // check atribute uniqueness (including nmespacese prefixes)
   *        return eventType = START_TAG;
   *
   *      }
   *    }
   * }  
   * </pre>
   *
   * <p>Actual parsing is more complex especilly for start tag due to 
   *   dealing with attributes reported separately from tokenizer and 
   *   declaring namespace prefixes and uris.
   *
   *
   *
   */

  public byte next() throws XmlPullParserException, IOException {
    if(emptyElement) {
	    --elStackDepth;  //TODO
	    if(elStackDepth == 0)
	      seenRootElement = true;
      emptyElement = false;
      return eventType = END_TAG;
    }  
    Attribute ap = null;
    ElementContent el = null;
    try {
      
      do {
        String s;
        token = tokenizer.next();
        //System.err.println("got token="+token);
        switch(token) {

        case XmlTokenizer.END_DOCUMENT:
          if(elStackDepth > 0) {
            throw new XmlPullParserException("expected element end tag '"
               +elStack[elStackDepth-1].qName+"' not end of document"
               +getPosDesc(), getLineNumber(), getColumnNumber());
          }
          // SAX endDocument
          return eventType = END_DOCUMENT;

        case XmlTokenizer.CONTENT:
          if(elStackDepth > 0) {
            elContent = null;
            //SAX characters(...)
            return eventType = CONTENT;          
          } else if(tokenizer.seenContent) {           
            throw new XmlPullParserException(
               "only whitespace content allowed outside root element"
                 +getPosDesc(), getLineNumber(), getColumnNumber());
          } 
          // we do allow whitespace content outside of root element
          break;

        case XmlTokenizer.ETAG_NAME:
          if(seenRootElement)
            throw new XmlPullParserException(
               "no markup allowed outside root element"+getPosDesc(), getLineNumber(), getColumnNumber());
          s = //HOT 30%
            new String(tokenizer.buf, tokenizer.posStart, tokenizer.posEnd - tokenizer.posStart); 
           //scache.convert(tokenizer.buf, tokenizer.posStart, tokenizer.posEnd, false); //FIXME
          --elStackDepth;
          if(elStackDepth == 0)
            seenRootElement = true;
          if(elStackDepth < 0) 
            throw new XmlPullParserException(
                "end tag without start stag"+getPosDesc(), getLineNumber(), getColumnNumber());
          if(!s.equals(elStack[elStackDepth].qName))
            throw new XmlPullParserException("end tag name should be "
               +elStack[elStackDepth].qName +" not "+s+getPosDesc(), getLineNumber(), getColumnNumber());
          // restore declared namespaces
          el = elStack[elStackDepth];
          if(supportNs && el.prefixes != null) {
            for(int i = el.prefixesEnd - 1; i >= 0; --i) {
              //SAX2 endPrefixMapping(el.prefixes[i]);  
              prefix2Ns.put(el.prefixes[i], el.prefixPrevNs[i]);
            }
            el.prefixesEnd = 0;
            //if(elStackDepth > 0 && !el.defaultNs.equals(elStack[elStackDepth-1].defaultNs)
            //SAX2 endPrefixMapping("", el.defaultNs) ???            
          }
          //SAX2 endElement(el.uri, el.localName, el.qName)
          return eventType = END_TAG;

        case XmlTokenizer.STAG_NAME:
          if(seenRootElement)
            throw new XmlPullParserException(
               "no markup allowed outside root element"+getPosDesc(), getLineNumber(), getColumnNumber());
          //beforeAtts = true;
          emptyElement = false;
          if(elStackDepth >= elStackSize) {
            ensureCapacity(elStackDepth);
          }  
          el = elStack[elStackDepth];
          el.prefixesEnd = 0;
          attrPosEnd = 0;
          el.defaultNs = null;
          el.qName = new String(
            tokenizer.buf,tokenizer.posStart, tokenizer.posEnd-tokenizer.posStart);
          
          if(supportNs && tokenizer.posNsColon >= 0) {
            if(tokenizer.nsColonCount > 1)
             throw new XmlPullParserException(
               "only one colon allowed in prefixed element name"
               +getPosDesc(), getLineNumber(), getColumnNumber());            
            el.prefix = 
              el.qName.substring(0, tokenizer.posNsColon - tokenizer.posStart);
            el.localName = 
              el.qName.substring(tokenizer.posNsColon - tokenizer.posStart + 1);
   				} else {
            el.prefix = null;
            el.localName = el.qName; 				
   				}
			    el.uri = null;

   				++elStackDepth;
					break;
					
        case XmlTokenizer.ATTR_NAME:
          if(attrPosEnd >= attrPosSize) ensureAttribs(attrPosEnd + 1);
          ap = attrPos[attrPosEnd];

   		    ap.qName = new String(
   		      tokenizer.buf,tokenizer.posStart,tokenizer.posEnd-tokenizer.posStart);
   		    ap.uri = null;
          if(supportNs && tokenizer.posNsColon >= 0) {
            if(tokenizer.nsColonCount > 1)
              throw new XmlPullParserException(
  	              "only one colon allowed in prefixed attribute name"
  	              +getPosDesc(), getLineNumber(), getColumnNumber());            
            ap.prefix = ap.qName.substring(
              0, tokenizer.posNsColon - tokenizer.posStart);
            ap.localName = 
              ap.qName.substring(tokenizer.posNsColon - tokenizer.posStart + 1);
          } else {
            ap.prefix = null;
            ap.localName = ap.qName;
          }
          break;
          
        case XmlTokenizer.ATTR_CONTENT:

          if(tokenizer.parsedContent) {
		        ap.value = new String(
		          tokenizer.pc, tokenizer.pcStart, tokenizer.pcEnd -tokenizer.pcStart);
          } else {
     		    ap.value = new String(
     		      tokenizer.buf,tokenizer.posStart,tokenizer.posEnd-tokenizer.posStart);
          } 
          if(supportNs) {
           if("xmlns".equals(ap.prefix)) {
            // add new NS prefix
            if(el.prefixesEnd >= el.prefixesSize) {
              el.ensureCapacity(el.prefixesEnd);
            }  
            el.prefixes[el.prefixesEnd] = ap.localName;
            el.prefixPrevNs[el.prefixesEnd] = 
             (String) prefix2Ns.get(ap.localName);
     	      if(CHECK_ATTRIB_UNIQ) {
              //NOTE: O(n2) complexity but n is very small...    
              //System.err.println("checking xmlns name="+ap.localName);
              for(int i = 0; i < el.prefixesEnd; ++i) {
                if(ap.localName.equals(el.prefixes[i])) {
                  throw new XmlPullParserException(
                    "duplicate xmlns declaration name '"+ap.localName+"'"
                     +getPosDesc(), getLineNumber(), getColumnNumber());
                }               
              }
            }
            ++el.prefixesEnd;
            //System.err.println("adding prefix="+att.localName+" ns="+att.value);
            prefix2Ns.put(ap.localName, ap.value);
            //SAX2 startPrefixMapping(ap.localName, ap.value)
           } else if("xmlns".equals(ap.qName)) {
            if(el.defaultNs != null)
              throw new XmlPullParserException(
                "default namespace was alredy declared by xmlns attribute"
                +getPosDesc(), getLineNumber(), getColumnNumber());            
            //System.err.println("adding default ns="+att.value);
            el.defaultNs = ap.value;
           } else {
            //SAX2 if namespace-prefixes always add
            ++attrPosEnd; // new attribute is added to list
           }  
          } else {
     	      if(CHECK_ATTRIB_UNIQ) {
              //NOTE: O(n2) complexity but n is small...    
              for(int i = 0; i < attrPosEnd; ++i) {
                if(ap.qName.equals(attrPos[i].qName)) {
                  throw new XmlPullParserException(
                    "duplicate attribute name '"+ap.qName+"'"
                    +getPosDesc(), getLineNumber(), getColumnNumber());
                }
              }
            }
            ++attrPosEnd; // new attribute is added to list
          }                 
          break;

        case XmlTokenizer.EMPTY_ELEMENT:
          emptyElement = true;
          break;

        case XmlTokenizer.STAG_END:
      		if(supportNs) {	      
            if(el.defaultNs == null) {
              if(elStackDepth > 1) {
                el.defaultNs = elStack[elStackDepth-2].defaultNs;
              } else {
                el.defaultNs = "";
              }
            } // else //SAX2 startPrefixMapping("", el.defaultNs) ???
            if(el.uri == null) {
              el.uri = el.defaultNs;          
            }

            //System.err.println("el default ns="+el.defaultNs);
            if(el.prefix != null) {
              el.uri = (String) prefix2Ns.get(el.prefix);
              if(el.uri  == null)
                throw new XmlPullParserException(
                  "no namespace for prefix '"+el.prefix+"'"
                  +getPosDesc(), getLineNumber(), getColumnNumber()); 
              // assert(stag.uri != null);
            } else {
              //stag.uri = "";
              el.uri = el.defaultNs;
              //System.err.println("setting el default uri="+stag.uri);
              el.localName = el.qName;
            }
  
            for(int j = 0; j < attrPosEnd ; ++j) {
        		  ap = attrPos[j];
        	    if(ap.uri == null && ap.prefix != null) {
      	        ap.uri = (String) prefix2Ns.get(ap.prefix);
      	        if(ap.uri  == null)
      	           throw new XmlPullParserException(
      	             "no namespace for prefix "+ap.prefix+getPosDesc(), getLineNumber(), getColumnNumber()); 
      	      }
     	      }  		  
     	      if(CHECK_ATTRIB_UNIQ) {
              // only now can check attribute uniquenes 
              //NOTE: O(n2) complexity but n is small...    
              for(int j = 1; j < attrPosEnd ; ++j) {
          		  ap = attrPos[j];
                for(int i = 0; i < j; ++i) {
                  if(ap.localName.equals(attrPos[i].localName)
                    && ( (ap.uri != null && ap.uri.equals(attrPos[i].uri))
                        || (ap.uri == null && attrPos[i].uri == null))
                  ) {
                     throw new XmlPullParserException(
                       "duplicate attribute name '"+ap.qName+"'"
                       +((ap.uri != null) ? 
                           " (with namespace '"+ap.uri+"')" : "")
                       +" and "    
                       +((ap.uri != null) ? 
                           " (with namespace '"+ap.uri+"')" : "")
                       +getPosDesc(), getLineNumber(), getColumnNumber());
                  }
                }
              }
            }

          } else { // no namespaces
            el.prefix = null;
            el.localName = el.qName;
            el.uri = "";
          }
          //SAX2 startElement(el.uri, el.localName, el.qName, el)
          return eventType = START_TAG;

        case XmlTokenizer.DOCTYPE:
          throw new XmlPullParserException(
             "<![DOCTYPE declarations not supported"
             +getPosDesc(), getLineNumber(), getColumnNumber());

        default:
          throw new XmlPullParserException("unknown token "+token
             +getPosDesc(), getLineNumber(), getColumnNumber());
        }
      } while(token != XmlTokenizer.STAG_END);
    } catch(XmlTokenizerException ex) {
      throw new XmlPullParserException("tokenizer exception", ex);
    }
    throw new XmlPullParserException(
      "invalid state of tokenizer token="+token);
  }

  /** 
   * Return true if just read CONTENT contained only white spaces.
   */
  public boolean whitespaceContent() throws XmlPullParserException {
    if(eventType != CONTENT)
      throw new XmlPullParserException("no content available to read");
    return tokenizer.seenContent == false;  
  }  
  
  /**
   * Return String that contains just read CONTENT.
   */
  public String readContent() throws XmlPullParserException {
    if(eventType != CONTENT)
      throw new XmlPullParserException("no content available to read");
    if(elContent == null) {
      if(tokenizer.parsedContent) {
        elContent = new String(tokenizer.pc, tokenizer.pcStart, 
          tokenizer.pcEnd - tokenizer.pcStart);
        //scache.convert(tokenizer.pc, tokenizer.posStart, tokenizer.posEnd, true);
      } else {
        //System.out.println("posStart="+tokenizer.posStart+" posEnd="+tokenizer.posEnd);
        elContent = new String(tokenizer.buf, 
          tokenizer.posStart, tokenizer.posEnd - tokenizer.posStart);
        //scache.convert(tokenizer.buf, tokenizer.posStart, tokenizer.posEnd, true);
      }
    }
    return elContent; 
  }

  /**
   * Read value of just read END_TAG into passed as argument EndTag.    
   */
  public void readEndTag(EndTag etag) throws XmlPullParserException {
    if(eventType != END_TAG)
      throw new XmlPullParserException(
        "no end tag available to read"+getPosDesc(), getLineNumber(), getColumnNumber());
    etag.qName = elStack[elStackDepth].qName;
    etag.uri = elStack[elStackDepth].uri;
    etag.localName = elStack[elStackDepth].localName;
  }

  /**
   * Read value of just read START_TAG into passed as argument StartTag.    
   */
  public void readStartTag(StartTag stag) 
      throws XmlPullParserException, IOException 
  {
    if(eventType != START_TAG)
      throw new XmlPullParserException(
        "no start tag available to read"+getPosDesc(), getLineNumber(), getColumnNumber());
    //if(beforeAtts == false)
    //  throw new XmlPullParserException(
    //    "start tag was already read"+getPosDesc(), getLineNumber(), getColumnNumber());      
    ElementContent el = elStack[elStackDepth - 1];
    stag.qName = el.qName;
    stag.uri = el.uri;
    stag.localName = el.localName;
    
    // process atttributes
    stag.ensureCapacity(attrPosEnd);
    stag.attEnd = attrPosEnd;
		for(int i = 0; i < attrPosEnd; ++i) {
		  Attribute ap = attrPos[i];
      // place for next attribute value
      Attribute att = stag.attArr[i];  
      att.qName =  ap.qName;
      att.localName = ap.localName;
      att.value = ap.value;
      att.uri = ap.uri;
    }
  }


  // ====== utility methods 
  /**
   * Make sure that we have enough space to keep element stack if passed size.
   */  
  private void ensureCapacity(int size) {
    int newSize = 2 * size;
    if(newSize == 0)
      newSize = 25;
    if(elStackSize < newSize) {
      ElementContent[] newStack = new ElementContent[newSize];
      if(elStack != null) {
        System.arraycopy(elStack, 0, newStack, 0, elStackSize);
      }
      for(int i = elStackSize; i < newSize; ++i) {
        newStack[i] = new ElementContent();
      }
      elStack = newStack;
      elStackSize = newSize;
    }
  }

  /**
   * Make sure that in attributes temporary array is enough space.
   */
  private void ensureAttribs(int size) {
    int newSize = 2 * size;
    if(newSize == 0)
      newSize = 25;
    if(attrPosEnd < newSize) {
      Attribute[] newAttrPos = new Attribute[newSize];
      if(attrPos != null) {
        System.arraycopy(attrPos, 0, newAttrPos, 0, attrPosSize);
      }
      for(int i = attrPosSize; i < newSize; ++i) {
        newAttrPos[i] = new Attribute(); //inner classes are weird  :-)
      }
			attrPos = newAttrPos;
			attrPosSize = newSize;
    }
  }
      

  // ===== internals

  /** 
   * Should attribute uniqueness be checked for attributes 
   * as in specified XML and NS specifications?
   */
  private final static boolean CHECK_ATTRIB_UNIQ = true;
  //private boolean beforeAtts;
  /** Have we read empty element? */ 
  private boolean emptyElement;
  /** Have we seen root element */
  private boolean seenRootElement;
  /** Content of current element if in CONTENT state */
  private String elContent;

  /** XML tokenizer that is doing actual tokenizning of input stream. */  
  protected XmlTokenizer tokenizer = new XmlTokenizer();
  /** what is current evebt type as returned from next()? */
  protected byte eventType;
  /*8 what is current token returned form tokeizer */
  private byte token;

  //private StringCache scache = new StringCache(); //FIXME
  
  // mapping namespace prefixes to uri
  /** should parser support namespaces? */
  private boolean supportNs = true;
  /** mapping of names prefixes to uris */
  private Map prefix2Ns = new HashMap();   

/*
  private String stagValue;
  private String stagPC;
  private int stagEnd;
  private int stagPCStart;
  private int stagPCEnd;
  private int stagValueStart;
  private int stagValueEnd;
  private int stagPosNsColon;
  //private int stagNsColonCount;
*/
  /** index for last attribute in attrPos array */
	private int attrPosEnd;
  /** size of attrPos array */
	private int attrPosSize;
	/** temprary array of all attributes */
	private Attribute attrPos[];
  
  // for validating element pairing and string namespace context
  /** how many elements are on elStack */
  private int elStackDepth;
  /** size of elStack array */
  private int elStackSize;
  /** temprary array to keep ElementContent stack */
  private ElementContent[] elStack;

  /** 
   * Utility class to kee information about element such as name etc.
   * and if namespaces enabled also list of declared prefixes and
   * previous values of prefixes namespace uri (to pop them from stack)
   */
  private class ElementContent { 
    String qName;
    String uri;
    String localName;
    String prefix;

    String defaultNs;

    int prefixesEnd;
    int prefixesSize;
    String[] prefixes;
    String[] prefixPrevNs;

    private void ensureCapacity(int size) {
      int newSize = 2 * size;
      if(newSize == 0)
        newSize = 25;
      if(prefixesSize < newSize) {
        String[] newPrefixes = new String[newSize];
        String[] newPrefixPrevNs = new String[newSize];
        if(prefixes != null) {
          System.arraycopy(prefixes, 0, newPrefixes, 0, prefixesEnd);
          System.arraycopy(
            prefixPrevNs, 0, newPrefixPrevNs, 0, prefixesEnd);
        }
        prefixes = newPrefixes;
        prefixPrevNs = newPrefixPrevNs;
        prefixesSize = newSize;
      }
    }

  };
    
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
