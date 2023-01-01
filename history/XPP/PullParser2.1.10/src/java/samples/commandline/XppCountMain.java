/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XppCountMain.java,v 1.4 2003/04/06 00:04:01 aslom Exp $
 */

package commandline;

import java.io.*;

import shared.count.ProgressObserver;
import shared.count.ReaderFactory;
import shared.count.XppCount;

public class XppCountMain
    implements ProgressObserver, ReaderFactory
{
    // when enabled do not use whole file buffering
    private boolean hugeFiles;
    // file content read into string
    private String fileContent;
    private boolean trace;
    private boolean verbose;
    private boolean nowait;
    private String xmlFile = "test.xml";
    private String charsetName;
    
    public void print(String msg) { System.out.print(msg); }
    public void println(String msg) { System.out.println(msg); }
    public void trace(String msg) { if(trace) System.out.println(msg); }
    public void verbose(String msg) { if(verbose) System.out.println(msg); }
    public boolean stopRequested() { return false; }
    
    public Reader newReader() throws IOException
    {
        Reader r;
        if(hugeFiles) {
            trace("opening stream reader for file "+xmlFile);
            if(charsetName != null) {
                r = new InputStreamReader(openData(this.getClass(), xmlFile), charsetName);
            } else {
                r = new InputStreamReader(openData(this.getClass(), xmlFile));
            }
        } else {
            if(fileContent == null) {
                trace("caching file content of "+xmlFile);
                fileContent = loadData(this.getClass(), xmlFile, charsetName);
            }
            trace("returning stream reader for cached "+xmlFile);
            r = new StringReader(fileContent);
        }
        return r;
    }
    
    public static void main(String[] args) throws Exception {
        XppCountMain runner = new XppCountMain();
        runner.run(args);
    }
    
    public void run(String[] args) throws Exception {
        XppCount driver = new XppCount();
        
        // allows user to provide system default -Dorg.gjt.xpp.XmlPullParserFactory=FACTORY_CLASS
        driver.factoryClassName = System.getProperty(
            org.gjt.xpp.XmlPullParserFactory.DEFAULT_PROPERTY_NAME);
        
        for(int i = 0; i < args.length; ++i) {
            String arg = args[i];
            String value = null;
            if(i < args.length - 1) value = args[i + 1];
            if(arg.charAt(0) == '-') {
                if(arg.equals("-help")) {
                    usage();
                } else if(arg.equals("-nowait")) {
                    nowait = true;
                } else if(arg.equals("-hard")) {
                    if(hugeFiles) {
                        usage("before setting hard limit huge files must be enabled");
                    }
                    if(value == null) usage();
                    driver.hardLimit = Integer.parseInt(value);
                    ++i;
                } else if(arg.equals("-soft")) {
                    if(hugeFiles) {
                        usage("before setting soft limit huge files must be enabled");
                    }
                    if(value == null) usage();
                    driver.softLimit = Integer.parseInt(value);
                    ++i;
                } else if(arg.equals("-c")) {
                    if(value == null) usage();
                    if(charsetName != null) {
                        usage("charset name was already set to '"+charsetName+"' and not '"+value+"'");
                    }
                    charsetName = value;
                    ++i;
                } else if(arg.equals("-H")) {
                    hugeFiles = true;
                } else if(arg.equals("-t")) {
                    trace = true;
                } else if(arg.equals("-v")) {
                    verbose = true;
                } else if(arg.equals("-n")) {
                    driver.supportNamespaces = true;
                } else if(arg.equals("-s")) {
                    //driver.speedTest = true;
                    if(value == null) usage();
                    driver.speedTest = Integer.parseInt(value);
                    ++i;
                } else if(arg.equals("-x")) {
                    driver.disallowMixedContent = true;
                } else if(arg.equals("-f")) {
                    if(driver.factoryClassName != null) {
                        usage("factory class name already set to "
                                  +driver.factoryClassName );
                    }
                    if(value == null) usage();
                    driver.factoryClassName  = value;
                    ++i;
                } else
                    usage();
            } else {
                xmlFile = arg;
            }
        }
        
        trace("parameters processed and starting test now...");
        driver.runTest(xmlFile, this, this);
    }
    
    private void usage() { usage(null); }
    
    private void usage(String errMsg) {
        if(errMsg != null) {
            System.err.print(
                "Error: "+errMsg+"\n");
        }
        System.err.print(
            "Usage\n"
                +"  "+getClass().getName()+" [options] <XML file>\n"
                +"\n"
                +"Options:\n"
                +"  -c CHARSET_NAME\tUse charset for decoding input as defined in Java\n"
                +"             \teg.: US-ASCII, ISO-8859-1, UTF-8, UTF-16) .\n"
                +"  -f\tFactory class name to use. Defaults to system property if any.\n"
                +"  -H\tEnable processing huge files (do not buffer whole file)\n"
                +"  -n\tEnable namespace processing. Defaults to off.\n"
                +"  -s COUNT\tRun speed test COUNT times.\n"
                +"  -t\tPrint tracing output only. Defaults to off.\n"
                +"  -v\tVerbose output. Defaults to off.\n"
                +"  -x\tDisallow mixed content. Defaults to off.\n"
                +"  -soft x\tSet soft limit to x  on buffer size. Defaults to -1.\n"
                +"  -hard x\tSet hard limit to x on buffer size. Defaults to -1.\n"
                +"\n"
                +"This program prints the number of elements, attributes,\n"
                +"white spaces and other non-white space characters in the input file.\n"
                +"\n"
        );
        System.exit(1);
    }
    
    
    private static InputStream openData(Class klass, String name)
        throws IOException
    {
        final String RES_PREFIX = "/src/samples/common/count/";
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
    
    private static String loadData(Class klass, String name, String charsetName)
        throws IOException
    {
        InputStream in = openData(klass, name);
        if(in == null) {
            throw new IOException("can't load "+name);
        }
        
        Reader reader;
        if(charsetName != null) {
            reader = new InputStreamReader(new BufferedInputStream(in), charsetName);
        } else {
            reader = new InputStreamReader(new BufferedInputStream(in));
        }
        StringWriter sink = new StringWriter();
        char[] buf = new char[4096];
        int got;
        while((got = reader.read(buf)) != -1) {
            sink.write(buf, 0, got);
        }
        return sink.toString();
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

