// -*-c++-*- --------------74-columns-wide-------------------------------|
#ifndef XPP_END_TAG_H_
#define XPP_END_TAG_H_

#include <string>

#include <xpp/XmlPullParser.h>

using namespace std;

/**
 * Encapsulate XML ETag
 * 
 *
 * @author Aleksander Slominski [aslom@extreme.indiana.edu]
 */

namespace xpp {

  class EndTag {

    friend class XmlPullParser;

  public:
    EndTag() { init(); }
    /** 
     *  
     * @return DO NOT DEALLOCATE RETURN VALUE! 
     */
    const SXT_CHAR* getUri() const { return uri; }

    /** 
     *  
     * @return DO NOT DEALLOCATE RETURN VALUE! 
     */
    const SXT_CHAR* getLocalName() const { return localName; }      

    /** 
     *  
     * @return DO NOT DEALLOCATE RETURN VALUE! 
     */
    const SXT_CHAR* getQName() const { return qName; }

    const SXT_STRING toString() const {
      string buf = SXT_STRING("EndTag={");    
      buf = buf + " '" + qName + "'";
      if(SXT_STRING(_T("")) != uri) {
        buf = buf + "('" + uri +"','" + localName + "') ";
      }
      buf += " }";
      return buf;
    }
    
  protected:
    void init() {
      uri = NULL;
      localName = NULL;
      qName = NULL;
    }

    friend ostream& operator<<(ostream& output, 
      const EndTag& startTag);
          
               
    // ===== internals available for superclasses
    
    const SXT_CHAR* uri;
    const SXT_CHAR* localName;
    const SXT_CHAR* qName;
   
  };
  
inline ostream& operator<<(ostream& output, 
  const EndTag& endTag) 
{
    const SXT_STRING s = endTag.toString();
    output << s << endl;
    return output;
}

  
  
} //namespace

#endif // XPP_END_TAG_H_
