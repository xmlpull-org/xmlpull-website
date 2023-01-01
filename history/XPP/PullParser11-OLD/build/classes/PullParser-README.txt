Pull Parser 1.1  was designed for and it should be optimal for applications
that require very small size XML parser - the jar file with compiled classes is around 20KB.
Its pull parsing model is especially well suited for
unmarshalling complex data structures from XML.

Full source code both for Java and C++ versions is available
under open source license (see LICENSE.txt).

In short - advantages:
 * supports most of XML 1.0 (except validation and external entities)
 * source code for Java & C++ version available under open source license
 * almost identical for both Java and C++ versions
 * pull interface - ideal for deserializing XML objects (like SOAP)
 * fast and simple (thin wrapper around XmlTokenizer class - adds  
   about 10% for big documents, maximum 50% additional time for small documents)
 * lightweight memory model - minimized memory allocation: 
   element content and attributes are only read on explicit 
   method calls, both StartTag and EndTag can be reused during parsing
 * small footprint - total compiled size around 20K
 * by default supports namespaces parsing  (can be switched off)
 * support for mixed content can be explicitly disabled
 * minimal memory utilization: does not use memory except for 
   input and content buffer (that can grow in size)
 * fast: all tokenizing done in one function (simple automata)
 * xml tokenizer supports on demand parsing of 
   Characters, CDSect, Comments, PIs etc.

and it has some limitations:
 * this is beta version - may have still bugs :-)
 * this is non validating parser and it does not parse DTD (recognizes only 
   predefined entities) but most of new applications will use XML schemas 
   and ignore DTDs...
 * C++ version can potentially work with UNICODE (SXT_CHAR deined to wchar_t) 
   but it was not tested, more() could be changed to support UTF8 
 
Precompiled jar file and class files are included so compilation is not necessary but
to compile Java version from source make sure to have ant.jar and xerces.jar (1.1.2)
copied to lib subdirectory and use make.bat on Windows or ./make.sh on UN*X:

  make xpp
  
To run automatic tests use run.bat on Windows or ./run.sh on UN*X:

  run xpp_test

To run sample application that counts XML elements, attributes etc. (-h for felp)
use on Windows run.bet:
  
  run xppcount [file.xml]


To compile C++ version:

  cd src/cpp/samples/pullparser

please edit Makefile to edit C++ compiler invocations and do:

  make

then you can run test program (where MACH is either SunOS, Linux or IRIX64)
 
  ./XPPCount_MACH

to test speed please execute:

  make rel

and run test:

  ./XPPCountRel_MACH -s


Let us know if you find this package useful :-)

Aleksander Slominski

IU Extreme! Computing Lab 
http://www.extreme.indiana.edu/soap    
For bug reports please use mailing list mailto:soaprmi@extreme.indiana.edu
To subscribe and to see archives use http://mailman.cs.indiana.edu/mailman/listinfo/soaprmi/
