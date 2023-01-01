/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: LowLevelTest.java,v 1.7 2003/04/06 00:04:02 aslom Exp $
 */

package lowlevel;

import java.io.*;
import java.util.Enumeration;

import junit.framework.*;

import org.gjt.xpp.*;

/**
 * Some tests to implementation of XML Pull Parser (xpp).
 *
 */
public class LowLevelTest extends TestCase {

    private int type;
    private XmlStartTag stag;
    private XmlEndTag etag;
    private XmlPullParserFactory factory;

    private static final String HEADER_STR = "####";
    private static final String TRAILER_STR = "%%%%%XXX";
    private static final String XML_STR = HEADER_STR +
        "<?xml version=\"1.0\"?>\n"+
        "<m:PlaceOrder xmlns:m='Some-Namespace-URI'"+
        " xmlns:xsd=\"http://w3.org/ns/data\""+
        " xmlns:xsi=\"http://w3.org/ns/instances\""+
        ">\n"+
        "<DaysToDelivery xsd:type=\"xsi:integer\">7</DaysToDelivery>\n"+
        "<tree><subtree1><a1>aaa</a1><a2><a3>X</a3></a2></subtree1><subtree2/>"+
        "</tree>"+
        "<empty/>\n"+
        "<top xmlns=\"http://my.top.com\">\n"+
        "<c:codebase xsi:type= 'xsd:string' xmlns:c='http://foo'>"+
        " </c:codebase>\n"+
        "<codebase2 xsi:type= 'xsd:string' ></codebase2>\n"+
        "<new-line>\r\r\n\n\r\n\n\n&#13;</new-line>"+
        "</top>"+
        "</m:PlaceOrder>\n"+
        ""+ TRAILER_STR;

    public LowLevelTest(String name) {
        super(name);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
        System.exit(0);
    }

    public static Test suite() {
        return new TestSuite(LowLevelTest.class);
    }

    protected void setUp() throws XmlPullParserException {
        factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.DEFAULT_PROPERTY_NAME) );
        factory.setNamespaceAware(true);
        stag = factory.newStartTag();
        etag = factory.newEndTag();
    }

    protected void tearDown() {
    }

    public void testSimple() throws IOException, XmlPullParserException
    {
        char[] buf =
            "   <top><hello >World &amp; Universe</hello></top>\n".toCharArray();
        //   -123456789-123456789-123456789-123456789-123456789
        //System.err.println("factory: "+factory);
        XmlPullParser pp = factory.newPullParser();
        pp.setInput(new CharArrayReader(buf));

        if(pp instanceof XmlPullParserBufferControl) {
            XmlPullParserBufferControl ppbc = (XmlPullParserBufferControl) pp;
            assertEquals(ppbc.getBufferShrinkOffset(), 0);
            assertTrue(ppbc.isBufferShrinkable());
        }

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("top", stag.getLocalName());
        String uri = stag.getNamespaceUri();
        assertEquals("", uri);

        XmlPullParserEventPosition ppev = null;
        if(pp instanceof XmlPullParserBufferControl) {
            ppev = (XmlPullParserEventPosition) pp;
        }
        int xmlStart = -1;
        int off = -1;
        String tag = null;
        if(ppev != null) {
            xmlStart = ppev.getEventStart();
            off = 3;
            assertEquals(off, ppev.getEventStart());
            off += 5;
            assertEquals(off, ppev.getEventEnd());
            tag = new String(ppev.getEventBuffer(), xmlStart,  ppev.getEventEnd() - xmlStart);
            assertEquals("<top>", tag);
        }

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("hello", stag.getLocalName());
        if(ppev != null) {
            assertEquals(off, ppev.getEventStart());
            off += 8;
            assertEquals(off, ppev.getEventEnd());
        }
        assertEquals(XmlPullParser.CONTENT, pp.next());
        assertEquals("World & Universe", pp.readContent());
        if(ppev != null) {
            xmlStart = ppev.getEventStart();
            String content = new String(ppev.getEventBuffer(), xmlStart,  ppev.getEventEnd() - xmlStart);
            assertEquals("World &amp; Universe", content);
            assertEquals(off, ppev.getEventStart());
            off += 20;
            assertEquals(off, ppev.getEventEnd());
        }
        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("hello", etag.getLocalName());

        if(ppev != null) {
            xmlStart = ppev.getEventStart();
            tag = new String(ppev.getEventBuffer(), xmlStart,  ppev.getEventEnd() - xmlStart);
            assertEquals("</hello>", tag);
            assertEquals(off, ppev.getEventStart());
            off += 8;
            assertEquals(off, ppev.getEventEnd());
        }
        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("top", etag.getLocalName());

        if(ppev != null) {
            xmlStart = ppev.getEventStart();
            tag = new String(ppev.getEventBuffer(), xmlStart,  ppev.getEventEnd() - xmlStart);
            assertEquals("</top>", tag);


            assertEquals(off, ppev.getEventStart());
            off += 6;
            assertEquals(off, ppev.getEventEnd());
        }

        assertEquals(pp.next(), XmlPullParser.END_DOCUMENT);
    }

    public void testUnshrinkableLowLevel() throws IOException, XmlPullParserException
    {
        char[] buf =
            "   <top><hello >World &amp; Universe</hello><empty/><empty></empty></top>\n<!--test-->\n".toCharArray();
        XmlPullParser pp = factory.newPullParser();
        pp.setInput(new CharArrayReader(buf));

        if(pp instanceof XmlPullParserBufferControl) {
            XmlPullParserBufferControl ppbc = (XmlPullParserBufferControl) pp;
            assertEquals(ppbc.getBufferShrinkOffset(), 0);
            assertTrue(ppbc.isBufferShrinkable());

            // NOTE: this will gurantee that buffer will not be shrinked during parsing
            //  however it may be still re-allocated so keeping pointer to it is useless...
            ppbc.setBufferShrinkable(false);
            assertEquals(ppbc.isBufferShrinkable(), false);
        } else {
            fail("buffer control is not supported in "+pp.getClass());
        }

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("top", stag.getLocalName());
        String uri = stag.getNamespaceUri();
        assertEquals("", uri);

        XmlPullParserEventPosition ppev = null;
        if(pp instanceof XmlPullParserBufferControl) {
            ppev = (XmlPullParserEventPosition) pp;
        } else {
            fail("event positioning is not supported in "+pp.getClass());
        }

        int xmlStart = ppev.getEventStart();
        int off = 3;
        assertEquals(off, ppev.getEventStart());
        off += 5;
        assertEquals(off, ppev.getEventEnd());
        assertEquals("<top>", new String(ppev.getEventBuffer(),
                                         ppev.getEventStart(),
                                         ppev.getEventEnd() - ppev.getEventStart()));

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("hello", stag.getLocalName());
        assertEquals(off, ppev.getEventStart());
        off += 8;
        assertEquals(off, ppev.getEventEnd());

        assertEquals(XmlPullParser.CONTENT, pp.next());
        assertEquals("World & Universe", pp.readContent());

        assertEquals(off, ppev.getEventStart());
        off += 20;
        assertEquals(off, ppev.getEventEnd());

        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("hello", etag.getLocalName());

        assertEquals(off, ppev.getEventStart());
        off += 8;
        assertEquals(off, ppev.getEventEnd());

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("empty", stag.getLocalName());
        assertEquals(off, ppev.getEventStart());
        off += 8;
        assertEquals(off, ppev.getEventEnd());

        //    assertEquals(XmlPullParser.CONTENT, pp.next());
        //    assertEquals("", pp.readContent());
        //    off += 0;

        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("empty", etag.getLocalName());
        assertEquals(off, ppev.getEventStart());
        off += 0; // zero as it is empty element...
        assertEquals(off, ppev.getEventEnd());

        assertEquals(XmlPullParser.START_TAG, pp.next());pp.readStartTag(stag);
        assertEquals("empty", stag.getLocalName());
        assertEquals(off, ppev.getEventStart());
        off += 7;
        assertEquals(off, ppev.getEventEnd());

        //assertEquals(XmlPullParser.CONTENT, pp.next());
        //assertEquals("", pp.readContent());
        //off += 0;

        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("empty", etag.getLocalName());
        assertEquals(off, ppev.getEventStart());
        off += 8;
        assertEquals(off, ppev.getEventEnd());

        assertEquals(XmlPullParser.END_TAG, pp.next());
        pp.readEndTag(etag);
        assertEquals("top", etag.getLocalName());

        assertEquals(off, ppev.getEventStart());
        off += 6;
        assertEquals(off, ppev.getEventEnd());

        int xmlEnd = ppev.getEventEnd();
        assertEquals(buf.length - 13, xmlEnd);

        assertEquals(pp.next(), XmlPullParser.END_DOCUMENT);


        String orig = (new String(buf)).substring(3, off);
        //System.out.println("orig='"+orig+"'");
        char[] xmlBuf = ppev.getEventBuffer();
        String xml = (new String(xmlBuf)).substring(xmlStart, xmlEnd);

        assertEquals(orig, xml);

        //TODO set very small soft limit and check buffer shrinkig...

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

}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2002 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */


