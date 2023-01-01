#include "XppTestCase.h"

#include <iostream>
#include <fstream>
#include <string>
#include <xpp/XmlPullParser.h>

using namespace std;
using namespace xpp;


void XppTestCase::setUp()
{
}


Test *XppTestCase::suite() 
{
	TestSuite *testSuite = new TestSuite ("XppTestCase");

	testSuite->addTest (new TestCaller <XppTestCase> 
	  ("testXpp", testXpp));
	testSuite->addTest (new TestCaller <XppTestCase> 
	  ("testAttribUniq", testAttribUniq));
	testSuite->addTest (new TestCaller <XppTestCase> 
	  ("testNormalizeLine", testNormalizeLine));
	return testSuite;
}

void XppTestCase::testXpp() throw(XmlPullParserException)
{
  char * buf = 
"<?xml version=\"1.0\"?>\n"
"<m:PlaceOrder xmlns:m='Some-Namespace-URI'"
" xmlns:xsd=\"http://w3.org/ns/data\""
" xmlns:xsi=\"http://w3.org/ns/instances\""
">\n"
"<DaysToDelivery xsd:type=\"xsi:integer\">7</DaysToDelivery>\n"
"<tree><subtree1><a1>aaa</a1><a2><a3>X</a3></a2></subtree1><subtree2/></tree>"
"<empty/>\n"
"<top>\n"
"<codebase xsi:type= 'xsd:string' > </codebase>\n"
"<codebase2 xsi:type= 'xsd:string' ></codebase2>\n"
;

  //int type; 
  int bufSize = strlen(buf);
  pp.setInput(buf, bufSize);
  pp.setMixedContent(false);

  //throw XmlPullParserException(string("test excpetion catching"));

  assert(pp.next() == XmlPullParser::START_TAG);
  pp.readStartTag(stag);
  assert(string("PlaceOrder")  == stag.getLocalName());
  const char * uri = stag.getUri();
  assert(string("Some-Namespace-URI") == uri);
  
  assert(pp.next() == XmlPullParser::START_TAG);
  pp.readStartTag(stag);
  assert(string("DaysToDelivery")  == stag.getLocalName());

  
  // check utility functions
  const char *local = pp.getQNameLocal("ns:alek");
  assert(string(local) == "alek");
  uri = pp.getQNameUri("ns:alek");
  assert(NULL == uri);
  uri = pp.getQNameUri("alek");
  assert(string("") == uri);
  uri = pp.getQNameUri("m:alek");
  assert(string("Some-Namespace-URI") == uri);
  
  assert(pp.next() == XmlPullParser::CONTENT);
  assert(string("7") == pp.readContent());

  assert(pp.next() == XmlPullParser::END_TAG);
  pp.readEndTag(etag);
  assert(string("DaysToDelivery")  == etag.getLocalName());

  assertEquals(XmlPullParser::START_TAG, pp.next()); pp.readStartTag(stag);
  assert(string("tree") ==  stag.getLocalName());
  pp.skipSubTree();
  pp.readEndTag(etag);
  assert(string("tree") == etag.getLocalName());

  assertEquals(XmlPullParser::START_TAG, pp.next()); pp.readStartTag(stag);
  assert(string("empty") == stag.getLocalName());
  pp.skipSubTree();
  pp.readEndTag(etag);
  assert(string("empty") == etag.getLocalName());

  assertEquals(XmlPullParser::START_TAG, pp.next()); 
  pp.readStartTag(stag);
  //assert(string("top") == stag.getLocalName());

  assertEquals(XmlPullParser::START_TAG, pp.next()); pp.readStartTag(stag);
  assert(string("codebase") == stag.getLocalName());
  assertEquals(XmlPullParser::CONTENT, pp.next());
  assert(string(" ") == pp.readContent());
  assertEquals(pp.next(), XmlPullParser::END_TAG);
  pp.readEndTag(etag);
  assert(string("codebase") == etag.getLocalName());

  assertEquals(XmlPullParser::START_TAG, pp.next()); pp.readStartTag(stag);
  assert(string("codebase2") == stag.getLocalName());
  assertEquals(XmlPullParser::CONTENT, pp.next());
  assert(string("") == pp.readContent());
  assertEquals(pp.next(), XmlPullParser::END_TAG);
  pp.readEndTag(etag);
  assert(string("codebase2") == etag.getLocalName());    
}

void XppTestCase::testAttribUniq() throw(XmlPullParserException)
{

    const SXT_STRING attribsOk = 
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"
"a='a' b='b' m:a='a' n:b='b' n:x='a'"
"/>\n"
"";

    const SXT_STRING duplicateAttribs =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"
"a='a' b='b' m:a='a' n:b='b' a='x'"
"/>\n"
"";

    const SXT_STRING duplicateNsAttribs =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:n='Some-Namespace-URI'"
"a='a' b='b' m:a='a' n:b='b' n:a='a'"
"/>\n"
"";

    const SXT_STRING duplicateXmlns =
"<m:test xmlns:m='Some-Namespace-URI' xmlns:m='Some-Namespace-URI'"
""
"/>\n"
"";

    const SXT_STRING duplicateAttribXmlnsDefault =
"<m:test xmlns='Some-Namespace-URI' xmlns:m='Some-Namespace-URI'"
"a='a' b='b' m:b='b' m:a='x'"
"/>\n"
"";

    parseOneElement(attribsOk.c_str(), false);      

    parseOneElement(attribsOk.c_str(), true);      
    parseOneElement(duplicateNsAttribs.c_str(), false);      
    parseOneElement(duplicateAttribXmlnsDefault.c_str(), false);      
    parseOneElement(duplicateAttribXmlnsDefault.c_str(), true);      
      
    bool thrown = false;
    try {
      parseOneElement(duplicateNsAttribs.c_str(), true);          
    } catch (const XmlPullParserException& e) {
      thrown = true;
    }
    assert(thrown);
    
    thrown = false;
    try {
      parseOneElement(duplicateAttribs.c_str(), false);          
    } catch (const XmlPullParserException& e) {
      thrown = true;
    }
    assert(thrown);

    thrown = false;
    try {
      parseOneElement(duplicateAttribs.c_str(), true);          
    } catch (const XmlPullParserException& e) {
      thrown = true;
    }
    assert(thrown);

    thrown = false;
    try {
      parseOneElement(duplicateXmlns.c_str(), true);          
    } catch (const XmlPullParserException& e) {
      thrown = true;
    }
    assert(thrown);

    thrown = false;
    try {
      parseOneElement(duplicateXmlns.c_str(), false);          
    } catch (const XmlPullParserException& e) {
      thrown = true;
    }
    assert(thrown);

}

void XppTestCase::testNormalizeLine() throw(XmlPullParserException)
{
    const SXT_STRING lineBreaks = 
"<m:test xmlns:m='Some-Namespace-URI'>"
"fifi\r&amp;\r&amp;\r\n foo &amp;\r bar \n\r\n&quot;"
"\r \r\n \n\r \n\n \r\n\r \r\r \r\n\n \n\r\r\n\r \r\n"
"</m:test>\r\n";

    const SXT_STRING expected = 
"fifi\n&\n&\n foo &\n bar \n\n\""
"\n \n \n\n \n\n \n\n \n\n \n\n \n\n\n\n \n"
;
        
    parseOneElement(lineBreaks.c_str(), true);      

    assertEquals(XmlPullParser::CONTENT, pp.next());
    assert(SXT_STRING(expected) == pp.readContent());

    assertEquals(pp.next(), XmlPullParser::END_TAG);
    pp.readEndTag(etag);
    assert(SXT_STRING("test") == etag.getLocalName());

    // having \r\n as last characters is the hardest case
    //assertEquals(XmlPullParser.CONTENT, pp.next());
    //assertEquals("\n", pp.readContent());

    assertEquals(pp.next(), XmlPullParser::END_DOCUMENT);
}
 
 
void XppTestCase::parseOneElement(const char * buf, 
	  const bool supportNamespaces) throw(XmlPullParserException)
{
    pp.setInput(buf, SXT_STRLEN(buf));
    pp.setSupportNamespaces(supportNamespaces);
    pp.setMixedContent(false);
    
    pp.next();
    pp.readStartTag(stag);
    if(supportNamespaces) {
      assert(string("test") ==  stag.getLocalName());
    } else {
      assert(string("m:test") == stag.getLocalName());
      //cout << (stag.getLocalName() == NULL); //stag.toString(); //stag.getQName();
    }
}	  
