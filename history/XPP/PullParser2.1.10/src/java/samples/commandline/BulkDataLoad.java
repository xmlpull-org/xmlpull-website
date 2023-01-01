/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: $
 */

package commandline;

import java.io.*;
import org.gjt.xpp.*;

public class BulkDataLoad
{
    
    public static void main(String[] args) throws Exception {
        BulkDataLoad runner = new BulkDataLoad();
        runner.run(args);
    }
    
    public void run(String[] args) throws Exception {
        
        // 1. creating instance of parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser pp = factory.newPullParser();
        
        // 2. setting options
        // ex. disabling mixed content for elements
        // (element can not have elements mixed with non-whitespace string content)
        pp.setAllowedMixedContent(false);
        
        // 3. setting input
        String data = "<test><Record1> data1 </Record1>\n<Record2> data2 </Record2>\n"+
            "<Record3> data3</doesntMatch>\n<Record4> data4 </Record4></test>";
        
        // input will be taken from java.io.Reader
        pp.setInput(new StringReader(data));
        
        // input could be also taken from String directly:
        //pp.setInput(data.toCharArray());
        
        // 4. parsing
        
        //declare variables used during parsing
        XmlStartTag stag = factory.newStartTag();
        XmlEndTag etag = factory.newEndTag();
        
        
        byte type;  // received event type
        byte prevType;  // previous event type
        
        type = prevType = pp.next();
        if(type == XmlPullParser.START_TAG) {
            pp.readStartTag(stag);
            //System.err.println("read start tag "+stag);
            if(! "test".equals(stag.getLocalName())) {
                throw new RuntimeException("bulk data must start with test not "
                                               +stag.getLocalName()+pp.getPosDesc());
            }
        } else {
            throw new RuntimeException("unexpected end of data "+pp.getPosDesc());
        }
        
        // start parsing loop
        for(;;) {
            type = pp.next();
            if(type == XmlPullParser.START_TAG) {
                pp.readStartTag(stag);
                //System.err.println("read start tag "+stag);
                type = pp.next();
                String content = "";
                if(type == XmlPullParser.CONTENT) {
                    content = pp.readContent();
                    //System.err.println("read content="+content);
                    while(type != XmlPullParser.END_TAG) {
                        try {
                            type = pp.next();
                        } catch(Exception e){
                            System.err.println("ERROR  recovering from "+e);
                            // give it a second chance
                            //type = pp.next();
                            type = pp.getEventType();
                        }
                    }
                }
                if(type != XmlPullParser.END_TAG) {
                    throw new RuntimeException("expected end tag not "+pp.getPosDesc());
                }
                System.err.println("LOAD   tag="+stag.getLocalName()+" data='"+content+"'");
            } else if(type == XmlPullParser.END_TAG) {
                break;
            } else if(type == XmlPullParser.END_DOCUMENT) {
                throw new RuntimeException("unexpected end of data "+pp.getPosDesc());
            } else {
                throw new RuntimeException("unknown event type: "+type);
            }
        }
        
        if(pp.next() != XmlPullParser.END_DOCUMENT) {
            throw new RuntimeException("expected end of data not "+pp.getPosDesc());
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

