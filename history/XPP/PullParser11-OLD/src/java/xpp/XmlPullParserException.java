//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XmlPullParserException.java,v 1.8 2001/08/15 21:37:25 aslom Exp $
 */

package xpp;

/**
 * This exception is thrown to signal any XML parsing error.
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */

public class XmlPullParserException extends Exception {
  public Throwable detail;
  private int row = -1;
  private int column = -1;

  public XmlPullParserException() {
  }

  public XmlPullParserException(String s) {
    super(s);
  }

  public XmlPullParserException(String s, int row, int column) {
    super(s);
    this.row = row;
    this.column = column;    
  }

  public XmlPullParserException(String s, Throwable ex) {
    super(s);
    detail = ex;
  }

  public Throwable getDetail() { return detail; }
  public int getLineNumber() { return row; }
  public int getColumnNumber() { return column; }

  public String getMessage() {
    if(detail == null)
      return super.getMessage();
    else
      return super.getMessage() + "; nested exception is: \n\t" 
        + detail.toString();
  }

  public void printStackTrace(java.io.PrintStream ps) {
    if (detail == null) {
        super.printStackTrace(ps);
    } else {
        synchronized(ps) {
          //ps.println(this);
          ps.println(super.getMessage() + "; nested exception is:");          
          detail.printStackTrace(ps);
        }
    }
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(java.io.PrintWriter pw){
    if (detail == null) {
        super.printStackTrace(pw);
    } else {
      synchronized(pw) {
        //pw.println(this);
        pw.println(super.getMessage() + "; nested exception is:");
        detail.printStackTrace(pw);
      }
    }
  }

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
