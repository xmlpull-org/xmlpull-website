// -*-c++-*- --------------74-columns-wide-------------------------------|
#include <stdio.h>
#include <iostream>
//#include <fstream>
#include <string>
#include <sxt/XmlTokenizer.h>

using namespace std;
using namespace sxt;


#ifdef WIN32
// for standard long GetTickCount( void );
#include <windows.h>
#else
#include    <unistd.h>
#include    <sys/timeb.h>
long GetTickCount( void ) {
    timeb aTime;
    ftime(&aTime);
    return (long)(aTime.time*1000 + aTime.millitm);
}
#endif


static bool verbose;

const char * loadFile(const char* fileName) {
  FILE *stream;

  stream = fopen( fileName, "rb" );
  if( stream == NULL ) {
    cerr << "File " << fileName << " could not be opened " << endl;
    exit(1);
  }
  fseek( stream, 0L, SEEK_END);
  int size = ftell( stream );
  if(verbose) cout << fileName << " size=" << size << endl;
  fseek( stream, 0L, SEEK_SET);
  char * buf = (char *) malloc(size * sizeof(*buf) + 1);
  int read = fread( buf, sizeof(*buf), size, stream);
  if(verbose) cout << fileName << " read=" << read << endl;
  buf[read] = 0;
  fclose( stream );
  return buf;
  /*  
  MUCH MUCH TOO SLOW for 2M file
  ifstream inputFile(inputName, ios::in);
  if (!inputFile) {
  }
  //string inputXML;
  string buffer;
  //int const lineSize=1000;
  while (!inputFile.eof() ) {
    //buffer += inputXML;
    //cout << "next string is: " << inputXML << endl;
    string line;
    getline(inputFile, line);
    buffer += line ;
    buffer += "\n";
  }
  return buffer;
*/
}




void usage() {
  cout << 
    "Usage\n"
    "  SXTCount [options] <XML file>\n"
    "\n"
    "Options:\n"
    "  -a\tEnable processing of all tokens. Defaults to off.\n"
    "  -s\tRun speed test.\n"
    "  -t\tPrint tracing output only. Defaults to off.\n"
    "  -v\tVerbose output.\n"
    "  -x\tDislallow mixed content. Defaults to off.\n"
    "\n"
    "This program prints the number of elements, attributes,\n"
    "white spaces and other non-white space characters in XML file.\n"
    "\n";
  exit(1);
}




int main(int argc, char **argv) {  
  bool allTokens = false;
  bool trace = false; 
  bool disallowMixedContent = false; 
  bool speedTest = false;
  bool nowait = false;  
  const char* xmlFile = "test.xml";
  for(int argpos = 1; argpos < argc; ++argpos) {
    char *arg = argv[argpos];
    if(arg[0] == '-') {
      if(strncmp(arg, "-a", 2) == 0)
        allTokens = true;
      else if(strncmp(arg, "-nowait", 7) == 0)
        nowait = true;
      else if(strncmp(arg, "-s", 2) == 0)
        speedTest = true;
      else if(strncmp(arg, "-t", 2) == 0)
        trace = true;
      else if(strncmp(arg, "-v", 2) == 0)
        verbose = true;
      else if(strncmp(arg, "-x", 2) == 0)
        disallowMixedContent = true;
      else
        usage();
    } else {
      xmlFile = arg;
    }
  }

  const char *buf = loadFile(xmlFile);


  XmlTokenizer* sxt = new XmlTokenizer();
  int bufSize = strlen(buf);
  sxt->setMixedContent(!disallowMixedContent);
  sxt->setNotifyAll(allTokens);
  sxt->setParseContent(true);
  
  int token; 

  int elementCount = 0;
  int attrCount = 0;
  int spaceCount = 0;
  int characterCount = 0;

  

  double duration = -1;
  double walltime = -1;

  try {
   
     if(trace) {
       cout << "================ START OF INPUT(" 
            << xmlFile << ")" << endl;
       cout << buf;
       cout << "================ END OF INPUT" << endl;

       cout << "================ START TOKENIZING" << endl;
       sxt->setInput(buf, bufSize);


       string name;
       string s;
       while((token = sxt->next()) != XmlTokenizer::END_DOCUMENT) {
         switch(token) {
         case XmlTokenizer::CHARACTERS:
           name = "characters";
           break;  
         case XmlTokenizer::CHAR_REF:
           name = "char_ref";
           break;  
         case XmlTokenizer::ENTITY_REF:
           name = "entity";
           break;  
         case XmlTokenizer::CDSECT:
           name = "CDATA";
           break;  
         case XmlTokenizer::COMMENT:
           name = "comment";
           break;  
         case XmlTokenizer::CONTENT:
           name = "content";
           break;  
         case XmlTokenizer::DOCTYPE:
           name = "DOCTYPE";
           break;  
         case XmlTokenizer::PI:
           name = "PI";
           break;  
         case XmlTokenizer::STAG_NAME:
           name = "STag name";
           break;  
         case XmlTokenizer::ETAG_NAME:
           name = "ETag name";
           break;  
         case XmlTokenizer::EMPTY_ELEMENT:
           name = "Empty element";
           break;  
         case XmlTokenizer::STAG_END:
           name = "STag end";
           break;  
         case XmlTokenizer::ATTR_NAME:
           name = "Attr name";
           break;  
         case XmlTokenizer::ATTR_CHARACTERS:
           name = "Attr chars";
           break;  
         case XmlTokenizer::ATTR_CONTENT:
           name = "Attr content";
           break;  
        default:
           name = "unknown token "+token;
         }  
         if(sxt->parsedContent) {
            s = string(sxt->pc + sxt->pcStart, 
                       sxt->pcEnd - sxt->pcStart);
         } else {
            s = string(sxt->buf + sxt->posStart, 
                       sxt->posEnd - sxt->posStart);
         }
         string prefix = "";
         if(token == XmlTokenizer::STAG_NAME 
           || token == XmlTokenizer::ETAG_NAME 
           || token == XmlTokenizer::ATTR_NAME   
         ) 
         {
           if(sxt->posNsColon > 0)
             prefix = " prefix=" + 
                      s.substr(0, sxt->posNsColon - sxt->posStart);
         }  
         cout << name << "='"  << s << "'" << prefix << endl;
       }

       cout << "================ END TOKENIZING" << endl;     
     } //else {
     
   
     int runs =  10;
     if(speedTest) {
       runs =  10000000 / bufSize + 2;   //3; //parse 10M characters
       if(verbose) cout << "start speed test runs=" << runs << endl;
     }
     const long startMillis = GetTickCount();

     for(int i = 0; i  < runs; ++i) {

       elementCount = 0;
       attrCount = 0;
       spaceCount = 0;
       characterCount = 0;

       sxt->setInput(buf, bufSize);
	     
       while((token = sxt->next()) != XmlTokenizer::END_DOCUMENT) {
         switch(token) {
         case XmlTokenizer::CONTENT:
           if(sxt->parsedContent)
             characterCount += sxt->pcEnd - sxt->pcStart;
           else 
             characterCount += sxt->posEnd - sxt->posStart;
           break;
         case XmlTokenizer::STAG_NAME:
           ++elementCount;
           break;
         case XmlTokenizer::ATTR_NAME:  
            ++attrCount;
   	 }        
       }

     }
     
     const long endMillis = GetTickCount();
     walltime = (double)(endMillis - startMillis);
     duration = walltime / runs;
   
             
     delete sxt;     
     sxt = NULL;
     free((void *)buf);
     buf = NULL;		

    } catch (const XmlTokenizerException& e) {
        cerr << "\nTokenizer error during tokenizing: '" 
            << xmlFile << "'\n"
            << "Exception message is:  \n"
            << e.getMessage() << "\n" << endl;
        return 3;
    } catch (const exception& e) {     //catch (exception e)

        cerr << "\nError during tokenizing: '" << xmlFile << "'\n"
            << "Exception message is:  \n"
            << e.what() << "\n" << endl;
        return 4;
    } catch (...) {
        cerr << "\nUnexpected exception during parsing: '" << xmlFile << "'\n";
        return 5;
    }


     // Print out the stats that we collected and time taken
    cout << xmlFile << ": ";
    if(speedTest)
      cout << duration << " ms"
           << " total: " << walltime / 1000.0 << " s";

    cout << " ("
         << elementCount  << " elems, "
         << attrCount  << " attrs, "
         << spaceCount  << " spaces, "
         << characterCount << " chars)" << endl;

    if(verbose) cout << "done!" << endl;

    if(!nowait) {
      cout << "press Enter to close program ... ";
      string line;
      getline(cin, line);
    }

    return 0;
    
}
