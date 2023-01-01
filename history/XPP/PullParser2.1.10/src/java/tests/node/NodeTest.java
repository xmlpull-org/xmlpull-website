/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: NodeTest.java,v 1.5 2003/04/06 00:04:02 aslom Exp $
 */
package node;

import java.io.*;
import java.util.Enumeration;

import junit.framework.*;

import org.gjt.xpp.*;

/**
 * Some tests to implementation of XML Pull Parser (xpp).
 *
 */
public class NodeTest extends TestCase {

    private XmlPullParserFactory factory;

    private static final String HEADER_STR = "####";
    private static final String TRAILER_STR = "%%%%%YYY";
    private static final String XML_STR = HEADER_STR +
        "<?xml version=\"1.0\"?>\n"+
        "<m:PlaceOrder xmlns:m='Some-Namespace-URI'"+
        " xmlns:xsd=\"http://w3.org/ns/data\""+
        " xmlns:xsi=\"http://w3.org/ns/instances\""+
        ">\n"+
        " <DaysToDelivery xsd:type=\"xsi:integer\">7</DaysToDelivery>\n"+
        " <tree><subtree1><a1>aaa</a1><a2><a3>X</a3></a2></subtree1><subtree2/>"+
        " </tree>"+
        " <empty/>\n"+
        " <top xmlns=\"http://my.top.com\">\n"+
        "   <c:codebase xsi:type= 'xsd:string' xmlns:c='http://foo'>"+
        "   </c:codebase>\n"+
        "   <codebase2 xsi:type= 'xsd:string' ></codebase2>\n"+
        //TODO DIXME when Xerces 2 is fixed
        //" <new-line>\r\r\n\n\r\n\n\n&#13;</new-line>"+
        " </top>"+
        "</m:PlaceOrder>\n"+
        ""+ TRAILER_STR;

    public NodeTest(String name) {
        super(name);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
        System.exit(0);
    }

    public static Test suite() {
        return new TestSuite(NodeTest.class);
    }

    protected void setUp() throws XmlPullParserException {
        factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.DEFAULT_PROPERTY_NAME) );
        factory.setNamespaceAware(true);
    }

    protected void tearDown() {
    }

    private final static String CHILD1_NAME = "hello";
    private final static String CHILD1_CONTENT = "some <>& content";
    private final static String CHILD1_ATTR1_NAME = "attr";
    private final static String CHILD1_ATTR1_VALUE = "foo&bar";
    private final static String CHILD2 = "Hello";
    private final static String CHILD3_URI = "ssURI";
    private final static String CHILD3_PREFIX = "x";
    private final static String CHILD3_NAME = "helloNS";
    private final static String CHILD3_ATTR1_NAME = "test";
    private final static String CHILD3_ATTR1_VALUE = "less<than \n\r\t";
    private final static String CHILD3_ATTR2_NAME = CHILD1_ATTR1_NAME ;
    private final static String CHILD3_ATTR2_VALUE = CHILD1_ATTR1_VALUE;

    private void checkNodeManipulating(XmlNode node)
        throws IOException, XmlPullParserException {

        // check that we have children we wanted ...
        XmlNode child1 = (XmlNode) node.getChildAt(0);
        assertEquals("", child1.getNamespaceUri());
        assertEquals(CHILD1_NAME, child1.getLocalName());
        assertEquals(CHILD1_NAME, child1.getRawName());
        assertEquals(1, child1.getAttributeCount());
        assertEquals(null, child1.getAttributeNamespaceUri(0));
        assertEquals(CHILD1_ATTR1_NAME, child1.getAttributeRawName(0));
        assertEquals(CHILD1_ATTR1_VALUE, child1.getAttributeValue(0));
        assertEquals(CHILD1_ATTR1_VALUE,
                     child1.getAttributeValueFromRawName(CHILD1_ATTR1_NAME));

        assertEquals(1, child1.getChildrenCount());
        assertEquals(CHILD1_CONTENT, child1.getChildAt(0));

        XmlNode child4 = factory.newNode();
        child4.modifyTag(null, null, "foo");

        node.insertChildAt(0, child4);
        assertSame(child4, node.getChildAt(0));
        assertSame(child1, node.getChildAt(1));

        node.replaceChildAt(1, child4);
        assertSame(child4, node.getChildAt(0));
        node.removeChildAt(0);
        node.insertChildAt(1, child1);
        node.removeChildAt(0);

        // input node may be pull so do first some manips
        //    before caling getChildrenCount that forces to parse fully...
        assertEquals(3, node.getChildrenCount());

        String child2 = (String) node.getChildAt(1);
        assertEquals(CHILD2, child2);
        XmlNode child3 = (XmlNode) node.getChildAt(2);
        assertEquals(CHILD3_URI, child3.getNamespaceUri());
        assertEquals(CHILD3_NAME, child3.getLocalName());
        assertEquals(CHILD3_PREFIX+':'+CHILD3_NAME, child3.getRawName());
        assertEquals(2, child3.getAttributeCount());
        assertEquals(CHILD3_URI, child3.getAttributeNamespaceUri(0));
        assertEquals(CHILD3_ATTR1_NAME, child3.getAttributeLocalName(0));
        assertEquals(CHILD3_ATTR1_VALUE, child3.getAttributeValue(0));
        assertEquals(
            CHILD3_ATTR1_VALUE,
            child3.getAttributeValueFromName(CHILD3_URI, CHILD3_ATTR1_NAME));
        assertEquals(null, child3.getAttributeNamespaceUri(1));
        assertEquals(CHILD3_ATTR2_NAME, child3.getAttributeRawName(1));
        assertEquals(CHILD3_ATTR2_VALUE, child3.getAttributeValue(1));
        assertEquals(CHILD3_ATTR2_VALUE,
                     child1.getAttributeValueFromRawName(CHILD3_ATTR2_NAME));

        //System.err.println("child3="+child3);
        assertEquals(0, child3.getChildrenCount());
        //System.err.println("child3="+child3);

        // now let check removing
        node.removeChildAt(0);
        assertEquals(2, node.getChildrenCount());
        assertSame(child2, node.getChildAt(0));
        assertSame(child3, node.getChildAt(1));
        node.removeChildAt(0);
        assertEquals(1, node.getChildrenCount());
        assertSame(child3, node.getChildAt(0));
    }

    /** Chekc how new node create/add/remove methods work */
    public void testNodeManipulating()
        throws IOException, XmlPullParserException {
        // node creation
        XmlNode node = factory.newNode();
        node.modifyTag(null, null, "top");

        node.appendChild(CHILD2);

        XmlNode child1 = factory.newNode();
        child1.modifyTag(null, null, CHILD1_NAME);
        child1.appendChild(CHILD1_CONTENT);
        node.insertChildAt(0, child1);
        child1.addAttribute(
            null, null, CHILD1_ATTR1_NAME, CHILD1_ATTR1_VALUE);

        XmlNode child3 = factory.newNode();
        child3.modifyTag(
            CHILD3_URI, CHILD3_NAME, CHILD3_PREFIX+':'+CHILD3_NAME);
        //child3.addAttribute(
        //  null, CHILD3_PREFIX, "xmlns:"+CHILD3_PREFIX, CHILD3_URI);
        child3.addDeclaredNamespaces(
            new String[]{CHILD3_PREFIX}, 0, 1,
            new String[]{CHILD3_URI}
        );
        child3.addAttribute(
            CHILD3_URI, CHILD3_ATTR1_NAME, CHILD3_PREFIX+':'+CHILD3_ATTR1_NAME,
            CHILD3_ATTR1_VALUE);
        child3.addAttribute(
            null, null, CHILD3_ATTR2_NAME, CHILD3_ATTR2_VALUE);
        //child3.appendChild("");
        node.appendChild(child3);

        // persist node into string
        XmlFormatter frmtr = factory.newFormatter();
        StringWriter sw = new StringWriter();
        frmtr.setOutput(sw);
        frmtr.writeNode(node);
        sw.close();
        String xmlString = sw.toString();
        //System.err.println("xmlString="+xmlString);

        // now when node is persited we can check manipulation
        checkNodeManipulating(node);

        XmlPullParser pp = factory.newPullParser();
        pp.setInput(new StringReader(xmlString));
        pp.next();
        XmlPullNode pullNode = factory.newPullNode(pp);
        //sw=new StringWriter();frmtr.setOutput(sw); frmtr.write(pullNode);System.err.println("pull="+sw);

        checkNodeManipulating(pullNode);

        char xmlChars[] = xmlString.toCharArray();
        pp.setInput(xmlChars);
        pp.next();
        pullNode = factory.newPullNode(pp);
        checkNodeManipulating(pullNode);
    }

    private static String printable(char ch) {
        if(ch == '\n') {
            return "\\n";
        } else if(ch == '\r') {
            return "\\r";
        } else if(ch == '\t') {
            return "\\t";
        }
        return ""+ch;
    }

    private static String printable(String s) {
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

    private String checkNodeOutput(XmlPullParser pp, XmlNode node)
        throws IOException, XmlPullParserException  {
        StringWriter out = new StringWriter();
        XmlFormatter recorder = factory.newFormatter();
        if(pp.isAllowedMixedContent() == false) {
            recorder.setEndTagNewLine(true);
        }
        recorder.setOutput(out);
        recorder.writeNode(node);
        String outStr = out.toString();
        //System.err.println("outStr=---\n"+outStr+"---");

        XmlNode node2 = factory.newNode();
        pp.setInput(new StringReader(outStr));
        //pp.setAllowedMixedContent(false);
        assertEquals(XmlPullParser.START_TAG, pp.next());
        pp.readNode(node2);
        //System.err.println("pp.isAllowedMixedContent()="+pp.isAllowedMixedContent());

        StringWriter out2 = new StringWriter();
        recorder.setOutput(out2);
        recorder.writeNode(node2);

        String outStr2 = out2.toString();
        //System.err.println("outStr2=---\n"+outStr2+"---");
        assertEquals(printable(outStr), printable(outStr2));
        return outStr;
    }

    public void testNode() throws IOException, XmlPullParserException {
        //pp.setInput(buf);
        char[] buf = XML_STR.toCharArray();
        XmlPullParser pp = factory.newPullParser();
        pp.setNamespaceAware(true);
        pp.setInput(buf, HEADER_STR.length() ,
                    XML_STR.length() - HEADER_STR.length() - TRAILER_STR.length());

        //System.err.println("XML_STR="+XML_STR);

        XmlNode node = factory.newNode();
        assertEquals(XmlPullParser.START_TAG, pp.next());
        pp.readNode(node);

        checkNodeOutput(pp, node);
    }

    public void testNodeEquality() throws IOException, XmlPullParserException {
        final String XML = "<n:node xmlns:n='uri1' xmlns='uri2' n:att='fdfd'><test>hello</test></n:node>";
        XmlPullParser pp = factory.newPullParser();
        pp.setNamespaceAware(true);
        pp.setInput(new StringReader(XML));
        XmlNode node1 = factory.newNode();
        pp.next();
        pp.readNode(node1);

        pp.setInput(new StringReader(XML));
        XmlNode node2 = factory.newNode();
        pp.next();
        pp.readNode(node2);

        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node1, node2);
        XmlNode child = (XmlNode)node2.getChildAt(0);
        child.appendChild("something else");

        assertTrue(node1.equals(node2) == false);
    }

    public void testPullNode() throws IOException, XmlPullParserException {
        // check working of tree node wit no mixed content
        final String NO_MIX_STR =
            "<t1>"
            +"<child1></child1>"
            +"<child2/>"
            +"<child3>Hello&lt;</child3>"
            +"<child4><inner1/></child4>"
            +"<child5><inner2>Inner Tag!</inner2></child5>"
            +"</t1>";

        char[] buf = ("<top>"+NO_MIX_STR+"</top>").toCharArray();

        XmlPullParser pp = factory.newPullParser();
        pp.setNamespaceAware(false);
        pp.setAllowedMixedContent(false);
        pp.setInput(new CharArrayReader(buf));

        assertEquals(XmlPullParser.START_TAG, pp.next());

        XmlPullNode node = factory.newPullNode(pp);

        XmlPullNode t1 = (XmlPullNode) node.readNextChild();
        assertNotNull(t1);
        assertEquals("t1", t1.getLocalName());
        assertEquals("", t1.getNamespaceUri());
        assertEquals(null, t1.getDefaultNamespaceUri());

        XmlPullNode child1 = (XmlPullNode) t1.readNextChild();
        assertNotNull(child1);
        assertEquals(XmlPullParser.START_TAG, pp.getEventType());
        XmlStartTag stag = factory.newStartTag();
        pp.readStartTag(stag);
        assertEquals("child1", stag.getLocalName());
        String uri = stag.getNamespaceUri();
        assertEquals("", uri);

        XmlPullNode child2 = (XmlPullNode) t1.readNextChild();
        XmlPullNode child3 = (XmlPullNode) t1.readNextChild();
        assertEquals(XmlPullParser.START_TAG, pp.getEventType());
        pp.readStartTag(stag);
        assertEquals("child3", stag.getLocalName());
        child3.skipChildren();
        XmlPullNode child4 = (XmlPullNode) t1.readNextChild();
        XmlPullNode child5 = (XmlPullNode) t1.readNextChild();
        assertNull(t1.readNextChild());
        node.skipChildren();
        assertEquals(XmlPullParser.END_TAG, pp.getEventType());
        assertEquals(XmlPullParser.END_DOCUMENT, pp.next());

        String outStr = checkNodeOutput(pp, node);

        // just to double check that pull node behave like regular node
        pp.setInput(new CharArrayReader(buf));
        assertEquals(XmlPullParser.START_TAG, pp.next());
        node = factory.newPullNode(pp);
        t1 = (XmlPullNode) node.getChildAt(0);
        child3 = (XmlPullNode) t1.getChildAt(2);

        // simulate skipChildren with readChildren and removing children...
        t1.readChildren();
        assertTrue(child3.isFinished());
        child3.removeChildren();
        String outStr2 = checkNodeOutput(pp, node);
        assertEquals(outStr, outStr2);

        // check working of tree node with mixed content
        final String MIX_STR =
            "<t2 xmlns='URI2' xmlns:x='X'>Hello! <ns:child xmlns:ns='URI3'>Hello&lt;</ns:child></t2>";
        buf = ("<top>"+NO_MIX_STR+MIX_STR+"</top>").toCharArray();
        pp.setInput(buf);

        pp.setNamespaceAware(true);
        pp.setAllowedMixedContent(true);

        assertEquals(XmlPullParser.START_TAG, pp.next());
        node = factory.newPullNode(pp);

        t1 = (XmlPullNode) node.getChildAt(0); //node.readNextChild();
        assertTrue(! t1.isFinished());
        assertEquals("t1", t1.getLocalName());
        assertEquals("", t1.getNamespaceUri());
        assertEquals("", t1.getDefaultNamespaceUri());

        // check enumeration stuff...
        Enumeration enum = t1.children();
        assertEquals(0, t1.getChildrenCountSoFar());
        //child1, child2...
        assertTrue(enum.hasMoreElements());
        child1 = (XmlPullNode) enum.nextElement();
        assertEquals("child1", child1.getLocalName());
        assertNotNull(enum.nextElement());
        child3 = (XmlPullNode) enum.nextElement();
        assertEquals("child3", child3.getLocalName());
        assertNotNull(enum.nextElement());
        assertEquals(4, t1.getChildrenCountSoFar());
        assertEquals(true, enum.hasMoreElements());
        assertNotNull(enum.nextElement());
        assertEquals(false, enum.hasMoreElements());

        XmlPullNode t2 = (XmlPullNode) node.getChildAt(1);
        assertEquals("t2", t2.getLocalName());
        assertEquals("URI2", t2.getNamespaceUri());
        assertEquals("URI2", t2.getDefaultNamespaceUri());
        //      System.err.println("t2 node="+t2);
        assertEquals(1, t2.getDeclaredNamespaceLength());
        assertTrue(! t2.isFinished());
        assertTrue(t1.isFinished());
        assertEquals("Hello! ", t2.readNextChild());

        XmlPullNode nsChild = (XmlPullNode) t2.readNextChild();
        assertEquals("child", nsChild.getLocalName());
        assertEquals("URI3", nsChild.getNamespaceUri());
        assertEquals("URI2", nsChild.getDefaultNamespaceUri());

        // just check namespace handling...
        assertEquals("URI3", nsChild.getQNameUri("ns:y"));
        assertEquals("X", nsChild.prefix2Namespace("x"));
        assertEquals("X", nsChild.getQNameUri("x:y"));
        assertEquals("URI2", nsChild.getQNameUri(""));


        assertEquals(null, t1.readNextChild());

        outStr = checkNodeOutput(pp, node);
        // just to double check that pull node behave like regular node
        pp.setInput(new CharArrayReader(buf));
        assertEquals(XmlPullParser.START_TAG, pp.next());
        node = factory.newPullNode(pp);
        outStr2 = checkNodeOutput(pp, node);
        assertEquals(outStr, outStr2);


        // advanced: check that recursive pull/tree parsing is working

        pp.setInput(buf);
        assertEquals(XmlPullParser.START_TAG, pp.next());
        node = factory.newPullNode(pp);
        t1 = (XmlPullNode) node.getChildAt(0);

        // get embedded pp to parse t1 children directly
        XmlPullParser epp = t1.getPullParser();
        assertSame(pp, epp);

        // parse rest of t1

        assertEquals(XmlPullParser.START_TAG, pp.next());
        assertEquals("child1", pp.getLocalName());
        pp.skipNode();
        assertEquals(XmlPullParser.START_TAG, pp.next());
        assertEquals("child2", pp.getLocalName());
        pp.skipNode();
        assertEquals(XmlPullParser.START_TAG, pp.next());
        assertEquals("child3", pp.getLocalName());
        pp.skipNode();

        // and now: creating embedded pull node...
        assertEquals(XmlPullParser.START_TAG, pp.next());
        child4 = factory.newPullNode(pp);
        assertEquals("child4", pp.getLocalName());

        XmlPullNode inner1 = (XmlPullNode) child4.getChildAt(0);
        assertEquals(false, inner1.isFinished());
        assertEquals(false, child4.isFinished());

        inner1.skipChildren();
        assertTrue(inner1.isFinished());
        assertEquals(false, child4.isFinished());
        child4.readChildren();
        assertTrue(inner1.isFinished());
        assertTrue(child4.isFinished());

        // and back to pp
        assertEquals(XmlPullParser.START_TAG, pp.next());
        assertEquals("child5", pp.getLocalName());
        pp.skipNode();

        assertEquals(XmlPullParser.END_TAG, pp.next());

        // timing is important -- when embedding previous part MUST be parserd!!!
        t2 = (XmlPullNode) node.getChildAt(1);
        assertEquals("t2", t2.getLocalName());
        t2.readChildren();
        node.skipChildren();

        assertEquals(XmlPullParser.END_TAG, pp.getEventType());
        assertEquals(XmlPullParser.END_DOCUMENT, pp.next());
    }


    public void testPullNodeWithXpp() throws IOException, XmlPullParserException {
        // check working of tree node wit no mixed content
        final String NO_MIX_STR =
            "<t1>"
            +"<child1></child1>"
            +"<child2/>"
            +"</t1>"
            +"<t5><inner2>Inner Tag!</inner2></t5>"
            +"<t6/>"
            ;


        char[] buf = ("<top>"+NO_MIX_STR+"</top>").toCharArray();

        XmlPullParser pp = factory.newPullParser();
        pp.setNamespaceAware(false);
        pp.setAllowedMixedContent(false);
        pp.setInput(new CharArrayReader(buf));

        assertEquals(XmlPullParser.START_TAG, pp.next());

        XmlPullNode top = factory.newPullNode(pp);

        XmlPullNode t1 = (XmlPullNode) top.readNextChild();
        assertNotNull(t1);
        assertEquals("t1", t1.getLocalName());
        assertEquals("", t1.getNamespaceUri());
        assertEquals(null, t1.getDefaultNamespaceUri());

        XmlPullNode child1 = (XmlPullNode) t1.readNextChild();
        assertNotNull(child1);
        XmlStartTag stag = factory.newStartTag();
        pp.readStartTag(stag);
        assertEquals("child1", stag.getLocalName());
        String uri = stag.getNamespaceUri();
        assertEquals("", uri);

        XmlPullParser embed = t1.getPullParser();
        assertSame(pp, embed);
        // this is tricky part - we actually requested pp from parent node
        // it must make child1 finished and pp must be now on </child1>
        XmlEndTag etag = factory.newEndTag();
        pp.readEndTag(etag);
        assertEquals("child1", etag.getLocalName());
        assertTrue(child1.isFinished());

        // now finish parsing child1 and parent can correctly continue

        while(true) {
            byte state = pp.next();
            if(state == XmlPullParser.END_TAG) break;
            assertEquals(XmlPullParser.START_TAG, state);
            pp.skipNode();
        }
        XmlPullNode t5 = (XmlPullNode) top.readNextChild();
        assertEquals("t5", t5.getLocalName());
        assertEquals("", t5.getNamespaceUri());
        assertEquals(null, t5.getDefaultNamespaceUri());

        XmlPullNode inner2 = (XmlPullNode) t5.readNextChild();
        pp.readStartTag(stag);
        assertEquals("inner2", stag.getLocalName());


        // the sam trick as before but here we call no readNext child on parent
        //  but pull node is deep down just on inner2 child!!!
        XmlPullNode t6 = (XmlPullNode) top.readNextChild();
        assertTrue(t5.isFinished());
        assertEquals("t6", t6.getLocalName());
        assertEquals("", t6.getNamespaceUri());
        assertEquals(null, t6.getDefaultNamespaceUri());

        assertEquals(false, t6.isFinished());
        assertEquals(false, top.isFinished());
        assertNull(top.readNextChild());
        assertTrue(t6.isFinished());
        assertTrue(top.isFinished());
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

