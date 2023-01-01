/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: XmlNodeToTreeModelAdapter.java,v 1.3 2003/04/06 00:04:01 aslom Exp $
 */

package swing;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeModelListener;

import org.gjt.xpp.*;

public class XmlNodeToTreeModelAdapter implements TreeModel
{
  protected XmlNode root;
  
  public XmlNodeToTreeModelAdapter(XmlNode root)
  {
	this.root = root;
  }
  
  public Object getRoot()
  {
	return root;
  }
  
  public boolean isLeaf(Object o)
  {
	return (o instanceof XmlNode) == false;
  }
  
  public int getChildCount(Object parent)
  {
	if(parent instanceof XmlNode) {
	  int count = ((XmlNode)parent).getChildrenCount();
	  // do not display emty content...
	  if(count == 1) {
		Object onlyChild = ((XmlNode)parent).getChildAt(0);
		if(onlyChild instanceof String) {
		  if( ((String)onlyChild).length() == 0) {
			return 0;
		  }
		}
	  }
	  return count;
	}
	return 0;
  }
  
  public Object getChild(Object parent, int index)
  {
	Object o = null;
	if(parent instanceof XmlNode) {
	  o = ((XmlNode)parent).getChildAt(index);
	}
	return o;
  }
  
  public int getIndexOfChild(Object parentO, Object child)
  {
	if(parentO instanceof XmlNode) {
	  XmlNode parent = (XmlNode)parentO;
	  int count = parent.getChildrenCount();
	  for (int i = 0; i < count; ++i) {
		Object c = parent.getChildAt(i);
		if (c == child) return i;
	  }
	}
	return -1;
  }
  
  // not implemented but required by tree model...
  public void valueForPathChanged(TreePath path, Object newValue) {}
  public void addTreeModelListener(TreeModelListener listener) {}
  public void removeTreeModelListener(TreeModelListener listener) {}
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

