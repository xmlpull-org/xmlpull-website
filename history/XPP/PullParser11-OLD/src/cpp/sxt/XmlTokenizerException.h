// -*-c++-*- --------------74-columns-wide-------------------------------|
#ifndef _XML_TOKENIZER_EXCEPTION_H__
#define _XML_TOKENIZER_EXCEPTION_H__

#include <string>
#include <exception>

using namespace std;

namespace sxt {

  class XmlTokenizerException  : public exception{
  public:
    //XmlTokenizerException() throw() : message(string("XmlTokenizerException")) {}
    XmlTokenizerException(string exMessage) throw() 
      : message(exMessage) {}

    XmlTokenizerException(string exMessage, int exRow, int exColumn) throw() 
      : message(exMessage), row(exRow), column(exColumn) {}

    XmlTokenizerException(const XmlTokenizerException& other) throw() 
      : exception (other)
     {  message       = other.message; }

    XmlTokenizerException& operator=(const XmlTokenizerException& other)
     throw()
    { 
      exception::operator= (other);

      if (&other != this) {
        message = other.message; 
      }

      return *this; 
    }

    int getLineNumber() const { return row; }
    int getColumnNumber() const { return column; }

    string getMessage() const throw() { return message; }
    void setMessage(string exMessage) throw() { message = exMessage;}
    
    virtual const char* what() const throw() {
      return message.c_str();
    }
          

    friend ostream& operator<<(ostream& output, 
       const XmlTokenizerException& xte);

  protected:
    string message;
    int row;
    int column;
  };

inline ostream& operator<<(ostream& output, 
  const XmlTokenizerException& xte) 
{
    output << "XmlTokenizerException: ";
    output << xte.message << endl;
    return output;
}


} //namespace
#endif // _XML_TOKENIZER_EXCEPTION_H__
