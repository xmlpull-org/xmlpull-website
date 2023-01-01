// -*-c++-*- --------------74-columns-wide-------------------------------|
#include <stdio.h>
#include <iostream>
//#include <fstream>
#include <string>
#include <xpp/XmlPullParser.h>

using namespace std;
using namespace xpp;


#ifdef WIN32
// long GetTickCount( void ); is in windows.h
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
    "  XPPCount [options] <XML file>\n"
    "\n"
    "Options:\n"
    "  -n\tEnable namespace processing. Defaults to off.\n"
    "  -s\tRun speed test.\n"
    "  -t\tPrint tracing output only. Defaults to off.\n"
    "  -v\tVerbose output.\n"
    "  -x\tDislallow mixed content. Defaults to off.\n"
    "\n"
    "This program prints the number of elements, attributes,\n"
    "white spaces and other non-white space characters in the input file.\n"
    "\n";
  exit(1);
}

int main(int argc, char **argv) {  
  bool supportNamespaces = false; 
  bool trace = false; 
  bool disallowMixedContent = false; 
  bool speedTest = false;
  bool nowait = false;
  const char* xmlFile = "test.xml";
  for(int argpos = 1; argpos < argc; ++argpos) {
    char *arg = argv[argpos];
    if(arg[0] == '-') {
      if(strncmp(arg, "-nowait", 7) == 0)
        nowait = true;
      else if(strncmp(arg, "-n", 2) == 0)
        supportNamespaces = true;
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

  //char *buf = "<foo><alek>test</alek>";
  
  XmlPullParser* xpp = new XmlPullParser();
  int bufSize = strlen(buf);
  xpp->setMixedContent(!disallowMixedContent);
  xpp->setSupportNamespaces(supportNamespaces);

  int elementCount = 0;
  int attrCount = 0;
  int spaceCount = 0;
  int characterCount = 0;

  int type; 
  StartTag stag;
  EndTag etag;    
   
  double duration = -1;
  double walltime = -1;

  try {

    if(trace) {
      cout << "================ START OF INPUT(" << xmlFile << ")" << endl;
      cout << buf;
      cout << "================ END OF INPUT" << endl;
      cout << "================ START PARSING" << endl;

      xpp->setInput(buf, bufSize);

	    while((type = xpp->next()) != XmlPullParser::END_DOCUMENT) {
	      if(type == XmlPullParser::CONTENT) {
	        string s = xpp->readContent();
	        cout << "CONTENT={'" << s << "'}" << endl;  
	      } else if(type == XmlPullParser::END_TAG) {
	        xpp->readEndTag(etag);
	        cout << etag << endl;  //.toString()
	      } else if(type == XmlPullParser::START_TAG) {
	        xpp->readStartTag(stag);
	        cout << stag << endl;  //.toString()
	      }        
	    }

      cout << "================ END PARSING" << endl;

     } //else {
      
       int runs = 10;
       if(speedTest) {
         runs =  10000000 / bufSize + 2;  //parse 10M charcters
         if(verbose) cout << "start speed test runs=" << runs << endl;
       }
       const long startMillis = GetTickCount();
       for(int i = 0; i  < runs; ++i) {

         elementCount = 0;
      	 attrCount = 0;
      	 spaceCount = 0;
      	 characterCount = 0;

         xpp->setInput(buf, bufSize);
	        

	        while((type = xpp->next()) != XmlPullParser::END_DOCUMENT) {
	          if(type == XmlPullParser::CONTENT) {
	            //string s = xpp->readContent();
	            //characterCount += s.size();
	            const char *s = xpp->readContent();
	            characterCount += strlen(s);
	            //cout << "CONTENT={'" << s << "'}" << endl;  
	            //} else if(type == XmlPullParser::END_TAG) {
	            //xpp->readEndTag(etag);
	            //cout << etag.toString() << endl;;  
	          } else if(type == XmlPullParser::START_TAG) {
	            xpp->readStartTag(stag);
	            ++elementCount;
	            attrCount += stag.getLength(); 
	            //cout << stag.toString() << endl;  
	          }        
	        }
        }

        const long endMillis = GetTickCount();
        walltime = (double)(endMillis - startMillis);
        duration = walltime / runs;
      //}
        
      delete xpp;
      xpp = NULL;
      free((void *)buf);
	    buf = NULL;		

    } catch (const XmlPullParserException& e) {
        cerr << "\nPull Parser error when parsing: '" << xmlFile << "'\n"
            << "Exception message is:  \n"
            << e.getMessage() << "\n" << endl;
        return 3;
    } catch (const exception& e) {     //catch (exception e)

        cerr << "\nError during parsing: '" << xmlFile << "'\n"
            << "Exception message is:  \n"
            << e.what() << "\n" << endl;
        return 4;
    } catch (...) {
        cerr << "\nUnexpected exception during parsing: '" 
             << xmlFile << "'\n";
        return 5;
    }

    
    // Print out the stats that we collected and time taken
    //if(!trace)
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
