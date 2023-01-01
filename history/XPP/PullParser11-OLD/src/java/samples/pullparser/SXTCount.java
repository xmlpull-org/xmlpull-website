//-------------------------74-columns-wide-------------------------------|
/*
 * Copyright (c) 2001 Extreme! Lab, Indiana University. All rights
 * reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 *
 * $Id: SXTCount.java,v 1.6 2001/08/15 21:37:22 aslom Exp $
 */

package samples.pullparser;

import sxt.XmlTokenizer;
import sxt.XmlTokenizerException;

import java.io.*;

/**
 * Simple demonstration of XML tokenizer - count all tokens
 * in file passed as argument (test.xml by default)
 *
 * @version $Revision: 1.6 $ $Date: 2001/08/15 21:37:22 $ (GMT)
 * @author $Author: aslom $ 
 */
public class SXTCount {
  private  static boolean verbose;

  public static void main(String args[]) throws Exception {
    boolean allTokens = false;
    boolean trace = false; 
    boolean disallowMixedContent = false; 
    boolean speedTest = false;
    boolean nowait = false;  
    String xmlFile = "test.xml";
    for(int i = 0; i < args.length; ++i) {
      String arg = args[i];
      if(arg.charAt(0) == '-') {
        if(arg.startsWith("-a")) {
          allTokens = true;
        } else if(arg.startsWith("-nowait")) {
          nowait = true;
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
    String data = loadData(SXTCount.class, xmlFile);

    XmlTokenizer sxt = new XmlTokenizer();
    sxt.setMixedContent(!disallowMixedContent);
    sxt.setNotifyAll(allTokens);
    sxt.setParseContent(true);

    byte token;

    int elementCount = 0;
    int attrCount = 0;
    int spaceCount = 0;
    int characterCount = 0;

    double duration = -1;
    double walltime = -1;


    if(trace) {
      System.err.println("================ START OF INPUT("+xmlFile+")");
      System.err.println(data);
      System.err.println("================ END OF INPUT");

      //sxt.setInput(data.toCharArray());
      sxt.setInput(new StringReader(data));

      System.err.println("================ START TOKENIZING");
      
      String name;
      
      while((token = sxt.next()) != XmlTokenizer.END_DOCUMENT) {
        switch(token) {
        case XmlTokenizer.CHARACTERS:
          name = "characters";
          break;  
        case XmlTokenizer.CHAR_REF:
          name = "char_ref";
          break;  
        case XmlTokenizer.ENTITY_REF:
          name = "entity";
          break;  
        case XmlTokenizer.CDSECT:
          name = "CDATA";
          break;  
        case XmlTokenizer.COMMENT:
          name = "comment";
          break;  
        case XmlTokenizer.CONTENT:
          name = "content";
          break;  
        case XmlTokenizer.DOCTYPE:
          name = "DOCTYPE";
          break;  
        case XmlTokenizer.PI:
          name = "PI";
          break;  
        case XmlTokenizer.STAG_NAME:
          name = "STag name";
          break;  
        case XmlTokenizer.ETAG_NAME:
          name = "ETag name";
          break;  
        case XmlTokenizer.EMPTY_ELEMENT:
          name = "Empty element";
          break;  
        case XmlTokenizer.STAG_END:
          name = "STag end";
          break;  
        case XmlTokenizer.ATTR_NAME:
          name = "Attr name";
          break;  
        case XmlTokenizer.ATTR_CHARACTERS:
          name = "Attr characters";
          break;  
        case XmlTokenizer.ATTR_CONTENT:
          name = "Attr content";
          break;  
        default:
          name = "unknown token "+token;
        }  
        String s = new String(sxt.buf, 
          sxt.posStart, sxt.posEnd - sxt.posStart);
        if(sxt.parsedContent)
          s = new String(sxt.pc, sxt.pcStart, sxt.pcEnd - sxt.pcStart);
        String prefix = "";
        if(token == XmlTokenizer.STAG_NAME 
          || token == XmlTokenizer.ETAG_NAME 
          || token == XmlTokenizer.ATTR_NAME   
          ) 
        {
          if(sxt.posNsColon > 0)
            prefix = " prefix="
              +s.substring(0, sxt.posNsColon - sxt.posStart);
        }  
        System.err.println(name+"='"+s+"'"+prefix);
      }
      System.err.println("================ END TOKENIZING");
    } //else {
     
	     
    final int runs;
    if(speedTest) {
      runs =  10000000 / data.length() + 2;   //3; //parse 10M characters
      if(verbose) System.out.println("start speed test runs=" + runs);
    } else {
      runs = 10;
    }
 	  
    final long startMillis = System.currentTimeMillis();

    for(int i = 0; i  < runs; ++i) {

      elementCount = 0;
      attrCount = 0;
      spaceCount = 0;
      characterCount = 0;

      //sxt.setInput(buf);
      //sxt.setInput(data.toCharArray());
      sxt.setInput(new StringReader(data));
	     
      while((token = sxt.next()) != XmlTokenizer.END_DOCUMENT) {
        switch(token) {
	case XmlTokenizer.CONTENT:
          if(sxt.parsedContent)
            characterCount += sxt.pcEnd - sxt.pcStart;
          else 
            characterCount += sxt.posEnd - sxt.posStart;
          break;
        case XmlTokenizer.STAG_NAME:
          ++elementCount;
          break;
        case XmlTokenizer.ATTR_NAME:  
          ++attrCount;
        }        
      }

    }
    final long endMillis = System.currentTimeMillis();
    walltime = (double)(endMillis - startMillis);
    duration = walltime / runs;
    
    System.out.print( xmlFile + ": ");
    
    if(speedTest) System.out.print("" + duration + " ms"
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
    
  }

  private final static String RES_PREFIX = 
    "/src/java/samples/pullparser/";
  
  public static InputStream openData(Class klass, String name)
    throws IOException 
  {
    InputStream in = null;
    try {
      in = new FileInputStream(name);
    } catch(IOException ex) {
      in = klass.getResourceAsStream( name );
      if(in  == null) {
        in = klass.getResourceAsStream(RES_PREFIX +  name );
      }

    }
    return in;    
  }

  public static String loadData(Class klass, String name) throws IOException {
    InputStream in = openData(klass, name);
    if(in == null) {
     throw new IOException("can't load "+name);
    }
    in = new BufferedInputStream(in);
    StringWriter sink = new StringWriter();
    int i;
    while((i = in.read()) != -1) {
      sink.write((char)i);
    }
    return sink.toString();
  }

  static void usage() {
    System.err.print( 
       "Usage\n"
      +"  SXTCount [options] <XML file>\n"
      +"\n"
      +"Options:\n"
      +"  -a\tEnable processing of all tokens. Defaults to off.\n"
      +"  -s\tRun speed test.\n"
      +"  -t\tPrint tracing output only. Defaults to off.\n"
      +"  -v\tVerbose output.\n"
      +"  -x\tDislallow mixed content. Defaults to off.\n"
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
