/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: FormatTest.java,v 1.4 2003/04/06 00:04:02 aslom Exp $
 */

package format;

import java.io.*;
import java.util.Enumeration;

import junit.framework.*;

import org.gjt.xpp.*;

/**
 * Some tests to implementation of XML Pull Parser 2 formatting/recording output API.
 *
 */
public class FormatTest extends TestCase {
    
    private int type;
    private XmlStartTag stag;
    private XmlEndTag etag;
    private XmlPullParserFactory factory;
    
    
    public FormatTest(String name) {
        super(name);
    }
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
        System.exit(0);
    }
    
    public static Test suite() {
        return new TestSuite(FormatTest.class);
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
    
    public void testAttribFormat()
        throws IOException, XmlPullParserException
    {
        final String attribEntities =
            "<m:test attrib='&lt;table' xmlns:m='URI'>";
        
        XmlPullParser pp = factory.newPullParser();
        parseOneElement(pp, attribEntities+"</m:test>", false);
        pp.readStartTag(stag);
        //System.err.println("attr="+stag.getAttributeValue(0));
        assertEquals("<table", stag.getAttributeValue(0));
        XmlFormatter frmtr = factory.newFormatter();
        StringWriter sw = new StringWriter();
        frmtr.setOutput(sw);
        frmtr.writeStartTag(stag);
        String s = sw.toString();
        //System.err.println("stag='"+s+"'");
        assertEquals(attribEntities, s);
    }
    
    private void parseOneElement(
        final XmlPullParser pp,
        final String buf,
        final boolean supportNamespaces)
        throws IOException, XmlPullParserException
    {
        //pp.setInput(buf.toCharArray());
        pp.setInput(new StringReader(buf));
        pp.setNamespaceAware(supportNamespaces);
        pp.setAllowedMixedContent(false);
        pp.next();
        pp.readStartTag(stag);
        if(supportNamespaces) {
            assertEquals("test", stag.getLocalName());
        } else {
            assertEquals("m:test", stag.getLocalName());
        }
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

