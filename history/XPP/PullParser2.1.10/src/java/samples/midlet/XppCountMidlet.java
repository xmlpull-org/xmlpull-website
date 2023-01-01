/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XppCountMidlet.java,v 1.3 2003/04/06 00:04:01 aslom Exp $
 */

package midlet;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import org.gjt.xpp.*;

import shared.count.ProgressObserver;
import shared.count.ReaderFactory;
import shared.count.XppCount;


/**
 * Simple Midlet to show XppCount working.
 */
public class XppCountMidlet extends MIDlet
  implements CommandListener, ProgressObserver, ReaderFactory, Runnable
{
  private final static boolean DEBUG = false;
  Command exitCommand  = new Command("Exit", Command.EXIT, 2);
  Command parseCommand = new Command("Parse", Command.SCREEN, 1);
  Command speedCommand = new Command("Test", Command.SCREEN, 1);
  Command okCommand = new Command("OK", Command.OK, 1);
  Display display;
  Form welcomeForm;
  TextField inputXml;
  TextBox parsingOutput;
  //Form outputForm;
  //TextField outputParsing;
  
  boolean stopped = true;
  boolean finishRun;
  char[] xmlContent;
  XppCount driver;
  
  Thread runner;
  
  public XppCountMidlet() {}
  
  private void debug(String msg)
  {
        // notice that J2ME output is at leat erratic....
        //  CAN NOT be depended for real debugging...
        if(DEBUG) System.err.println(msg);
  }
  
  public void startApp() throws MIDletStateChangeException
  {
        welcomeForm = new Form("XPP2 DEMO");
        inputXml =  new TextField(
          "Enter XML", "<hello>World! 10</hello>", 1000,  TextField.ANY);
        welcomeForm.append(inputXml);
        welcomeForm.addCommand(parseCommand);
        welcomeForm.addCommand(exitCommand);
        welcomeForm.setCommandListener(this);
        
//      outputForm = new Form("Output");
//      outputParsing =  new TextField(
//        "", "", 1000,  TextField.ANY);
//      outputForm.append(outputParsing);
//      outputForm.addCommand(okCommand);
//      outputForm.setCommandListener(this);
        
        parsingOutput = new TextBox(
          "Output",     "", 1000,  TextField.ANY);
        parsingOutput.setCommandListener(this);
        parsingOutput.addCommand(okCommand);
        parsingOutput.setCommandListener(this);
        
        display = Display.getDisplay(this);
        display.setCurrent(welcomeForm);
        
        driver = new XppCount();
        
        runner = new Thread(this);
        finishRun = false;
        runner.start();
  }
  
  public void pauseApp()
  {
        stopped = true;
        //display = null;
  }
  
  public void destroyApp(boolean unconditional)
        throws MIDletStateChangeException
  {
        finishRun = stopped = true;
        if (unconditional) {
          notifyDestroyed();
        } else {
        }
  }
  
  public void commandAction(Command c, Displayable s) {
        try {
          debug("command="+c);
          if (c == exitCommand) {
                debug("EXIT pressed");
                
                destroyApp(false);
                notifyDestroyed();
          } else if (c == parseCommand) {
                debug("PARSE pressed");
                parsingOutput.setString("Parsing...\n");
                //outputParsing.setString("Parsing...\n");
                //print("Now parsing started...");
                display.setCurrent(parsingOutput);
                //display.setCurrent(outputForm);
                
                //transfer user input so be acccessd by newReader()
                xmlContent = new char[inputXml.size()];
                inputXml.getChars(xmlContent);
                if(DEBUG) println("parsing requested");
                debug("parsing requested");
                
                //XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                //XmlPullParser pp = factory.newPullParser();
                //pp.setInput(xmlContent);
//        while(!stopped) {
//              byte event = pp.next();
//              int last = parsingOutput.size();
//              parsingOutput.insert("X", last);
//        }
                
                stopped = false;
                //runTest(); // runner Thread will pick it up
                //runner.interrupt(); // not supported in J2ME
                Thread.currentThread().yield();
          } else if (c == okCommand) {
                debug("OK pressed");
                debug("parsing stopped");
                stopped = true;
                display.setCurrent(welcomeForm);
                //runner.interrupt(); // not supported in J2ME
                Thread.currentThread().yield();
          } else {
                debug("Unknown command "+c);
          }
        } catch (Exception ex) {
          debug(ex.toString());
          ex.printStackTrace();
        }
  }
  
  public void print(String msg)
  {
        int last = parsingOutput.size();
        parsingOutput.insert(msg, last);
//      int last = outputParsing.size();
//      outputParsing.insert(msg, last);
  }
  public void println(String msg) { print(msg+"\n"); }
  public void trace(String msg) {  }
  public void verbose(String msg) { //println(msg);
  }
  
  public boolean stopRequested() { return stopped; }
  
  public Reader newReader() throws IOException
  {
        Reader r = new MyCharArrayReader(xmlContent);
        return r;
  }
  
  
  public void runTest() {
        try{
          if(stopped == false) {
                debug("paring started...");
                if(DEBUG) println("paring started...");
                System.err.println("runtTest paring start");
                driver.runTest("INTERNAL", this, this);
                stopped = true;
                debug("parsing finished");
                if(DEBUG) println("paring finished.");
          }
        } catch(XmlPullParserException ex) {
          stopped = true;
          println("\nParsing error: "+ex.getMessage());
          ex.printStackTrace();
        } catch(java.io.IOException ex) {
          stopped = true;
          println("\nIO error: "+ex.getMessage());
          ex.printStackTrace();
        } catch(java.lang.Exception ex) {
          stopped = true;
          println("\nGeneric error: "+ex.getMessage());
          ex.printStackTrace();
        }
        
  }
  public void run() {
        
//      try {
//        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//        XmlPullParser pp = factory.newPullParser();
//        char[] data = new char[inputXml.size()];
//        inputXml.getChars(data);
//        pp.setInput(data);
//        while(!stopped) {
//              byte event = pp.next();
//              int last = parsingOutput.size();
//              parsingOutput.insert("X", last);
//        }
        int count = 0;
        while(finishRun == false) {
          try {
                Thread.sleep(200);
          } catch(InterruptedException ex) {
          }
          
          //debug("RUN "+(count++)+" stopped="+stopped);
          //if(DEBUG) println("RUN "+(count++)+" "+stopped);
          runTest();
        }
  }
  
  public class MyCharArrayReader extends Reader {
        char[] buf;
        int pos;
        
        public MyCharArrayReader(char[] buf) {
          this.buf = buf;
          pos = 0;
        }
        
        public int read(char[] cbuf,
                                        int off,
                                        int len)
          throws IOException
        {
          debug(
                "buf="+buf+" cbuf="+cbuf+" off="+off+" len="+len);
          int toRead = len;
          if(pos + toRead > buf.length) {
                toRead = buf.length - pos;
          }
          if(toRead == 0) return -1;
          System.arraycopy(buf, pos, cbuf, off, toRead);
          pos += toRead;
          return toRead;
        }
        
        public void close()
          throws IOException
        {
          if(pos >= buf.length) {
                throw new IOException(
                  "attempt to close already closed reader");
          }
          pos = buf.length;
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

