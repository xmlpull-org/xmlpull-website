DEPTH = .
TOP = $(DEPTH)
MACH := $(shell uname)
SRC=../..

#
# GENERIC
#

INCLUDES=-I$(SRC)
LIBS= 
DEFINES = -D${MACH}

#
# IRIX64
#
ifeq ($(strip $(MACH)),IRIX64)
CPP=/usr/bin/CC 
CPPFLAGS = -g -DDEBUG -LANG:std -brief_diagnostics  $(DEFINES) $(INCLUDES)
RELEASE_CPPFLAGS =  -O3 -DNDEBUG -LANG:std -brief_diagnostics  $(DEFINES) $(INCLUDES)
endif

#
# LINUX
#
ifeq ($(strip $(MACH)),Linux)
#CPPFLAGS = -D_GNU_SOURCE -pthread -DDEBUG -D_REENTRANT -g $(DEFINES) $(INCLUDES)
#CPP = /usr/bin/g++
CPP = g++
CPPFLAGS = -g -DDEBUG -LANG:std -D_GNU_SOURCE $(DEFINES) $(INCLUDES)
RELEASE_CPPFLAGS = -O3 -DNDEBUG -LANG:std -D_GNU_SOURCE $(DEFINES) $(INCLUDES)
endif

#
# SUNOS
#
ifeq ($(strip $(MACH)),SunOS)
#CPP = /opt/SUNWspro/bin/CC
# -xarch=v9a file:/opt/SUNWspro/READMEs/c++ file:/opt/SUNWspro/DOC5.0/lib/locale/C/html/index.html
#CXXFLAGS = -fast -LANG:std  $(DEFINES) $(INCLUDES)
# highest -O3 form GNU gcc info page
CPP=g++
CPPFLAGS= -g -DDEBUG -LANG:std $(DEFINES) $(INCLUDES)
RELEASE_CPPFLAGS = -O3 -DNDEBUG -LANG:std $(DEFINES) $(INCLUDES)
endif


#
# Compile sample application - counting XML elements by XML tokenizer and parser
#


all: SXTCount_$(MACH) XPPCount_$(MACH)


release: rel

rel: SXTCountRel_$(MACH) XPPCountRel_$(MACH)


SXT_DEPS = $(SRC)/sxt/XmlTokenizer.h $(SRC)/sxt/XmlTokenizerException.h

XPP_DEPS = $(SRC)/xpp/EndTag.h $(SRC)/xpp/StartTag.h $(SRC)/xpp/XmlPullParser.h \
	$(SRC)/xpp/XmlPullParserException.h


SXTCount_$(MACH): $(SXT_DEPS) SXTCount.cpp
	$(CPP) -o $@ $(CPPFLAGS) SXTCount.cpp $(LDFLAGS) $(LIBS)

XPPCount_$(MACH): $(SXT_DEPS) $(XPP_DEPS) XPPCount.cpp
	$(CPP) -o $@ $(CPPFLAGS) XPPCount.cpp $(LDFLAGS) $(LIBS)

SXTCountRel_$(MACH): $(SXT_DEPS) SXTCount.cpp
	$(CPP) -o $@ $(RELEASE_CPPFLAGS) SXTCount.cpp $(LDFLAGS) $(LIBS)

XPPCountRel_$(MACH): $(SXT_DEPS) $(XPP_DEPS) XPPCount.cpp
	$(CPP) -o $@ $(RELEASE_CPPFLAGS) XPPCount.cpp $(LDFLAGS) $(LIBS)


clean:
	/bin/rm -rf *.o SXTCountRel_* SXTCount_* XPPCountRel_* XPPCount_* ii_files SunWS_cache core
