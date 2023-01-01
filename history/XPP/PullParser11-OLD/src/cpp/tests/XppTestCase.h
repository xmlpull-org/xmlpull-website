#ifndef CPP_UNIT_XppTESTCASE_H
#define CPP_UNIT_XppTESTCASE_H

#include "TestCase.h"
#include "TestSuite.h"
#include "TestCaller.h"

#include <xpp/XmlPullParser.h>

using namespace std;
using namespace xpp;

/* 
 * A test case that is designed to test XML Pull Parser
 *
 */

class XppTestCase : public TestCase
{
protected:

  StartTag stag;
  EndTag etag;    
  XmlPullParser pp;

public:
  XppTestCase (std::string name) : TestCase (name) {}

  void setUp ();
  static Test *suite ();

protected:
  void testXpp() throw(XmlPullParserException);
  void testNormalizeLine() throw(XmlPullParserException);
  void testAttribUniq() throw(XmlPullParserException);

  void parseOneElement(const char * buf, 
    const bool supportNamespaces) throw(XmlPullParserException);

};


#endif
