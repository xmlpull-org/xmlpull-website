/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XppNodeTree.java,v 1.4 2003/04/06 00:04:01 aslom Exp $
 */

package swing;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import org.gjt.xpp.*;

public class XppNodeTree extends JTree
{
    static final int HEIGHT = 460;
    static final int WIDTH = 540;
    
    
    public XppNodeTree(TreeModel newModel)
    {
        super(newModel);
    }
    
    public String convertValueToText(Object node,
                                     boolean selected,
                                     boolean expanded,
                                     boolean leaf,
                                     int row,
                                     boolean hasFocus)
    {
        //return "ALEK "+value.toString();
        if(node instanceof XmlNode) {
            XmlNode xnode = (XmlNode)node;
            StringBuffer buf = new StringBuffer(""); //Node:");
            buf.append(xnode.getRawName());
            for (int i = 0; i < xnode.getAttributeCount(); i++)
            {
                buf.append(" ");
                buf.append(xnode.getAttributeRawName(i));
                buf.append("='");
                buf.append(xnode.getAttributeValue(i));
                buf.append("'");
            }
            return buf.toString();
        } else {
            return node.toString();
            
        }
    }
    
    public static void makeFrame(String inputName, XmlNode root) {
        JFrame frame = new JFrame("XppNodeTreeFrame");
        frame.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            }
        );
        
        JPanel treePanel = new JPanel();
        
        EmptyBorder eb = new EmptyBorder(7,7,7,7);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb,bb);
        treePanel.setBorder(new CompoundBorder(cb,eb));
        
        JTree tree = new XppNodeTree(new XmlNodeToTreeModelAdapter(root));
        
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(
            new Dimension( WIDTH, HEIGHT ));
        
        treePanel.setLayout(new BorderLayout());
        treePanel.add("Center", treeView );
        
        frame.getContentPane().add("Center", treePanel );
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = WIDTH + 10;
        int h = HEIGHT + 10;
        frame.setLocation(screenSize.width/3 - w/2,
                          screenSize.height/2 - h/2);
        frame.setSize(w, h);
        frame.setVisible(true);
        frame.setTitle("XPP Node Tree - "+inputName);
    }
    
    public static void main(String args[])
    {
        if (args.length < 1) {
            System.err.println(
                "Usage: java "
                    +XppNodeTree.class.getName()+" filename");
            System.exit(1);
        }
        
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.DEFAULT_PROPERTY_NAME));
            factory.setNamespaceAware(true);
            XmlPullParser pp = factory.newPullParser();
            XmlNode root =factory.newNode();
            try {
                // try to load XML assuming no mixed content
                //   if it is the case then built tree is much nicer
                pp.setAllowedMixedContent(false);
                pp.setInput(new FileReader( args[0] ));
                pp.next(); // get first start tag
                pp.readNode(root);
            } catch(XmlPullParserException ex) {
                System.out.println("Trying again with mixed content "+ex);
                pp.setAllowedMixedContent(true);
                pp.setInput(new FileReader( args[0] ));
                pp.next(); // get first start tag
                pp.readNode(root);
            }
            //System.out.println(root);
            
            makeFrame(args[0], root);
            
        } catch (XmlPullParserException sxe) {
            Throwable t = sxe;
            if (sxe.getDetail() != null)
                t = sxe.getDetail();
            t.printStackTrace();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

