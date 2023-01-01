/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XppCount.java,v 1.6 2003/04/06 00:04:01 aslom Exp $
 */

package shared.count;

import java.io.Reader;
import java.io.IOException;

import org.gjt.xpp.*;

public class XppCount
{
    // made public for simplicity ...
    public String factoryClassName;
    public boolean supportNamespaces;
    public boolean trace;
    public boolean disallowMixedContent;
    public int speedTest;
    public boolean nowait;
    public int softLimit = -1;
    public int hardLimit = -1;
    
    //parsing statistics
    public int attrCount;
    public int characterCount;
    public int elementCount;
    
    private XmlPullParser pp;
    private XmlStartTag stag;
    private XmlEndTag etag;
    
    private void setup(ProgressObserver po) throws XmlPullParserException {
        attrCount = characterCount = elementCount = 0;
        XmlPullParserFactory factory =
            XmlPullParserFactory.newInstance(factoryClassName);
        po.verbose("using factory "+factory.getClass());
        etag = factory.newEndTag();
        stag = factory.newStartTag();
        pp = factory.newPullParser();
        pp.setNamespaceAware(supportNamespaces);
        if(disallowMixedContent) pp.setAllowedMixedContent(false);
        
//        if(softLimit != -1) {
//            if(pp instanceof XmlPullParserBufferControl) {
//                ((XmlPullParserBufferControl)pp).setSoftLimit(softLimit);
//            } else {
//                po.verbose("could not set soft limit as this "+pp.getClass()
//                               +" does not support interface "+XmlPullParserBufferControl.class);
//            }
//        }
//        if(hardLimit != -1) {
//            if(pp instanceof XmlPullParserBufferControl) {
//                ((XmlPullParserBufferControl)pp).setHardLimit(softLimit);
//            } else {
//                po.verbose("could not set hard limit as this "+pp.getClass()
//                               +" does not support interface "+XmlPullParserBufferControl.class);
//            }
//        }
        
    }
    
    private void tearDown() {
        pp = null;
    }
    
    /**
     * Run speed test - return wall time in miliseconds
     */
    private long speedTest(
        ReaderFactory readerFactory,
        ProgressObserver po)
        throws XmlPullParserException, IOException
    {
        final int runs = speedTest;
        if(speedTest <= 0) throw new IllegalArgumentException(
                "speedTest count must be positive not "+speedTest);
        final long startMillis = System.currentTimeMillis();
        for(int i = 0; i  < runs; ++i) {
            if(po.stopRequested())
            {
                po.println("Parsing interrupted!");
                return -1;
            }
            Reader reader = readerFactory.newReader();
            pp.setInput(reader);
            byte type;
            while((type = pp.next()) != XmlPullParser.END_DOCUMENT) {
                if(type == XmlPullParser.CONTENT) {
                    //String s = pp.readContent();
                    //characterCount += s.length();
                    characterCount += pp.getContentLength();
                } else if(type == XmlPullParser.START_TAG) {
                    pp.readStartTag(stag);
                    ++elementCount;
                    attrCount += stag.getAttributeCount();
                }
            }
            reader.close();
        }
        
        final long endMillis = System.currentTimeMillis();
        final long walltimeMillis = endMillis - startMillis;
        return walltimeMillis;
    }
    
    private void prettyParse(Reader r,
                             ProgressObserver po)
        throws XmlPullParserException, IOException
    {
        pp.setInput(r);
        
        byte type;
        while((type = pp.next()) != XmlPullParser.END_DOCUMENT) {
            if(po.stopRequested())
            {
                po.println("Parsing interrupted!");
                break;
            }
            if(type == XmlPullParser.CONTENT) {
                String s = pp.readContent();
                po.println("Content={'"+escape(s)+"'}");
                characterCount += s.length();
            } else if(type == XmlPullParser.END_TAG) {
                pp.readEndTag(etag);
                po.println(""+etag);
            } else if(type == XmlPullParser.START_TAG) {
                pp.readStartTag(stag);
                po.println(""+stag);
                ++elementCount;
                attrCount += stag.getAttributeCount();
            }
        }
    }
    
    private static String escape(String s) {
        StringBuffer buf = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(c == '\n') {
                buf.append("\\n");
            } else if(c == '\r') {
                buf.append("\\r");
            } else if(c == '\t') {
                buf.append("\\t");
            } else if(c == '\\') {
                buf.append("\\");
            } else if(c == '"') {
                buf.append('"');
            } else if(c < 32) {
                buf.append("\\x"+Integer.toHexString(c));
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
    
    private static String to1000th(long v) {
        
        long vM = v % 1000;
        String sM = "" + vM;
        if(sM.length() == 1) {
            sM = "00" + sM;
        } else if(sM.length() == 2) {
            sM = "0" + sM;
        }
        
        String s = v / 1000 + "." + sM;
        return s;
    }
    
    public void runTest(String inputName,
                        ReaderFactory rf,
                        ProgressObserver po)
        throws XmlPullParserException, IOException
    {
        setup(po);
        
        if(speedTest > 0) {
            
            po.verbose("run speed test for "+inputName+" "+ speedTest+" times");
            
            long walltime = speedTest(rf, po);
            
            if(walltime > 0)
            {
                
                //long duration = walltime / speedTest;
                
                po.print( inputName + ": ");
                
                // all this pain as J2ME does not support double/float
                
                String ms = to1000th((1000 * walltime) / speedTest);
                
                String s = to1000th(walltime);
                
                po.println("parse duration " + ms + " [ms]"
                               +" total: " + s + " [s]");
            }
            
        } else {
            
            po.verbose("pretty printing parsed input "+inputName);
            
            po.verbose("================ START PARSING: "+inputName);
            
            Reader r = rf.newReader();
            prettyParse(r, po);
            
            po.verbose("================ END PARSING");
            
        }
        
        po.println("statistics: "
                       + elementCount  + " elems, "
                       + attrCount  + " attrs, "
                       + characterCount + " chars"
                  );
        
        tearDown();
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

