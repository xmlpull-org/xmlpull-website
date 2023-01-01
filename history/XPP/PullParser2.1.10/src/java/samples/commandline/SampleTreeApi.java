/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */


package commandline;

import java.io.*;

import org.gjt.xpp.*;

/**
 * XPP usage example code. Contains the example methods which perform
 * the processing of an input document.
 *
 * @author Dennis M. Sosnoski
 * @author Aleksander Slominski [http://www.extreme.indiana.edu/~aslom/]
 * @version 1.2
 */

public class SampleTreeApi
{
    protected int m_elements;
    protected int m_adds;
    protected int m_deletes;
    
    /** Pull parser factory used within a test run. */
    protected XmlPullParserFactory m_parserFactory;
    
    /**
     * Modify subtree for element. This recursively walks through the document
     * nodes under an element, performing the modifications.
     *
     * @param element element to be walked
     */
    
    protected void modifyElement(XmlNode element) throws Exception {
        
        // loop through child nodes
        for (int i = 0; i < element.getChildrenCount(); i++) {
            
            // handle child by node type
            Object child = element.getChildAt(i);
            if (child instanceof String) {
                
                // trim whitespace from content text
                String trimmed = child.toString().trim();
                if (trimmed.length() == 0) {
                    
                    // delete child if only whitespace (adjusting index)
                    element.removeChildAt(i--);
                    m_deletes++;
                    
                } else {
                    
                    // wrap the trimmed content with new element
                    XmlNode text = element.newNode(element.getNamespaceUri(), "text");
                    text.appendChild(trimmed);
                    element.replaceChildAt(i, text);
                    m_adds++;
                    
                }
            } else if (child instanceof XmlNode) {
                
                // handle child elements with recursive call
                modifyElement((XmlNode)child);
                m_elements++;
                
            }
        }
    }
    
    /**
     * Process document file. This override of the abstract base class method
     * demonstrates document handling for this representation.
     *
     * @param in input stream for reading document text
     * @param out output stream for writing document text
     * @throws Exception anything thrown by example code
     */
    
    protected void processFile(String fileName, InputStream in, OutputStream out)
        throws Exception {
        
        // setup XPP2 factory
        m_parserFactory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.DEFAULT_PROPERTY_NAME));
        System.out.println("using factory "+m_parserFactory.getClass());
        m_parserFactory.setNamespaceAware(true);
        
        // parse the document from input stream
        XmlNode doc = m_parserFactory.readNode(new BufferedReader(new InputStreamReader(in)));
        
        // recursively walk and modify document
        m_elements++;
        modifyElement(doc);
        
        // write the document to output stream
        m_parserFactory.writeNode(doc, new OutputStreamWriter(out), true);
        
        // statistics
        //            System.out.println("\n elements: "+m_elements
        //                                   +" adds: "+m_adds
        //                                   +" deletes: "+m_deletes);
        
        System.out.println("\n\n"+fileName+": "
                               +""+m_elements+" elements visited, "
                               +m_adds+" text wrappers added"
                               +" and "+m_deletes+" whitespace content deleted");
        
    }
    
    /**
     * Main execution method. Just creates an instance of the class and passes
     * the file list to the base class method for processing.
     *
     * @param argv command line arguments
     */
    
    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.err.println("required argument with xnml file");
            System.exit(1);
        }
        InputStream in = new FileInputStream(args[0]);
        OutputStream out = new PrintStream(System.err);
        new SampleTreeApi().processFile(args[0], in, out);
    }
}

