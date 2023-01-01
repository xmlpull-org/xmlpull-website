//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XmlTokenizer.java,v 1.36 2001/08/15 21:37:24 aslom Exp $
 */

package sxt;

import java.io.*;

/**
 * Simpe XML Tokenizer (SXT) performs input stream tokenizing.
 * 
 * Advantages:<ul>
 * <li>utility class to simplify creation of XML parsers, especially 
 *    suited for pull event model but can support also push (SAX2)
 * <li>small footprint: whole parser is in one file 
 * <li>minimal memory utilization: does not use memory except for input
 *    and content buffer (that can grow in size)  
 * <li>fast: all parsing done in one function (simple automata)
 * <li>supports most of XML 1.0 (except validation and external entities)
 * <li>low level: supports on demand parsing of Characters, 
 *    CDSect, Comments, PIs etc.)
 * <li>parsed content: supports providing on demand  
 *     parsed content to application (standard entities expanded
 *    all CDATA sections inserted, Comments and PIs removed)
 *    not for attribute values and element content
 * <li>mixed content: allow to dynamically disable mixed content
 * <li>small - total compiled size around 15K
 * </ul>
 *
 * Limitations:<ul>
 * <li>it is just a tokenizer - does not enforce grammar
 * <li>readName() is using Java identifier rules not XML
 * <li>does not parse DOCTYPE declaration (skips everyting in [...])
 * </ul>
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */
public class XmlTokenizer {
  //enumeration of tokens that can be returned 
  public final static byte END_DOCUMENT          = 2;        
  public final static byte CONTENT               = 10;        
  public final static byte CHARACTERS            = 20;
  public final static byte CDSECT                = 30;
  public final static byte COMMENT               = 40;
  public final static byte DOCTYPE               = 50;
  public final static byte PI                    = 60;
  public final static byte ENTITY_REF            = 70;
  public final static byte CHAR_REF              = 75;
  public final static byte ETAG_NAME             = 110;  
  public final static byte EMPTY_ELEMENT         = 111;  
  public final static byte STAG_END              = 112;  
  public final static byte STAG_NAME             = 120;  
  public final static byte ATTR_NAME             = 122;  
  public final static byte ATTR_CHARACTERS       = 124;  
  public final static byte ATTR_CONTENT          = 127;  

  // parameters controlling tokenizer behaviour
  public boolean paramNotifyCharacters;
  public boolean paramNotifyComment;
  public boolean paramNotifyCDSect;
  public boolean paramNotifyDoctype;
  public boolean paramNotifyPI;
  public boolean paramNotifyCharRef;
  public boolean paramNotifyEntityRef;
  public boolean paramNotifyAttValue;
  
  public char[] buf = new char[BUF_SIZE];
  public int pos;
  public int posStart;
  public int posEnd;
  public int posNsColon;
  public int nsColonCount;
  public boolean seenContent;

  public boolean parsedContent;
  public char[] pc = new char[BUF_SIZE];
  public int pcStart;
  public int pcEnd;

  public XmlTokenizer() {
  }
    
  public void reset() {
     // release buffer that may have been used by setInput(char[])...
    if(!reading) {  // data was taken from input char[]
      //|| ((hardLimit != -1) && (buf.length < hardLimit))
      //) {
      //if(hardLimit != -1) {
      if(softLimit != -1) {  
        resize(softLimit);
      } else {
        resize(BUF_SIZE);      
      }
    }
    resetState();
  }

  private void resetState() {
    reading = true;
    bufSize  = buf.length;
    bufEnd = 0;
    posEnd = posStart = pos = 0;
    posNsColon = -1;
    state = STATE_INIT;
    prevCh = '\0';
    posCol = posRow = 1;
    reachedEnd = false;
    pcEnd = pcStart = 0;
    previousState = -1;
    backtracking = false;
    seenContent = false;
  }

  /** Reset tokenizer state and set new input source */  
  public void setInput(char[] data) {
    resetState();
    reading = false;
    buf = data;
    bufSize = bufEnd = buf.length;
    if(paramPC && pc.length < bufSize) {
      pc = new char[bufSize];    
      if(TEST_VALIDATING) for(int i = 0; i < bufSize; ++i) pc[i]='X';
    }
  }

  /** Reset tokenizer state and set new input source */  
  public void setInput(Reader r) {
    reset();
    reading = true;
    reader = r;
    bufEnd = 0;
  }

  /** 
   * Set notification of all XML content tokens:
   * Characters, Comment, CDSect, Doctype, PI, EntityRef, CharRef, AttValue 
   * (tokens for STag, ETag and Attribute are always sent).
   */
  public void setNotifyAll(boolean enable) {
    paramNotifyCharacters = enable;
    paramNotifyComment = enable;
    paramNotifyCDSect = enable;
    paramNotifyDoctype = enable;
    paramNotifyPI = enable;
    paramNotifyEntityRef = enable;
    paramNotifyCharRef = enable;
    paramNotifyAttValue = enable;
  }

  /**
   * Allow reporting parsed content for element content 
   * and attribute content (no need to deal with low level
   * tokens such as in setNotifyAll).
   */
  public void setParseContent(boolean enable) {
    paramPC = enable;
    if(paramPC && pc.length < bufSize) {
      pc = new char[bufSize];    
    }
  }

  /**
   * Set support for mixed conetent. If mixed content is
   * disabled tokenizer will do its best to ensure that
   * no element has mixed content model also ignorable whitespaces
   * will not be reported as element content. 
   */
  public void setMixedContent(boolean enable) {
    paramNoMixContent = !enable;
  }

  /**
   * Set soft limit on internal buffer size.
   * That means suggested size that tokznzier will try to keep.
   */
  public void setSoftLimit(int value) throws XmlTokenizerException {
    //if(state != STATE_INIT) {
    //  throw new XmlTokenizerException(
    //    "soft limit can not be changed after parsing started");
    //}
    if(!reading) {
      throw new XmlTokenizerException(
       "hard limit can not be set for char array input"
       );    
    }
    if((value != -1) && (hardLimit != -1) && (2 * value > hardLimit)) {
      throw new XmlTokenizerException(
        "soft limit can no tbe bigger than half of hard limit"
        +"current hard limit "+hardLimit
        );
    
    }
    softLimit = value;
    if(softLimit != -1) {
      posSafe = softLimit;
    } else if(hardLimit != -1) {
      posSafe = hardLimit / 2;
    } else {
      posSafe = (int)(loadFactor * bufSize); //restore default    
    }
  }

  /**
   * Set hard limit on internal buffer size.
   * That means that if input (such as element content) is bigger than
   * hard limit size tokenizer will throw XmlTokenizerBufferOverflowException.
   */
  public void setHardLimit(int value) throws XmlTokenizerException {
    if(!reading) {
      throw new XmlTokenizerException(
       "hard limit can not be set for char array input"
       );    
    }
    if(state != STATE_INIT && value < hardLimit) {
      throw new XmlTokenizerException(
       "hard limit on buffer size can not be shrunk during parsing"
       );
    }
    if(softLimit == -1 && value != -1) {
      throw new XmlTokenizerException(
        "soft limit must be set to non -1 before setting hard limit"
        +getPosDesc(), getLineNumber(), getColumnNumber()
        );
    }
    if((value != -1) && ((2 * softLimit) >= value)) {
      throw new XmlTokenizerException(
        "hard limit must be at least twice the size of soft limit"
        +"current soft limit "+softLimit+" and hard limit "+value
        +getPosDesc(), getLineNumber(), getColumnNumber()
        );
    
    }
    // resize buffer to new hard limit
    hardLimit = value;
    if(softLimit != -1 && softLimit < bufSize) {
      resize(softLimit);
    }
  }

  private int findFragment(char[] b, int i, int j) {
    if(i == 0) return i;
    while(i-- > 0) {
      if((j - i) > 55) break;
      char c = b[i];
      if(c == '<') break;
    }
    return i;
  }

  /**
   * Return string describing current position of parsers as
   * text 'at line %d (row) and column %d (colum) [seen %s...]'.
   */
  public String getPosDesc() {
    String fragment = null;
    if(parsedContent) {
      //System.err.println("pcStart="+pcStart+" pcEnd="+pcEnd);
      //if(pcStart > 0) fragment = "...";
      if(pcStart <= pcEnd) {
        int start = findFragment(pc, pcStart, pcEnd);
        fragment = new String(pc, start, pcEnd - start);
        if(start > 0) fragment = "..." + fragment;
      }            
    } else {
      //System.err.println("posStart="+posStart+" posEnd="+posEnd);
      //if(posStart > 0) fragment = "...";
      if(posStart <= posEnd) {
        int start = findFragment(buf, posStart, posEnd);
        fragment = new String(buf, start, posEnd - start);
        if(start > 0) fragment = "..." + fragment;
      }
    }
    return " at line "+posRow
      +" and column "+(posCol-1)
      +(fragment != null ? " seen "+printable(fragment)+"..." : "");
  }
  
  public int getLineNumber() { return posRow; }
  public int getColumnNumber() { return posCol-1; }
  
  /**
   * Return next recognized toke or END_DOCUMENT if no more input.
   *
   * <p>This is simple automata (in pseudo-code):
   * <pre>
   * byte next() {
   *    while(state != END_DOCUMENT) {
   *      ch = more();  // read character from input
   *      state = func(ch, state); // do transition
   *      if(state is accepting)
   *        return state;  // return token to caller
   *    }
   * }  
   * </pre>
   *
   * <p>For simplicity it is using few procedures such as readName() or isS().
   *
   *
   *
   */
  public byte next() throws XmlTokenizerException, IOException {
    if(state == STATE_FINISHED)
      throw new XmlTokenizerException("attempt to read beyond end of input");

    parsedContent = false;

LOOP:
    while(true) {
      if(reachedEnd) {
        if(state != STATE_FINISH) {
          if(state != STATE_CONTENT && state != STATE_CONTENT_INIT
             && state != STATE_CONTENT_CONTINUED) {
            throw new XmlTokenizerException(
              "unexpected end of stream (state="+state+")");
          }
          if(state == STATE_CONTENT_INIT || state == STATE_CONTENT_CONTINUED) {
            if(state == STATE_CONTENT_INIT) {
              pcEnd = pcStart = pos - 1;
            }
            posEnd = posStart = pos - 1;
          }
          state = STATE_FINISH;  
          if(paramPC && (pcStart != pcEnd || posEnd != posStart)) {
          //  if(pcEnd == pcStart) {
          //    pcStart = posStart;
          //    pcEnd = posEnd;
          //    pc = buf;
          //  } else {
          //    pc = pc;
          //  }
            parsedContent = (pcEnd != pcStart);
            if(paramNoMixContent == false || seenContent == false)
              return CONTENT;
            else if(parsedContent)
              throw new XmlTokenizerException(
                "no element content allowed before end of stream");
          }
        }
        state = STATE_FINISHED;
        if(TRACE_SIZING) {
          System.err.println("bufEnd="+bufEnd+" bufSize="+bufSize
            +" softLimit="+softLimit+" hardLimit="+hardLimit);          
        }
        return END_DOCUMENT;
      }
      char ch = more();
      // 2.11 End-of-Line Handling: "\r\n" -> "\n"; "\rX" -> "\nX"
      //XXX
      if(NORMALIZE_LINE_BREAKS) {
        if(ch == '\r') {
          // TODO: joinPC()
          if(pcStart == pcEnd && posEnd > posStart) {      
            int len = posEnd - posStart;
            System.arraycopy(buf, posStart, pc, pcEnd, len);
            pcEnd += len;
          }
          //ch = '\n';
        } else if(prevPrevCh == '\r' && ch == '\n') {
          continue LOOP; //ask for more chars --> ch = more();
          // it can not be break as we are not yet in switch(..)
        }
      }
      switch(state) {
      case STATE_INIT:
        // detect BOM and frop it (Unicode Byte Order Mark)
        if(ch == '\uFFFE') {
          throw new XmlTokenizerException(
            "first character in input was UNICODE noncharacter (0xFFFE)"+
            "- input requires byte swapping");
        }
        if(ch == '\uFEFF') {
          // skupping UNICODE BOM
          state = STATE_CONTENT_INIT;
          break;
        }
        ; // fall through
      case STATE_CONTENT_INIT:
        pcEnd = pcStart = pos - 1;
        ; // fall through
      case STATE_CONTENT_CONTINUED:
        posEnd = posStart = pos - 1;
        state = STATE_CONTENT;
        ; // fall through
      case STATE_CONTENT:
        if(ch == '<') {
          state = STATE_SEEN_LT;
          if(paramNotifyCharacters && posStart != posEnd)
            return CHARACTERS;  
        } else if(ch == '&') {  
          if(paramPC && pcStart == pcEnd && posEnd > posStart) {      
            // TODO: joinPC()
            int len = posEnd - posStart;
            System.arraycopy(buf, posStart, pc, pcEnd, len);
            pcEnd += len;
          }   
          if(!seenContent) {
            seenContent = true;     
            if(paramNoMixContent && !mixInElement)
              throw new XmlTokenizerException(
                "mixed content disallowed outside element"+getPosDesc(), getLineNumber(), getColumnNumber());
          }
          state = STATE_SEEN_AMP;
          previousState = STATE_CONTENT_CONTINUED;
          posStart = pos - 1;
        } else {
          if(!seenContent && !isS(ch)) {
            seenContent = true;     
            if(paramNoMixContent && !mixInElement)
              throw new XmlTokenizerException(
                "mixed content disallowed outside element, "
                +"character '"+printable(ch)+"'"
                +" ("+(int)ch+")"+getPosDesc(), getLineNumber(), getColumnNumber());
          }
          posEnd = pos;          
          if(paramPC && 
            ((pcStart != pcEnd) || (NORMALIZE_LINE_BREAKS && ch == '\r'))
          ) {
          //NOTE: normalization is done at beginning of LOOP
          //but it only will prepare pc buffer that is only now filled
          // CONSIDER: worst case when \r is first character!!!!
            if(NORMALIZE_LINE_BREAKS && ch == '\r') 
              pc[pcEnd++] = '\n';
            else
              pc[pcEnd++] = ch;
          }
          //if(NORMALIZE_LINE_BREAKS && ch == '\r') {
          //  throw new IllegalStateException(
          //    "end-of-line normalization failed"+getPosDesc());
          //}
          if(paramNotifyCharacters && reachedEnd)
            return CHARACTERS;
        }
        break;
      case STATE_SEEN_LT:
        if(ch == '!') {
          state = STATE_SEEN_LT_BANG;
        } else if(ch == '?') {
          state = STATE_PI;
        } else { // it must be STag or ETag
          boolean prevMixSeenContent = seenContent;
          boolean prevMixInElement = mixInElement;
          if(ch == '/') {
            state = STATE_SCAN_ETAG_NAME;
            mixInElement = false;
          } else {
            state = STATE_SCAN_STAG_NAME;
            if(paramNoMixContent && seenContent)
              throw new XmlTokenizerException("mixed content disallowed"
                +" inside element and before start tag"+getPosDesc(), getLineNumber(), getColumnNumber());            
            mixInElement = true;
          }          
          //TODO TESTME 
          if(paramPC /*&& (pcStart != pcEnd || posEnd != posStart)*/) {
            parsedContent = (pcEnd != pcStart);
            if(paramNoMixContent == false
                || (paramNoMixContent && state == STATE_SCAN_ETAG_NAME
                    //&& prevMixInElement && prevMixSeenContent)) {
                    && prevMixInElement)) {
              return CONTENT;
            }  
          }
        }
        // gather parsed content - so we have what was before comments etc.
        if(paramPC && state != STATE_SCAN_STAG_NAME 
           && state != STATE_SCAN_ETAG_NAME) 
        {
          // TODO: joinPC()
          if(pcStart == pcEnd && posEnd > posStart) {      
            int len = posEnd - posStart;
            System.arraycopy(buf, posStart, pc, pcEnd, len);
            pcEnd += len;
          }
        }
        posStart = pos;  // to make PI start content
        break;
      case STATE_SEEN_LT_BANG:
        if(ch == '-') {
          ch = more();
          if(ch != '-') 
            throw new XmlTokenizerException(
              "expected - for start of comment <!-- not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
          state = STATE_COMMENT;
          posStart = pos;
        } else if(ch == '[') {
          ch = more(); if(ch != 'C') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'D') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'A') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'T') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'A') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != '[') 
            throw new XmlTokenizerException("expected <![CDATA"+getPosDesc(), getLineNumber(), getColumnNumber());
          posStart = pos;
          if(!seenContent) {
            seenContent = true;     
            if(paramNoMixContent && !mixInElement)
              throw new XmlTokenizerException("mixed content disallowed outside element"+getPosDesc(), getLineNumber(), getColumnNumber());
          }
          state = STATE_CDSECT;
        } else if(ch == 'D') {
          ch = more(); if(ch != 'O') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'C') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'T') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'Y') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'P') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          ch = more(); if(ch != 'E') 
            throw new XmlTokenizerException("expected <![DOCTYPE"+getPosDesc(), getLineNumber(), getColumnNumber());
          posStart = pos;
          state = STATE_DOCTYPE;
        } else {
          throw new XmlTokenizerException(
            "unknown markup after <! "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        }
        break;

      // [66]-[68] reference ; 4.1 Character and Entity Reference  
      case STATE_SEEN_AMP:
        posStart = pos - 2;
        if(ch == '#') {
          state = STATE_CHAR_REF;
          break;
        }
        state = STATE_ENTITY_REF;
        ; // fall through
      case STATE_ENTITY_REF:
        if(ch == ';') {
          state = previousState;
          posEnd = pos;
          // 4.6 Predefined Entities
          if(paramPC) {
            int i = posStart + 1;
            int j = pos - 1;
            int len = j - i;
            if(len == 2 && buf[i] == 'l' && buf[i+1] == 't') {
              pc[pcEnd++] = '<';
            } else if(len == 3 && buf[i] == 'a' 
                      && buf[i+1] == 'm' && buf[i+2] == 'p') {
              pc[pcEnd++] = '&';
            } else if(len == 2 && buf[i] == 'g' && buf[i+1] == 't') {
              pc[pcEnd++] = '>';
            } else if(len == 4 && buf[i] == 'a' && buf[i+1] == 'p'
                   && buf[i+2] == 'o' && buf[i+3] == 's')
            {
              pc[pcEnd++] = '\'';
            } else if(len == 4 && buf[i] == 'q' && buf[i+1] == 'u'
                   && buf[i+2] == 'o' && buf[i+3] == 't')
            {
              pc[pcEnd++] = '"';
            } else {
              String s = new String(buf, i, j - i);
              throw new XmlTokenizerException(
                "undefined entity "+s+getPosDesc(), getLineNumber(), getColumnNumber());
            }  
          }
          if(paramNotifyEntityRef)
            return ENTITY_REF;                    
        }
        break;
      case STATE_CHAR_REF:
        charRefValue = 0;
        state = STATE_CHAR_REF_DIGITS;
        if(ch == 'x') {
          charRefHex = true;
          break;
        }
        charRefHex = false;
        ; // fall through
      case STATE_CHAR_REF_DIGITS:
        if(ch == ';') {
          if(paramPC) { 
            pc[pcEnd++] = charRefValue;
          }
          state = previousState;
          posEnd = pos;
          if(paramNotifyCharRef)
            return CHAR_REF;                    
        } else if(ch >= '0' && ch <= '9') {
          if(charRefHex) {
            charRefValue = (char)(charRefValue * 16 + (ch - '0'));
          } else {
            charRefValue = (char)(charRefValue * 10 + (ch - '0'));
          }        
        } else if(charRefHex && ch >= 'A' && ch <= 'F') {
            charRefValue = (char)(charRefValue * 16 + (ch - 'A' + 10));
        } else if(charRefHex && ch >= 'a' && ch <= 'f') {
            charRefValue = (char)(charRefValue * 16 + (ch - 'a' + 10));
        } else {
          throw new XmlTokenizerException(
            "character reference may not contain "+ch+getPosDesc(), getLineNumber(), getColumnNumber());        
        }
        break;

      // [40]-[44]; 3.1 Start-Tags, End-Tags, and Empty-Element Tags
      case STATE_SCAN_ETAG_NAME:
        seenContent = false;
        posStart = pos - 1;
        ch = readName(ch);
        posEnd = pos - 1;
        ch = skipS(ch);
        if(ch != '>')
          throw new XmlTokenizerException(
            "expected > for end tag not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        state = STATE_CONTENT_INIT;
        return ETAG_NAME;

      case STATE_SCAN_STAG_NAME:
        // dangerous call!!!!
        if(reading && pos > 2 
          //&& (bufEnd - pos) <= 64
            && pos > posSafe
        ) {
           shrink(pos - 2);
        }

        seenContent = false;
        ch = less();
        posStart = pos - 1;
        ch = readName(ch);
        posEnd = pos - 1;        
        ch = less();
        //if(ch != '>')
        state = STATE_SCAN_ATTR_NAME;
        pcEnd = pcStart = 0; // to have place for attribute content        
        return STAG_NAME;


      case STATE_SCAN_STAG_GT:
        if(ch == '>') {
          state = STATE_CONTENT_INIT;
          posStart = pos -1;
          posEnd = pos;
          return STAG_END;
        } else {
          throw new XmlTokenizerException(
            "expected > for end of start tag not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        }
      case STATE_SCAN_ATTR_NAME:
        pcStart = pcEnd;
        ch = skipS(ch);
        if(ch == '/') { // [44] EmptyElemTag
          state = STATE_SCAN_STAG_GT;
          posStart = pos -1;
          posEnd = pos;
          mixInElement = false;
          return EMPTY_ELEMENT;
        } else if(ch == '>') {
          state = STATE_CONTENT_INIT;
          posStart = pos -1;
          posEnd = pos;
          return STAG_END;
        }
        posStart = pos - 1;
        ch = readName(ch);
        posEnd = pos - 1;
        ch = less();
        state = STATE_SCAN_ATTR_EQ;
        return ATTR_NAME;
      case STATE_SCAN_ATTR_EQ:
        ch = skipS(ch);
        if(ch != '=')
          throw new XmlTokenizerException(
            "expected = after attribute name not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        state = STATE_SCAN_ATTR_VALUE;
        break;
      case STATE_SCAN_ATTR_VALUE: // [10] AttValue
        ch = skipS(ch);
        if(ch != '\'' && ch != '"')
          throw new XmlTokenizerException(
            "attribute value must start with double quote"
            +" or apostrophe not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        attrMarker = ch;
        state = STATE_SCAN_ATTR_VALUE_CONTINUE;
        //make sure that attr value such as \r will be in buffer beginning
        posEnd = posStart = pos; 
        pcEnd = pcStart; //remove possible attr name form PC buffer
        break; 
      case STATE_SCAN_ATTR_VALUE_CONTINUE: 
        state = STATE_SCAN_ATTR_VALUE_END;
        ; // fall through
      case STATE_SCAN_ATTR_VALUE_END:
        if(ch == attrMarker) {
          if(paramPC) {
            state = STATE_ATTR_VALUE_CONTENT;
          } else {
            state = STATE_SCAN_ATTR_NAME;
          }
          if(paramNotifyAttValue)
            return ATTR_CHARACTERS;
         } else if(ch == '&') {  
          if(paramPC && (pcEnd == pcStart) && (posEnd > posStart)) {      
            // TODO: joinPC()
            int len = posEnd - posStart;
            System.arraycopy(buf, posStart, pc, pcEnd, len);
            pcEnd += len;
          }        
          state = STATE_SEEN_AMP;
          previousState = STATE_SCAN_ATTR_VALUE_CONTINUE;
          if(paramNotifyAttValue)
            return ATTR_CHARACTERS;
        } else if(ch == '<') {  
          throw new XmlTokenizerException(
            "attribute value can not contain "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        } else {
          //if(NORMALIZE_LINE_BREAKS && ch == '\r') {
          //  throw new IllegalStateException(
          //    "end-of-line normalization failed"+getPosDesc());
          //}
          //if(paramPC && pcStart != pcEnd
            //|| (paramPC && NORMALIZE_LINE_BREAKS && ch == '\r') 
          //) {
          if(paramPC && 
            ((pcStart != pcEnd) 
              //|| (NORMALIZE_LINE_BREAKS && ch == '\r')
              || ch == '\t' || ch == '\n' || ch == '\r')
          ) {
            //if(NORMALIZE_LINE_BREAKS && ch == '\r') {
            //  pc[pcEnd++] = '\n';
            //} else {
            //  pc[pcEnd++] = ch;
            //}

            // TODO: joinPC()
            if(pcStart == pcEnd && posEnd > posStart) {      
              int len = posEnd - posStart;
              System.arraycopy(buf, posStart, pc, pcEnd, len);
              pcEnd += len;
            }

            // 3.3.3 Attribute-Value Normalization
            if(ch == '\t' || ch == '\n' || ch == '\r') {
              pc[pcEnd++] = ' ';
            } else {
              pc[pcEnd++] = ch;
            
            }
          } else {
            posEnd = pos;
          }
        }
        break;
      case STATE_ATTR_VALUE_CONTENT:
        ch = less();
        // finishPC()
        parsedContent = (pcEnd != pcStart);
        state = STATE_SCAN_ATTR_NAME;
        return ATTR_CONTENT;
                
      // [18] - [21] CDSEct; 2.7 CDATA Sections  
      case STATE_CDSECT:
        if(ch == ']')
          state = STATE_CDSECT_BRACKET;
        break;  
      case STATE_CDSECT_BRACKET:
        if(ch == ']')
          state = STATE_CDSECT_BRACKET_BRACKET;
        else 
          state = STATE_CDSECT;
        break;  
      case STATE_CDSECT_BRACKET_BRACKET:
        if(ch == '>') {
          state = STATE_CONTENT_CONTINUED;
          posEnd = pos - 3;
          //TODO: avoid memory copy for <m:bar><![CDATA[TEST]]></m:bar> 
          if(paramPC && posEnd > posStart) { 
            int len = posEnd - posStart;
            System.arraycopy(buf, posStart, pc, pcEnd, len);
            pcEnd += len;
          }
          if(paramNotifyCDSect) 
            return CDSECT;
        } else {
          state = STATE_CDSECT;
        }
        break;        

      // [15] Comment; 2.5 Comments
      case STATE_COMMENT:
        if(ch == '-')
          state = STATE_COMMENT_DASH;
        break;  
      case STATE_COMMENT_DASH:  
        if(ch == '-')
          state = STATE_COMMENT_DASH_DASH;
        else
          state = STATE_COMMENT;  
        break;  
      case STATE_COMMENT_DASH_DASH:  
        if(ch == '>') {
          state = STATE_CONTENT_CONTINUED;
          posEnd = pos - 3;
          if(paramNotifyComment) 
            return COMMENT;
        } else {
          state = STATE_COMMENT;
        }
        break;

      // [28] doctypedecl; 2.8 Prolog and Document Type Declaration
      case STATE_DOCTYPE:
        if(ch == '[')
          state = STATE_DOCTYPE_BRACKET;
        else if(ch == '>') {
          state = STATE_CONTENT_CONTINUED;
          posEnd = pos - 1;
          if(paramNotifyDoctype) 
            return DOCTYPE;                
        }
        break;  
      case STATE_DOCTYPE_BRACKET:  
        if(ch == ']')
          state = STATE_DOCTYPE_BRACKET_BRACKET;
        break;  
      case STATE_DOCTYPE_BRACKET_BRACKET:  
        ch = skipS(ch);
        if(ch == '>') {
          state = STATE_CONTENT_CONTINUED;
          posEnd = pos - 1;
          if(paramNotifyDoctype) 
            return DOCTYPE;
        } else {
          throw new XmlTokenizerException(
            "expected > for DOCTYPE end not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
        }
        break;

      // [16]-[17] PI; 2.6 Processing Instructions
      case STATE_PI:  //TODO: enforce "XML" as reserved prefix 
        if(ch == '?')
          state = STATE_PI_END;
        break;
      case STATE_PI_END:
        if(ch == '>') {
          state = STATE_CONTENT_CONTINUED;
          posEnd = pos - 2;
          if(paramNotifyPI) 
            return PI;
        }
        break;
      default:
        throw new XmlTokenizerException(
          "invalid internal state "+state+getPosDesc(), getLineNumber(), getColumnNumber());
      }
    }
  }

  // ========= input buffer management

  /**
   * Get next available character from input.
   * If it is last character set internal flag reachedEnd.
   * If there are no more characters throw EOFException.
   */ 
  private char more() throws IOException, XmlTokenizerException {
    if(backtracking) {
      backtracking = false;
      //return prevCh = ch;  
      ++pos;
      ++posCol;
      if(TRACING) System.err.println(
        "TRACING BACK XmlTokenizer ch="+printable(prevCh)+" state="+state
                    +" posStart="+posStart+" posEnd="+posEnd
                    +" pcStart="+pcStart+" pcEnd="+pcEnd);
      //prevCh = prevPrevCh;
      return prevCh;  
    }
    if(!reading) {
      if(pos == bufEnd - 1)
        reachedEnd = true;
      if(pos >= bufEnd) 
        throw new EOFException("no more data available");
    } else if(hardLimit != -1 && (pos >= hardLimit - 1)) {
      // no more palce to grow...
      throw new XmlTokenizerBufferOverflowException(
        "reached hard limit on buffer size"
         +getPosDesc(), getLineNumber(), getColumnNumber());      
    } else if(pos >= bufEnd - 1) { 
      if(hardLimit != -1 && bufSize > hardLimit) {
        throw new IllegalStateException(
          "buffer size should never exceed hard limit"+getPosDesc());
      }      
      int spaceLeft = bufSize - bufEnd;
      if(spaceLeft <= readChunkSize) { //expand buffer to accomodate read
        int newSize = 2 * bufSize;
        if(hardLimit == -1) {
          if(newSize < softLimit) {
            newSize = 2 * softLimit;
          }
          if(newSize < 4*readChunkSize) {
            newSize = 12 * readChunkSize;
          }
        } else if(newSize > hardLimit) {  //hardLimit != -1
          if(bufEnd >= hardLimit) {
            throw new XmlTokenizerBufferOverflowException(
              "buffer can not grow beyond hard limit"
               +getPosDesc(), getLineNumber(), getColumnNumber());
          }
          if(TRACE_SIZING) {
            System.err.println("limiting new size to hard limit");
          }
          newSize = hardLimit;
        }
        if(newSize - bufSize > 0) {
          resize(newSize);
        }
      }
      int chunkie = readChunkSize;
      // sorry that is only how much we are allowed to read - hard limit..
      if(bufSize - bufEnd < chunkie) {  
        chunkie = bufSize -  bufEnd;
      }
      if(reader == null) {
        throw new XmlTokenizerException(
          "to start parsing setInput() must be called!");
      }
      int bufRead = reader.read(buf, bufEnd, chunkie);
      if(bufRead == -1) {
        reachedEnd = true;
      } if(bufRead == 0) {  
        throw new IllegalStateException("can't read more data in buffer");
      } else {
        bufEnd += bufRead;
      }      
    //} else { //reading && hardLimit != -1
    }
    char ch = buf[pos++];
    
    // update (row,colum) position so far
    // new lines: "\r\n","\r\n"...  "\n","\n",...   "\r","\r","\r"
    if(ch == '\n' || ch == '\r') {
      if ( prevCh != '\r' || ch != '\n' ) {
        posCol = 2; // always one char ahead
        ++posRow;        
      } 
      // 2.11 End-of-Line Handling 
      //XXX
      //if(NORMALIZE_LINE_BREAKS && ch == '\r') {
        
        //ch = '\n';
        // make sure that parsed content will have it correct
        //if(paramPC && pcStart == pcEnd && posEnd > posStart) {      
          //int len = posEnd - posStart;
          //System.arraycopy(buf, posStart, pc, pcEnd, len);
          //pcEnd += len;
        //}   
        //if(paramPC) 
          //pc[pcEnd++] = '\n';
      //}
      //System.err.println("ch = "+printable(ch)+" posRow="+posRow);
    } else {
      ++posCol;
    }
    prevPrevCh = prevCh;
    if(TRACING) System.err.println(
       "TRACING XmlTokenizer ch="+printable(ch)+" state="+state
                    +" posStart="+posStart+" posEnd="+posEnd
                    +" pcStart="+pcStart+" pcEnd="+pcEnd 
                    + " prevCh="+printable(prevCh));
    return prevCh = ch;
  }

  private void resize(int newSize) {
    if(TRACE_SIZING) {
      System.err.println("resizing "+bufSize+" ==> "+newSize);
    }
    char[] newBuf = new char[newSize];
    int end = bufEnd;
    if(end > newSize) { // case of shriniking
      end = newSize;
      if(state != STATE_INIT) { // should never happen...
        throw new IllegalStateException(
          "can not shrink internl buffer during parsing");
      }
    }
    System.arraycopy(buf, 0, newBuf, 0, end);
    buf = newBuf;
    bufSize = newSize;
    if(softLimit == -1) {
      posSafe = (int)(loadFactor * bufSize);
    }
    if(TEST_VALIDATING) for(int i = bufEnd; i < bufSize; ++i) buf[i]='X';
    if(paramPC && pc.length < bufSize); {
      char[] newpc = new char[bufSize];
      System.arraycopy(pc, 0, newpc, 0, pcEnd);
      pc = newpc;
      if(TEST_VALIDATING) for(int i = pcEnd; i < bufSize; ++i) pc[i]='X';
    }       
  }

  /**
   * Special procedure to undo last read character...
   */ 
  private char less() {
    //NOTE: trick - we are backtracing one characker....
    --pos;
    --posCol;
    backtracking = true;
    //return buf[pos - 1];    
    return prevPrevCh;
  }

  /**
   * Not really used but could be to shrink processing buffer size.
   */
  private void shrink(int posCut) {
    //System.err.println("shrink called posCut="+posCut);
    System.arraycopy(buf, posCut, buf, 0, bufEnd - posCut);
    bufEnd -= posCut;
    pos -= posCut;
    posStart -= posCut;
    posEnd -= posCut;
    posNsColon -= posCut;
  }

  // ============ utility methods
  
  /** 
   * Compare char arraychar after cha 
   * - faster than converting to strings
   */
  private static boolean compareCharArr(char[] a, char[] b, 
    int bStart, int bEnd) 
  {
    if(bEnd - bStart != a.length) {
      return false;      
    }
    for(int i = bStart, j = 0; i < bEnd; ++i, ++j) {
      if(a[j] != b[i])
        return false; 
    }
    return true;
  }  

  /** Return printable representation for all specaile chars */
  
  private String printable(char ch) {
    if(ch == '\n') {
      return "\\n";
    } else if(ch == '\r') {
      return "\\r";      
    } else if(ch == '\t') {
      return "\\t";      
    } 
    return ""+ch;
  }   

  private String printable(String s) {
    int iN = s.indexOf('\n');
    int iR = s.indexOf('\r');
    int iT = s.indexOf('\t');
    if((iN != -1) || (iR != -1) || (iT != -1)) {
       StringBuffer buf = new StringBuffer();
       for(int i = 0; i < s.length(); ++i) {
         buf.append(printable(s.charAt(i)));
       }
       s = buf.toString();
    }
    return s;
  }

  /**
   * Read name from input or throw exception ([4] NameChar, [5] Name).
   */ 
  // TODO: make it fully complaint with XML spec
  private char readName(char ch) throws IOException, 
                                      XmlTokenizerException {
    //int type = Character.getType(ch); //TODO: translate [84]-[89] for Java...
    posNsColon = -1;
    nsColonCount = 0;
    if(!Character.isJavaIdentifierStart(ch) && ch != ':' && ch != '_')
      throw new XmlTokenizerException(
        "expected name start not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
    do {
      ch = more();
      if(ch == ':') {
        posNsColon = pos - 1;
        ++nsColonCount;
      }
    } while(Character.isJavaIdentifierPart(ch)
              || ch == '.' || ch == '-'  
              || ch == '_' || ch == ':');    
    return ch;  
  }

  /**
   * Determine if ch is whitespace ([3] S)
   */
  private boolean isS(char ch) {
    return (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
  }


  private char skipS(char ch) throws IOException, 
   XmlTokenizerException //XmlTokenizerBufferOverflowException 
  {
    while(ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r')
      ch = more();
    return ch; 
  }

  private char readS(char ch) throws IOException, XmlTokenizerException {
    if(!isS(ch))
      throw new XmlTokenizerException(
        "expected white space not "+ch+getPosDesc(), getLineNumber(), getColumnNumber());
    while(ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r')
      ch = more();
     return ch; 
  }
  
  // ============ internal parser state


  private final static boolean NORMALIZE_LINE_BREAKS = true;
  private final static boolean TRACING = false;
  private final static boolean TRACE_SIZING = false;
  private final static boolean TEST_VALIDATING = false;

  /** Parsed Content reporting */
  private boolean paramPC = true;
  /** Allow mixed content ? */
  private boolean paramNoMixContent = false;
  private boolean mixInElement = false;
  private boolean backtracking  = false;
  
  private final static int BUF_SIZE = 12 * 1024;;  
  private int readChunkSize = 1024;
  private float loadFactor = 0.99f;
  //private int posSafe = BUF_SIZE - 1024; //2*readChunkSize; // 
  private int posSafe = (int)(loadFactor * BUF_SIZE);
  private int softLimit = -1;
  private int hardLimit = -1;
  private boolean reading = true;
  private Reader reader;
  private int bufEnd;
  private int bufSize = BUF_SIZE;

  // strictly internal
  private int posCol;
  private int posRow;
  private char prevCh;
  private char prevPrevCh;

  private char attrMarker;
  private char charRefValue;
  private boolean charRefHex;
  private boolean reachedEnd;
  private byte previousState;
  private byte state;

  // internal tokenizer automata states
  private final static byte STATE_INIT                    = 1;
  private final static byte STATE_FINISH                  = 6;
  private final static byte STATE_FINISHED                = 7;
  private final static byte STATE_CONTENT_INIT            = 10;
  private final static byte STATE_CONTENT_CONTINUED       = 11;
  private final static byte STATE_CONTENT                 = 12;
  private final static byte STATE_SEEN_LT                 = 13;
  private final static byte STATE_SEEN_LT_BANG            = 14;
  private final static byte STATE_CDSECT                  = 30;
  private final static byte STATE_CDSECT_BRACKET          = 31;
  private final static byte STATE_CDSECT_BRACKET_BRACKET  = 32;
  private final static byte STATE_COMMENT                 = 40;
  private final static byte STATE_COMMENT_DASH            = 41;
  private final static byte STATE_COMMENT_DASH_DASH       = 42;
  private final static byte STATE_DOCTYPE                 = 50;
  private final static byte STATE_DOCTYPE_BRACKET         = 51;
  private final static byte STATE_DOCTYPE_BRACKET_BRACKET = 52;
  private final static byte STATE_PI                      = 60;
  private final static byte STATE_PI_END                  = 61;
  private final static byte STATE_SEEN_AMP                = 70;
  private final static byte STATE_ENTITY_REF              = 71;
  private final static byte STATE_CHAR_REF                = 75;
  private final static byte STATE_CHAR_REF_DIGITS         = 76;
  private final static byte STATE_SCAN_ETAG_NAME          = 110;  
  private final static byte STATE_SCAN_STAG_NAME          = 120;  
  private final static byte STATE_SCAN_STAG_GT            = 121;  
  private final static byte STATE_SCAN_ATTR_NAME          = 122;  
  private final static byte STATE_SCAN_ATTR_EQ            = 123;  
  private final static byte STATE_SCAN_ATTR_VALUE         = 124;  
  private final static byte STATE_SCAN_ATTR_VALUE_CONTINUE= 125;  
  private final static byte STATE_SCAN_ATTR_VALUE_END     = 126;  
  private final static byte STATE_ATTR_VALUE_CONTENT      = 127;  
  
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
