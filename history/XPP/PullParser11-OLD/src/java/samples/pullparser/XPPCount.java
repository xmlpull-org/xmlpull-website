//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: XPPCount.java,v 1.7 2001/08/15 21:37:22 aslom Exp $
 */

package samples.pullparser;
//package xpp;

import xpp.EndTag;
import xpp.StartTag;
import xpp.XmlPullParser;
import xpp.XmlPullParserException;

import java.io.*;

/**
 * Simple demonstration of XML Pull Parser  - count XML elements
 * in file passed as argument (test.xml by default)
 *
 * @version $Revision: 1.7 $ $Date: 2001/08/15 21:37:22 $ (GMT)
 * @author $Author: aslom $ 
 */

public class XPPCount {
  private static boolean verbose;

  public static void main(String args[]) throws Exception {
    boolean supportNamespaces = false;
    boolean trace = false; 
    boolean disallowMixedContent = false; 
    boolean speedTest = false;
    boolean nowait = false;     
    int softLimit = -1; 
    int hardLimit = -1; 
    String xmlFile = "test.xml";
    for(int i = 0; i < args.length; ++i) {
      String arg = args[i];
      String value = null;
      if(i < args.length - 1) value = args[i + 1];
      
      if(arg.charAt(0) == '-') {
        if(arg.startsWith("-nowait")) {
          nowait = true;
        } else if(arg.startsWith("-hard")) {
          if(value == null) usage();
          hardLimit = Integer.parseInt(value);
          ++i;        
        } else if(arg.startsWith("-soft")) {
          if(value == null) usage();
          softLimit = Integer.parseInt(value);
          ++i;        
        } else if(arg.startsWith("-n")) {
          supportNamespaces = true;
        } else if(arg.startsWith("-s")) {
          speedTest = true;
        } else if(arg.startsWith("-t")) {
          trace = true;
        } else if(arg.startsWith("-v")) {
          verbose = true;
        } else if(arg.startsWith("-x")) {
          disallowMixedContent = true;
        } else
          usage();
      } else {
        xmlFile = arg;
      }
    }
    //System.out.println("Hello in "+new File("").getAbsolutePath());
    //String data = SXTCount.loadData(SXTCount.class, xmlFile);

    if(verbose) System.out.println("starting");
    
    XmlPullParser xpp = new XmlPullParser();
    xpp.setMixedContent(!disallowMixedContent);
    xpp.setSupportNamespaces(supportNamespaces);
    xpp.setSoftLimit(softLimit);
    xpp.setHardLimit(hardLimit);
    
    int elementCount = 0;
    int attrCount = 0;
    int spaceCount = 0;
    int characterCount = 0;


    byte type;
    StartTag stag = new StartTag();
    EndTag etag = new EndTag();    
    
    
    double duration = -1;
    double walltime = -1;

    if(trace) {
      System.err.println("================ START OF INPUT("+xmlFile+")");
      //System.err.println(SXTCount.loadData(SXTCount.class, xmlFile));
      System.err.println("================ END OF INPUT");

      //sxt.setInput(data.toCharArray());
      xpp.setInput(
        new InputStreamReader(SXTCount.openData(SXTCount.class, xmlFile)));  //new StringReader(data));

      System.err.println("================ START PARSING");

      //xpp.setInput(data.toCharArray());
      //xpp.setInput(new StringReader(data));

      while((type = xpp.next()) != XmlPullParser.END_DOCUMENT) {
        if(type == XmlPullParser.CONTENT) {
          String s = xpp.readContent();
          System.err.println("CONTENT={'"+s+"'}");  
        } else if(type == XmlPullParser.END_TAG) {
          xpp.readEndTag(etag);
          System.err.println(""+etag);  
        } else if(type == XmlPullParser.START_TAG) {
          xpp.readStartTag(stag);
          System.err.println(""+stag);  
        }        
      }
      
      System.err.println("================ END PARSING");

    } //else {

	     
    final int runs;
    if(speedTest) {
      if(verbose) System.out.println("reading input to find input size");
      Reader r = new InputStreamReader(SXTCount.openData(SXTCount.class, xmlFile));
      char[] buf = new char[100 * 1024];
      int count = 0;
      int read = 0;
      while(read != -1) {
        read = r.read(buf);
        count += read;
      }
      runs =  10000000 / count + 2; //data.length() + 2;   //3; //parse 10M characters
      if(verbose) System.out.println("start speed test runs=" + runs
        +" input size="+count);
    } else {
      runs = 10;
    }

    final long startMillis = System.currentTimeMillis();

    for(int i = 0; i  < runs; ++i) {

      elementCount = 0;
      attrCount = 0;
      spaceCount = 0;
      characterCount = 0;

      //xpp.setInput(buf);
      //xpp.setInput(data.toCharArray());
      xpp.setInput(
        new InputStreamReader(
          SXTCount.openData(SXTCount.class, 
            xmlFile)));//new StringReader(data));

      //if(verbose) System.out.println("round " + i);
	     
      while((type = xpp.next()) != XmlPullParser.END_DOCUMENT) {
	if(type == XmlPullParser.CONTENT) {
         //if(verbose) System.out.println("pos " + xpp.getPosDesc());
	  String s = xpp.readContent();
          characterCount += s.length();
	} else if(type == XmlPullParser.START_TAG) {
	  xpp.readStartTag(stag);
	  ++elementCount;
	  attrCount += stag.getLength(); 
	}
      }
    }

    final long endMillis = System.currentTimeMillis();
    walltime = (double)(endMillis - startMillis);
    duration = walltime / runs;
    
    System.out.print( xmlFile + ": ");
    
    if(speedTest) System.out.print("" + duration/1000.0 + " s"
      +" total: " + walltime / 1000.0 + " s"
      );
      
    System.out.println(" ("
         + elementCount  + " elems, "
         + attrCount  + " attrs, "
         + spaceCount  + " spaces, "
         + characterCount + " chars)"
    );      
    
    if(!nowait) {
      System.out.print("press Enter to close program ... ");
      System.in.read();
    }

    /*
    // example of SOAP deserializing code
    type = xpp.next();
    if(type != XmlPullParser.START_TAG)
      throw new RuntimeException("exected start tag");
    xpp.readStartTag(stag);
    if(!stag.getLocalName().equals("Envelope")
      || !stag.getUri().equals("urn:schemas-soap"))
      throw new RuntimeException("expected Envelope");
    type = xpp.next();
    xpp.readStartTag(stag);
    type = xpp.next();
    xpp.readStartTag(stag);
    System.err.println("object target="+stag.getUri());
    System.err.println("got method="+stag.getLocalName());
    */
    

  }
  
  static void usage() {
    System.err.print( 
     "Usage\n"
    +"  XPPCount [options] <XML file>\n"
    +"\n"
    +"Options:\n"
    +"  -n\tEnable namespace processing. Defaults to off.\n"
    +"  -s\tRun speed test.\n"
    +"  -t\tPrint tracing output only. Defaults to off.\n"
    +"  -v\tVerbose output.\n"
    +"  -x\tDislallow mixed content. Defaults to off.\n"
    +"  -soft x\tSet soft limit to x  on buffer size. Defaults to -1.\n"
    +"  -hard x\tSet hard limit to x on buffer size. Defaults to -1.\n"
    +"\n"
    +"This program prints the number of elements, attributes,\n"
    +"white spaces and other non-white space characters in the input file.\n"
    +"\n"
    );
    System.exit(1);
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
