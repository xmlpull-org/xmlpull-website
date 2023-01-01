//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XppTest.java,v 1.14 2001/08/15 21:37:24 aslom Exp $
 */

package tests;

import java.io.*;

import junit.framework.*;

import xpp.*;

/**
 * Some tests to implementation of XML Pull Parser (xpp).
 *
 */
public class XppTest extends TestCase {

  public XppTest(String name) {
    super(name);
  }

  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
    System.exit(0);
  }
  
  public static Test suite() {
    return new TestSuite(XppTest.class);
    //TestSuite suite= new TestSuite("All JUnit Tests for PullParser");
    //suite.addTest(XppTest.suite());
    //return suite;    
  }

  protected void setUp() {
    stag = new StartTag();
    etag = new EndTag();    
    pp = new XmlPullParser();
  }

  protected void tearDown() {
  }


  public void testXpp() throws IOException, XmlPullParserException
  {
    //if(Log.ON) l.log(Level.FINE, "starting test");

    final String buf = 
"<?xml version=\"1.0\"?>\n"+
"<m:PlaceOrder xmlns:m='Some-Namespace-URI'"+
" xmlns:xsd=\"http://w3.org/ns/data\""+
" xmlns:xsi=\"http://w3.org/ns/instances\""+
">\n"+
"<DaysToDelivery xsd:type=\"xsi:integer\">7</DaysToDelivery>\n"+
"<tree><subtree1><a1>aaa</a1><a2><a3>X</a3></a2></subtree1><subtree2/>"+
"</tree>"+
"<empty/>\n"+
"<top>\n"+
"<codebase xsi:type= 'xsd:string' > </codebase>\n"+
"<codebase2 xsi:type= 'xsd:string' ></codebase2>\n"+
"<new-line>\r\r\n\n\r\n\n\n&#13;</new-line>"+
"";

    pp.setInput(buf.toCharArray());
    pp.setMixedContent(false);


    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("PlaceOrder", stag.getLocalName());
    String uri = stag.getUri();
    assertEquals("Some-Namespace-URI", uri);

    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("DaysToDelivery", stag.getLocalName());


    // check utility functions
    String local = pp.getQNameLocal("ns:alek");
    assertEquals("alek", local);
    uri = pp.getQNameUri("ns:alek");
    assertEquals(null, uri);
    uri = pp.getQNameUri("alek");
    assertEquals("", uri);
    uri = pp.getQNameUri("m:alek");
    assertEquals("Some-Namespace-URI", uri);

    assertEquals(XmlPullParser.CONTENT, pp.next());
    assertEquals("7", pp.readContent());

    assertEquals(pp.next(), XmlPullParser.END_TAG);
    pp.readEndTag(etag);
    assertEquals("DaysToDelivery", etag.getLocalName());

    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("tree", stag.getLocalName());
    pp.skipSubTree();
    pp.readEndTag(etag);
    assertEquals("tree", etag.getLocalName());

    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("empty", stag.getLocalName());
    pp.skipSubTree();
    pp.readEndTag(etag);
    assertEquals("empty", etag.getLocalName());

    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("top", stag.getLocalName());
 
    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("codebase", stag.getLocalName());
    assertEquals(XmlPullParser.CONTENT, pp.next());
    assertEquals(" ", pp.readContent());
    assertEquals(pp.next(), XmlPullParser.END_TAG);
    pp.readEndTag(etag);
    assertEquals("codebase", etag.getLocalName());

    assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
    assertEquals("codebase2", stag.getLocalName());
    assertEquals(XmlPullParser.CONTENT, pp.next());
    assertEquals("", pp.readContent());
    assertEquals(pp.next(), XmlPullParser.END_TAG);
    pp.readEndTag(etag);
    assertEquals("codebase2", etag.getLocalName());
  }


  public void testAttribUniq() throws IOException, XmlPullParserException
  {
    Exception ex;

    final String attribsOk = 
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"+
"a='a' b='b' m:a='a' n:b='b' n:x='a'"+
"/>\n"+
"";

    final String duplicateAttribs =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"+
"a='a' b='b' m:a='a' n:b='b' a='x'"+
"/>\n"+
"";

    final String duplicateNsAttribs =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"+
"a='a' b='b' m:a='a' n:b='b' n:a='a'"+
"/>\n"+
"";

    final String duplicateXmlns =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:m='Some-Namespace-URI'"+
""+
"/>\n"+
"";

    final String duplicateAttribXmlnsDefault =
"<m:test xmlns='Some-Namespace-URI' xmlns:m='Some-Namespace-URI'"+
"a='a' b='b' m:b='b' m:a='x'"+
"/>\n"+
"";

    parseOneElement(attribsOk, false);      
    parseOneElement(attribsOk, true);      
    parseOneElement(duplicateNsAttribs, false);      
    parseOneElement(duplicateAttribXmlnsDefault, false);
    parseOneElement(duplicateAttribXmlnsDefault, true);

    ex = null;
    try {
      parseOneElement(duplicateNsAttribs, true);          
    } catch(XmlPullParserException rex) {
      ex = rex;
    }  
    assert(ex != null);


    ex = null;
    try {
      parseOneElement(duplicateAttribs, true);          
    } catch(XmlPullParserException rex) {
      ex = rex;
    }  
    assert(ex != null);

    ex = null;
    try {
      parseOneElement(duplicateAttribs, false);          
    } catch(XmlPullParserException rex) {
      ex = rex;
    }  
    assert(ex != null);

    ex = null;
    try {
      parseOneElement(duplicateXmlns, false);          
    } catch(XmlPullParserException rex) {
      ex = rex;
    }  
    assert(ex != null);

    ex = null;
    try {
      parseOneElement(duplicateXmlns, true);          
    } catch(XmlPullParserException rex) {
      ex = rex;
    }  
    assert(ex != null);
    //ex.printStackTrace();
   
  }

  public void testNormalizeLine() 
    throws IOException, XmlPullParserException
  {

    final String firstR = 
"\r \r\n \n\r \n\n \r\n\r \r\r \r\n\n \n\r\r\n\r"+
"";  

    final String expectedN = 
"\n \n \n\n \n\n \n\n \n\n \n\n \n\n\n\n";

    final String normalizedN = 
"                       ";


    final String manyLineBreaks = 
"fifi\r&amp;\r&amp;\r\n foo &amp;\r bar \n\r\n&quot;"+
firstR;

    final String manyExpected = 
"fifi\n&\n&\n foo &\n bar \n\n\""+
expectedN;
//"\r \r\n \n\r \n\n \r\n\r \r\r \r\n\n \n\r\r\n\r";

    final String manyNormalized = 
"fifi & &  foo &  bar   \""+
normalizedN;

    final String tagR = 
"<m:test xmlns:m='Some-Namespace-URI'>"+
firstR+
"</m:test>\r\n";

    final String attrR = 
"<m:test xmlns:m='Some-Namespace-URI' fifi='"+firstR+"'/>";

    final String manyTag = 
"<m:test xmlns:m='Some-Namespace-URI'>"+
manyLineBreaks+
"</m:test>\r\n";

    final String manyAttr = 
"<m:test xmlns:m='Some-Namespace-URI' fifi='"+manyLineBreaks+"'/>";


    parseOneElement(tagR, true);      

    assertEquals(XmlPullParser.CONTENT, pp.next());
    assertEquals(printable(expectedN), printable(pp.readContent()));

    parseOneElement(attrR, true);      
    pp.readStartTag(stag);
    String attrVal = stag.getValue("fifi");
    //System.err.println("attrNormalized.len="+normalizedN.length());
    //System.err.println("attrVal.len="+attrVal.length());
    assertEquals(printable(normalizedN), printable(attrVal));
        
    parseOneElement(manyTag, true);      

    assertEquals(XmlPullParser.CONTENT, pp.next());
    assertEquals(manyExpected, pp.readContent());

    assertEquals(pp.next(), XmlPullParser.END_TAG);
    pp.readEndTag(etag);
    assertEquals("test", etag.getLocalName());

    // having \r\n as last characters is the hardest case
    //assertEquals(XmlPullParser.CONTENT, pp.next());
    //assertEquals("\n", pp.readContent());

    assertEquals(pp.next(), XmlPullParser.END_DOCUMENT);

    parseOneElement(manyAttr, true);      
    pp.readStartTag(stag);
    attrVal = stag.getValue("fifi");
    assertEquals(printable(manyNormalized), printable(attrVal));
    
  }

  private void parseOneElement(final String buf, 
    final boolean supportNamespaces) 
    throws IOException, XmlPullParserException
  {
    //pp.setInput(buf.toCharArray());
    pp.setInput(new StringReader(buf));
    pp.setSupportNamespaces(supportNamespaces);
    pp.setMixedContent(false);   
    pp.next();
    pp.readStartTag(stag);
    if(supportNamespaces) {
      assertEquals("test", stag.getLocalName());
    } else {
      assertEquals("m:test", stag.getLocalName());
    }
  }

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


  private int type; 
  private StartTag stag;
  private EndTag etag;    
  private XmlPullParser pp;
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
