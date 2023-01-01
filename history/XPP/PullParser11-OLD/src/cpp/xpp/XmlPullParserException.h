// -*-c++-*- --------------74-columns-wide-------------------------------|
#ifndef _XML_PULL_PARSER_EXCEPTION_H__
#define _XML_PULL_PARSER_EXCEPTION_H__

#include <string>
#include <exception>
//#include <stdexcept>

using namespace std;

namespace xpp {

  class XmlPullParserException : public exception {
  public:
    //XmlPullParserException() throw() : message(string("XmlPullParserException")) {}
    XmlPullParserException(string exMessage) 
      throw() : message(exMessage) {}

    XmlPullParserException(string exMessage, int exRow, int exColumn) throw() 
      : message(exMessage), row(exRow), column(exColumn) {}



    XmlPullParserException(const XmlPullParserException& other) 
      throw() : exception (other)
     {  message       = other.message; }

    XmlPullParserException& operator=(const XmlPullParserException& other) throw()
    { 
      exception::operator= (other);

      if (&other != this) {
        message       = other.message; 
      }

      return *this; 
    }

    int getLineNumber() const { return row; }
    int getColumnNumber() const { return column; }

    string getMessage() const throw() {return message;}
    void setMessage(string exMessage) throw() { message = exMessage;}
    
    virtual const char* what() const throw() {
      return message.c_str();
    }

  protected:

    friend ostream& operator<<(ostream& output, 
       const XmlPullParserException& xppe);
          

    string message;
    int row;
    int column;    
  };

inline ostream& operator<<(ostream& output, 
  const XmlPullParserException& xppe) 
{
    output << "XmlPullParserException: ";
    output << xppe.message << endl;
    return output;
}


} //namespace

#endif // _XML_PULL_PARSER_EXCEPTION_H__
