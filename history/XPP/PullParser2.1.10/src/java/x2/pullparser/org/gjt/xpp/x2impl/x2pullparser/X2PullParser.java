/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: X2PullParser.java,v 1.14 2003/04/06 00:04:03 aslom Exp $
 */

package org.gjt.xpp.x2impl.x2pullparser;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.gjt.xpp.XmlEndTag;
import org.gjt.xpp.XmlNode;
import org.gjt.xpp.XmlPullParser;
import org.gjt.xpp.XmlPullParserBufferControl;
import org.gjt.xpp.XmlPullParserEventPosition;
import org.gjt.xpp.XmlPullParserException;
import org.gjt.xpp.XmlStartTag;


// TODO: more efficient text normalization (see how xercerce SAX2...)
// TODO: allow to disable mixed content
// TODO: investingate how to retrieve event location in input stream

/**
 * This is Xerces 2 driver that uses XNI pull parsing capabilities to
 * implement XML Pull Parser API.
 *
 * Advantages:<ul>
 * <li>uses Xerces 2 and bases in stable and standard compliant parser
 * <li>uses Xerces 2 XNI in pull parser mode but hides complexity
 *    of working with XNI with simple PullParser API
 * </ul>
 *
 * Limitations: <ul>
 * <li>this is alpha version - may have still bugs :-)
 * </ul>
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class X2PullParser //extends XMLDocumentParser
    implements XmlPullParser, XmlPullParserEventPosition, XmlPullParserBufferControl,
    XMLErrorHandler, XMLDocumentHandler
{
    private final static boolean DEBUG = false;
    private final static boolean PRINT_ERROR = false;
    private final static boolean TRACE_SIZING = false;

    // --- to access Xerces 2 resources

    protected static final String NAMESPACES_FEATURE_ID
        = "http://xml.org/sax/features/namespaces";

    protected static final String NAMESPACE_PREFIXES_FEATURE_ID
        = "http://xml.org/sax/features/namespace-prefixes";

    protected XMLPullParserConfiguration pullParserConfiguration;
    //protected XMLParserConfiguration pullParserConfiguration;

    protected XMLLocator locator;

    // reused when parising as value container
    protected QName attrQName = new QName();


    // used handling problem of not evaluating setFeature after setInputSource
    //    --> deferring setInputSource as  long as possible!!!

    protected boolean needToSetInput;
    protected CumulativeReader cumulativeReader;
    protected XMLInputSource inputSource;

    protected boolean shrinkable = true;

    // --- parser parameters

    /** Have we read empty element? */
    protected boolean emptyElement;

    ///** just to make that empty element content must be emitted */
    //protected boolean emptyElementContent;

    // mapping namespace prefixes to uri
    /** should parser support namespaces? */
    protected boolean supportNs;

    /** should parser report namespace xmlns* attributes ? */
    protected boolean reportNsAttribs;

    // handling mixed content
    protected boolean allowedMixedContent = true;

    //------------------------------------------------------
    // --- parser state

    protected Exception seenException;

    protected boolean disableOffsetTracking;

    protected int contentEventStart;
    protected int contentEventEnd;
    /** start position of current event in tokenizer biffer */
    protected int eventStart;
    /** end position of current event in tokenizer biffer */
    protected int eventEnd;

    /** Have we seen root element */
    protected boolean seenRootElement;
    /** Content of current element if in CONTENT state */
    //protected String elContent;
    protected StringBuffer contentBuf = new StringBuffer();


    /** what is current event type as returned from next()? */
    protected byte state;

    /** mapping of names prefixes to uris */
    protected Hashtable prefix2Ns = new Hashtable();

    /** index for last attribute in attrPos array */
    protected int attrPosEnd;
    /** size of attrPos array */
    protected int attrPosSize;

    /** temporary array of current attributes */
    protected X2Attribute attrPos[];

    // for validating element pairing and string namespace context
    /** how many elements are on elStack */
    protected int elStackDepth;
    /** size of elStack array */
    protected int elStackSize;
    /** temprary array to keep ElementContent stack */
    protected X2ElementContent[] elStack;

    // not used as chnaging state != -1 means that callback was called - see next()
    //protected boolean xniCallbackCalled = false;

    protected boolean startTagInitialized = false;

    protected boolean seenContent = false;
    protected boolean gotContent = false;

    protected boolean nonWhitespaceContent = false;

    protected boolean seenCR = false;

    protected boolean mixInElement = false;

    protected byte nextState;

    /**
     * Create instance of pull parser.
     */
    public X2PullParser() throws XmlPullParserException {
        //super(new StandardParserConfiguration());
        // hacking to overcome constrctor initialization ...
        //pullParserConfiguration = (StandardParserConfiguration) fConfiguration;
        pullParserConfiguration = new StandardParserConfiguration();
        pullParserConfiguration.setDocumentHandler(this);
        pullParserConfiguration.setErrorHandler(this);
        pullParserConfiguration.setFeature(
            "http://apache.org/xml/features/continue-after-fatal-error", true);
        //DocumentTracer tracer =
        //  new DocumentTracer(pullParserConfiguration);
        setNamespaceAware(false);
    }

    // -- privae debug methods

    private static void debug(String msg) { debug(msg, null); }

    private static void debug(String msg, Exception ex)
    {
        if(!DEBUG) {
            throw new RuntimeException(
                "only when DEBUG enabled can print messages");
        }
        System.err.println("X2PP: "+msg+(ex != null ? " "+ex.getMessage() : "") );
        if(ex != null) ex.printStackTrace();
    }

    private static void error(String msg, Exception ex)
    {
        if(!DEBUG && !PRINT_ERROR) {
            throw new RuntimeException(
                "only when DEBUG or REPORT_ERROR enabled can print messages");
        }
        System.err.println("X2PP ERROR: "+msg+(ex != null ? " "+ex.getMessage() : "") );
        if(ex != null) ex.printStackTrace();
    }
    //
    // --- XMLErrorHandler methods

    /** Warning. */
    public void warning(String domain, String key, XMLParseException ex)
        throws XNIException
    {
        if(PRINT_ERROR || DEBUG) error("XMLErrorHandler warning()", ex);
    }

    /** Error. */
    public void error(String domain, String key, XMLParseException ex)
        throws XNIException
    {
        if(PRINT_ERROR || DEBUG) error("XMLErrorHandler error()", ex);
    } // error(String,String,XMLParseException)

    /** Fatal error. */
    public void fatalError(String domain, String key, XMLParseException ex)
        throws XNIException
    {
        if(PRINT_ERROR || DEBUG) error("XMLErrorHandler fatalError()", ex);
        //throw ex;
        seenException = ex;
    } // fatalError(String,String,XMLParseException)

    //
    // --- XMLDocumentHandler methods
    //
    /** Document source*/
    protected XMLDocumentSource fDocumentSource;

    /** Sets the document source */
    public void setDocumentSource(XMLDocumentSource source){
        fDocumentSource = source;
    } // setDocumentSource

    /** Returns the document source */
    public XMLDocumentSource getDocumentSource (){
        return fDocumentSource;
    } // getDocumentSource

    public void startDocument(XMLLocator locator, String encoding,
                              NamespaceContext namespaceContext, Augmentations augs)
        throws XNIException
    {
        //TODO: check if we *can* safely ignore namespaceContext ???
        startDocument(locator, encoding, augs);
    }

    public void startDocument(XMLLocator locator, String encoding, Augmentations augs)
        throws XNIException
    {
        this.locator = locator;
        if(DEBUG) debug("startDocument locator="+locator);
        seenRootElement = false;
        startTagInitialized = false;
        seenContent = false;
        nonWhitespaceContent = false;
    }

    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException
    {}
    public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
        throws XNIException
    {}
    public void comment(XMLString text, Augmentations augs) throws XNIException
    {}

    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException
    {}
    public void startGeneralEntity(String name,
                                   XMLResourceIdentifier identifier,
                                   String encoding,
                                   Augmentations augs) throws XNIException {
    } // startGeneralEntity(String,XMLResourceIdentifier,String,Augmentations)
    public void endGeneralEntity(String name, Augmentations augs)
        throws XNIException {
    } // endGeneralEntity(String,Augmentations)

    private void initializeStartTag() {
        ensureCapacity(elStackDepth + 1);
        X2ElementContent el = elStack[elStackDepth];
        el.prefixesEnd = 0;
        el.defaultNs = null;

        attrPosEnd = 0;
        state = START_TAG;

        //contentBuf.setLength(0);

        startTagInitialized = true;
        //seenContent = false;
        gotContent = false;
    }

    public void startPrefixMapping(String prefix, String uri, Augmentations augs)
        throws XNIException
    {
        if(state != -1 && state != START_TAG)
            throw new XNIException("unexcpected state="+state);
        if(!startTagInitialized) {
            initializeStartTag();
        }
        X2ElementContent el = elStack[elStackDepth];

        //if(attrPosEnd >= attrPosSize) ensureAttribs(attrPosEnd + 1);
        //X2Attribute ap = attrPos[attrPosEnd];

        //TODO check me
        if(prefix == null || "".equals(prefix)) {
            if(el.defaultNs != null) {
                throw new XNIException(
                    "default namespace was alredy declared by xmlns attribute");
            }
            if(DEBUG) debug("adding default uri="+uri);
            el.defaultNs = uri;
            //                      if(reportNsAttribs) {
            //                              ap.xmlnsAttrib = true;
            //                              ++attrPosEnd;
            //                      }
        } else {
            if(el.prefixesEnd >= el.prefixesSize) {
                el.ensureCapacity(el.prefixesEnd);
            }
            el.prefixes[el.prefixesEnd] = prefix;
            el.namespaceURIs[el.prefixesEnd] = uri;
            el.prefixPrevNs[el.prefixesEnd] =
                (String) prefix2Ns.get(prefix);
            ++el.prefixesEnd;
            if(DEBUG) debug("adding prefix="+prefix+" uri="+uri);
            prefix2Ns.put(prefix, uri);
            //                      if(reportNsAttribs) {
            //                              ap.xmlnsAttrib = true;
            //                              ++attrPosEnd;
            //                      }
        }
    }

    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException
    {
        if(state != -1 && state != START_TAG) {
            throw new XNIException("unexcpected state="+state);
        }
        //xniCallbackCalled = true;
        if(!startTagInitialized) {
            initializeStartTag();
            emptyElement = false;
        }
        startTagInitialized = false;

        if(allowedMixedContent == false && nonWhitespaceContent) {
            throw new XNIException(
                "mixed content is not allowed (content='"+escape(contentBuf.toString())+"'"
                    +" mixed with element "+element+")");
        }
        nonWhitespaceContent = false;
        mixInElement = true;


        extractEventPosition(augs);
        //        if(element instanceof PositionedQName) {
        //          PositionedQName pqname = (PositionedQName) element;
        //          eventStart = pqname.posAbsoluteStart;
        //          eventEnd = pqname.posAbsoluteEnd;
        //        }

        X2ElementContent el = elStack[elStackDepth];
        //eventStart = tokenizer.posStart - 1;
        if(elStackDepth >= elStackSize) {
            ensureCapacity(elStackDepth);
        }

        el.qName = element.rawname;
        if(supportNs) {
            el.localName = element.localpart;
            el.prefix = element.prefix;
            el.uri = element.uri;
        } else {
            el.localName = el.qName;
            el.prefix = null;
            el.uri = null; //TODO is it correct for non namespaced?
        }
        //++elStackDepth;


        // process all attributes
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            attributes.getName(i, attrQName);
            //TODO better how xmlns and xmlns: attribs are handled?
            boolean xmlnsAttrib = false;
            if(supportNs) {
                xmlnsAttrib = ("xmlns".equals(attrQName.rawname)
                                   || attrQName.rawname.startsWith("xmlns:"));
                if(xmlnsAttrib && reportNsAttribs == false) {
                    continue; // skip NS attrib
                }
            }
            if(attrPosEnd >= attrPosSize) ensureAttribs(attrPosEnd + 1);
            X2Attribute ap = attrPos[attrPosEnd];
            ap.qName = attrQName.rawname;
            ap.xmlnsAttrib = xmlnsAttrib;
            ap.prefix = attrQName.prefix;
            ap.localName = attrQName.localpart;
            ap.uri = attrQName.uri;
            //String attrType = attributes.getType(i);
            ap.value = attributes.getValue(i);
            ++attrPosEnd;
        }

        if(el.defaultNs == null) {
            if(elStackDepth > 0) {
                el.defaultNs = elStack[elStackDepth - 1].defaultNs;
            } else {
                el.defaultNs  = "";
            }
        }
        //el.defaultNs = element.uri;

        if(DEBUG) debug("startTag() adding element el="+el+getPosDesc());

        //throw new XNIException("test");
    }

    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException
    {
        if(DEBUG) debug("emptyElement() called for "+element);
        // state may be already START_TAG when startPrefixMapping was called before...
        if(state != -1 && state != START_TAG) {
            throw new XNIException("unexpected state="+state);
        }
        startElement(element, attributes, augs);
        emptyElement = true;
        if(DEBUG) debug("emptyElement() exit for "+element);
        //endElement(element);
    }

    public void startEntity(String name,
                            String publicId, String systemId,
                            String baseSystemId,
                            String encoding,
                            Augmentations augs) throws XNIException
    {}
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException
    {}
    public void endEntity(String name, Augmentations augs) throws XNIException
    {}

    private void addNormalizedContent(XMLString text) {
        for (int i = text.offset; i < text.offset + text.length; i++)
        {
            char c = text.ch[i];
            if(c == '\r') {
                seenCR = true;
                contentBuf.append('\n');
            } else if(c == '\n') {
                if(seenCR == false) {
                    contentBuf.append('\n');
                }
                seenCR = false;
            } else if(c == '\t' || c == ' ') {
                seenCR = false;
                contentBuf.append(c);
            } else {
                // S as define by [3] in http://www.w3.org/TR/2000/REC-xml-20001006#NT-S
                nonWhitespaceContent = true;
                seenCR = false;
                contentBuf.append(c);
            }
        }
        //contentBuf.append(text.ch, text.offset, text.length);
    }

    private static String state(byte state) {
        if(state == END_DOCUMENT) {
            return "END_DOCUMENT";
        } else if(state == END_TAG) {
            return "END_TAG";
        } else if(state == START_TAG) {
            return "START_TAG";
        } else if(state == CONTENT) {
            return "CONTENT";
        } else {
            return "UNKNONW_EVENT ("+state+")";
        }
    }

    private static String escape(String s) {
        StringBuffer buf = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(c == '\n') {
                buf.append("\\n");
            } else if(c == '\r') {
                buf.append("\\r");
            } else if(c == '\t') {
                buf.append("\\t");
            } else if(c == '\\') {
                buf.append("\\");
            } else if(c == '"') {
                buf.append('"');
            } else if(c < 32) {
                buf.append("\\x"+Integer.toHexString(c));
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    //private Object[] noParams = new Object[0];

    private int getCurrentEntityAbsoluteOffset() {
        int pos = -1;
        if(locator != null && ! disableOffsetTracking ) {
            try {
                // use reflection to invoke:
                //  pos = locator.getCurrentEntityAbsoluteOffset();
                Class klass = locator.getClass();
                java.lang.reflect.Method m = klass.getMethod("getCurrentEntityAbsoluteOffset", null);
                Integer i = (Integer) m.invoke(locator, null);
                pos = i.intValue();
            } catch (Exception e) {
                disableOffsetTracking = true;
                if(DEBUG) debug("disabled offset tracking", e);
            }
        }
        return pos;
    }

    public void characters(XMLString text, Augmentations augs) throws XNIException
    {
        if(state != -1)  {// && state != CONTENT)
            throw new XNIException("unexpected state="+state);
        }
        //xniCallbackCalled = true;
        //state = CONTENT;
        if(DEBUG) debug("content='"+escape(new String(text.ch, text.offset, text.length)+"'"));
        seenContent = true;
        gotContent = false;
        addNormalizedContent(text);
        contentEventEnd = getCurrentEntityAbsoluteOffset();
        if(DEBUG) debug("added content '"+escape(text.toString())+"'");
    }

    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException
    {
        characters(text, augs);
        //        if(state != -1) // && state != CONTENT)
        //          throw new XNIException("unexpected state="+state);
        //        //state = CONTENT;
        //        seenContent = true;
        //        addNormalizedContent(text);
        //        if(DEBUG) debug("added whitespace '"+escape(text.toString())+"'");
    }

    private void endElement() {
        if(elStackDepth < 1)  { // should never happen...
            throw new XNIException(
                "end tag without start stag");
        }
        if(allowedMixedContent == false && !mixInElement) {
            // seen end tag before;
            if(seenContent && nonWhitespaceContent) {
                throw new XNIException(
                    "mixed content is not allowed (content='"+escape(contentBuf.toString())+"'"
                        +" before end tag unles it is element content");
            }
        }
        if(seenContent) {
            gotContent = true;
        }
        X2ElementContent el = elStack[elStackDepth-1];
        if(DEBUG) debug("end element el="+el);
        // restore declared namespaces
        if(supportNs && el.prefixes != null) {
            //NOTE: it is in REVERSE order!
            for(int i = el.prefixesEnd - 1; i >= 0; --i) {
                //System.err.println("el="+el);
                if( el.prefixPrevNs[i] != null) {
                    prefix2Ns.put( el.prefixes[i], el.prefixPrevNs[i] );
                } else {
                    prefix2Ns.remove( el.prefixes[i] );
                }
            }
            // el.prefixesEnd = 0;  // this would prevent readNamesapces* from working
        }
    }

    // Augmentations item names that can be used in patched Xerces2 to retrieve
    //          position of event for start and end tag
    public final String POS_ABSOLUTE_START =  "http://gjt.org/xpp/pos-absolute-start"; //ALEK
    public final String POS_ABSOLUTE_END =  "http://gjt.org/xpp/pos-absolute-end"; //ALEK

    public void endElement(QName element, Augmentations augs) throws XNIException
    {
        if(state != -1) throw new XNIException("unexcpected state="+state);
        //xniCallbackCalled = true;
        state = END_TAG;
        extractEventPosition(augs);
        endElement();
    }

    private void extractEventPosition(Augmentations augs)
    {
        //        if(element instanceof PositionedQName) {
        //            PositionedQName pqname = (PositionedQName) element;
        //            eventStart = pqname.posAbsoluteStart;
        //            eventEnd = pqname.posAbsoluteEnd;
        //        }
        if(augs != null && augs.getItem(POS_ABSOLUTE_START) != null) {
            eventStart = ((Integer)augs.getItem(POS_ABSOLUTE_START)).intValue();
            eventEnd = ((Integer)augs.getItem(POS_ABSOLUTE_END)).intValue();
        }
    }

    public void endPrefixMapping(String prefix, Augmentations augs) throws XNIException
    {
        // useless as does not contain previous uri to be restored ...
    }

    public void startCDATA(Augmentations augs) throws XNIException
    {}
    public void endCDATA(Augmentations augs) throws XNIException
    {}

    public void endDocument(Augmentations augs) throws XNIException {
        if(state != -1) throw new XNIException("unexcpected state="+state);
        //xniCallbackCalled = true;
        state = END_DOCUMENT;
        if(elStackDepth > 0) {
            throw new XNIException(
                "expected element end tag '"
                    +elStack[elStackDepth-1].qName+"' not end of document"
            );
        }
    } // endDocument()


    private static int step;

    // -- key method

    /**
     * This is a key method - translates XNI callbacks
     * into XPP events
     * (such as START_TAG, END_TAG, CONTENT).
     * or END_DOCUMENT if no more input.
     *
     */

    public byte next() throws XmlPullParserException, IOException {
        if(inputSource == null) {
            throw new XmlPullParserException(
                "setInput must be called before can start parsing");
        }
        if(needToSetInput) {
            needToSetInput = false;
            try {
                pullParserConfiguration.setInputSource(inputSource);
            } catch(IOException ex) {
                throw new XmlPullParserException(
                    "could not set input to reader", ex);
            }
        }
        if(state == CONTENT && seenContent && nextState != -1) {
            //seenContent = false;
            //assert previousState != CONTENT
            state = nextState;
            nextState = -1;
            gotContent = false;
            //contentBuf.setLength(0);
            if(DEBUG) debug("# END returned already CONTENT and now returning state="+state(state));
        } else if(emptyElement) {
            emptyElement = false;
            state = END_TAG;
            eventStart = eventEnd;
            nextState = -1;
            if(DEBUG) debug("# END empty element state="+state+" nextState="+state(nextState));
            endElement();
            gotContent = false;
        } else {
            // nothing available - pull some input from Xerces 2 ...
            try {
                nextState = state = -1;
                contentBuf.setLength(0);
                seenContent = false;
                seenCR = false;
                nonWhitespaceContent = false;
                eventStart = eventEnd = -1;
                contentEventStart = getCurrentEntityAbsoluteOffset();
                seenException = null;
                while(seenException == null) {
                    if(DEBUG) debug("# step["+(++step)+"]");
                    while(state == -1) {
                        if(pullParserConfiguration.parse(false) == false) {
                            // parsing now finished but may still be some state left...
                            if(state == -1) {
                                state = END_DOCUMENT;
                            }
                        }
                    }
                    //eventEnd = locator.getCurrentEntityAbsoluteOffset();
                    if(state != -1) {
                        if(seenContent) {
                            // skip whitespace content when no mixed content allowed
                            if(!allowedMixedContent) {
                                if(DEBUG) debug("# END skip state="+state(state)
                                                    +" mixInElement="+mixInElement
                                                    +" nonWhitespaceContent="+nonWhitespaceContent);
                                if(state == END_TAG
                                   && !mixInElement && !nonWhitespaceContent)
                                {
                                    if(DEBUG) debug("# END skip='"+escape(contentBuf.toString())+"'");
                                    break;  // skip content
                                } else if(state == START_TAG && !nonWhitespaceContent) {
                                    if(DEBUG) debug("# END skip='"+escape(contentBuf.toString())+"'");
                                    break; // skip content
                                }
                            }
                            nextState = state;
                            state = CONTENT;
                        }
                        break;
                    }
                }
            } catch(XNIException ex) {
                seenException = ex;
            }
        }
        if(state == END_TAG) {
            --elStackDepth;
            mixInElement =  false;
        } else if(state == START_TAG) {
            ++elStackDepth;
            //} else if(state == CONTENT) {
            //    eventStart = contentEventStart;
            //    eventEnd = contentEventEnd;
        }
        if(seenException != null && !gotContent)  // allow report content if it was ok
        {
            Exception ex = seenException;
            seenException = null;
            String msg = ex.getMessage();
            if(msg == null) {
                msg="";
            }
            if(msg.endsWith(".")) {
                msg = msg.substring(0, msg.length() - 1);
            }
            throw new XmlPullParserException(
                "could not parse input: "+msg+getPosDesc(), ex
            );
        }
        if(DEBUG) debug("# END state="+state(state)
                            +" nextState="+state(nextState)+" emptyElement="+emptyElement);
        if(DEBUG && state == CONTENT) debug("# END content='"+escape(contentBuf.toString())+"'");
        return state;
    }

    // -- generic methods

    /**
     * Reset parser and set new input.
     */
    public void setInput(Reader reader) throws XmlPullParserException {
        resetState();
        eventEnd = eventStart = 0;
        // sets to "" systemId to allow reporting line/column numbers ...
        cumulativeReader = new CumulativeReader(reader);
        inputSource = new XMLInputSource(null, "", null, cumulativeReader, null);
        needToSetInput = true;
    }

    /**
     * Reset parser and set new input.
     */
    public void setInput(char[] buf) throws XmlPullParserException {
        setInput(new CharArrayReader(buf));
    }

    public void setInput(char[] buf, int off, int len)
        throws XmlPullParserException
    {
        setInput(new CharArrayReader(buf, off, len));
    }

    /**
     * Reset parser state so it can be used to parse new
     */
    public void reset() {
        resetState();
    }

    public boolean isAllowedMixedContent()
    {
        return allowedMixedContent;
    }


    /*

     <p>Implementation notes: handling setAllowedMixedCotnet(...)

     <p>on very high level

     <pre>
     //easy cases:
     xxx  StartTag --> (ERROR if seenContent)
     //harder cases:
     StartTag xxx EndTag --> always OK
     EndTag xxx EndTag --> (ERRORif seenContent)
     </pre>

     <p>and this is how it is implemented modulo complexity of push callbacks ...

     <pre>
     onStartTag callback:
     if(nonWhitespaceContet) {
     ERROR;
     } else {
     IGNORE CONTENT
     }
     mixInElement = true;
     seenContent = false;
     nonWhitespaceContet = false

     onContent callback:
     seenContent = true;
     if (non whites space content) nonWhitespaceContet = true

     onEndTag callback:
     if(!mixInElement) {
     // seen end tag before;
     if(seenContent && nonWhitespaceContet) {
     ERROR
     } else {
     IGNORE CONTENT
     }
     }
     mixInElement = false
     </pre>
     */

    /**
     * Allow for mixed element content.
     * Enabled by default.
     * When disbaled element must containt either text
     * or other elements.
     */
    public void setAllowedMixedContent(boolean enable)
        throws XmlPullParserException
    {
        allowedMixedContent = enable;
    }


    public boolean isNamespaceAware() {
        return supportNs;
    }

    /**
     * Set support of namespaces. Disabled by default.
     */
    public void setNamespaceAware(boolean awareness) throws XmlPullParserException
    {
        if(elStackDepth > 0 || seenRootElement) {
            throw new XmlPullParserException(
                "namespace support can only be set when not parsing");
        }
        try {
            pullParserConfiguration.setFeature(NAMESPACES_FEATURE_ID, awareness);
        }
        catch (Exception e) {
            throw new XmlPullParserException(
                "parser does not support feature ("+NAMESPACES_FEATURE_ID+")", e);
        }
        supportNs = awareness;
    }

    public boolean isNamespaceAttributesReporting()
    {
        return reportNsAttribs;
        //throw new X2PullParserRuntimeException("not implemented");
    }

    /**
     * Make parser to report xmlns* attributes. Disabled by default.
     * Only meaningful when namespaces are enabled (when namespaces
     * are disabled all attributes are always reported).
     */
    public void setNamespaceAttributesReporting(boolean enable) throws XmlPullParserException
    {
        try {
            pullParserConfiguration.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, enable);
        }
        catch (Exception e) {
            throw new XmlPullParserException(
                "parser does not support feature ("+NAMESPACE_PREFIXES_FEATURE_ID+")", e);
        }
        reportNsAttribs = enable;
        //throw new X2PullParserRuntimeException("not implemented");
    }

    public String getNamespaceUri()
    {
        if(state != START_TAG && state != END_TAG) {
            throw new X2PullParserRuntimeException(
                "no end or start tag available to read"+getPosDesc());
        }
        X2ElementContent el = elStack[getDepth() - 1];
        return el.uri;
    }

    public String getLocalName()
    {
        if(state != START_TAG && state != END_TAG) {
            throw new X2PullParserRuntimeException(
                "no end or start tag available to read"+getPosDesc());
        }
        X2ElementContent el = elStack[getDepth() - 1];
        return el.localName;
    }

    public String getPrefix()
    {
        if(state != START_TAG && state != END_TAG) {
            throw new X2PullParserRuntimeException(
                "no end or start tag available to read"+getPosDesc());
        }
        X2ElementContent el = elStack[getDepth() - 1];
        return el.prefix;
    }

    public String getRawName()
    {
        if(state != START_TAG && state != END_TAG) {
            throw new X2PullParserRuntimeException(
                "no end or start tag available to read"+getPosDesc());
        }
        X2ElementContent el = elStack[getDepth() - 1];
        return el.qName;
    }

    public String getQNameLocal(String qName) {
        int i = qName.lastIndexOf(':');
        return qName.substring(i + 1);
    }

    public String getQNameUri(String qName)
        throws XmlPullParserException
    {
        if(elStackDepth == 0) {
            throw new XmlPullParserException(
                "parsing must be started to get uri from qname");
        }
        int i = qName.lastIndexOf(':');
        if(i > 0) {
            String prefix = qName.substring(0, i);
            return (String) prefix2Ns.get(prefix);
        } else {
            return elStack[elStackDepth-1].defaultNs;
        }
    }

    public int getDepth()
    {
        return
            (state != END_TAG) ? elStackDepth : elStackDepth + 1;
    }

    public int getNamespacesLength(int depth)
    {
        if(depth <= 0)
            throw new IllegalArgumentException(
                "element depth must be bigger than zero");
        int maxDepth = getDepth();
        if(depth > maxDepth) {
            throw new IllegalArgumentException(
                "the depth "+depth+" that was passed for length of namespaces "+
                    " can not be bigger than current depth of "+maxDepth);
        }
        X2ElementContent el = elStack[ depth-1 ];
        return el.prefixesEnd;
    }

    /**
     * Return namespace prefixes for element at depth
     */
    public void readNamespacesPrefixes(int depth,
                                       String[] prefixes,
                                       int off,
                                       int len)
        throws XmlPullParserException
    {
        if(depth <= 0)
            throw new XmlPullParserException(
                "element depth must be bigger than zero");
        int maxDepth = getDepth();
        if(depth > maxDepth) {
            throw new XmlPullParserException(
                "passed prefixes array of length "+depth+
                    " can not be bigger than current depth of "+maxDepth);
        }
        X2ElementContent el = elStack[ depth-1 ];
        if(len > el.prefixesEnd) {
            throw new XmlPullParserException(
                "number of prefixes to copy "+len
                    +" is bigger than available "+el.prefixesEnd);
        }
        System.arraycopy(el.prefixes, 0, prefixes, off, len);
    }

    /**
     * Return namespace URIs for element at depth
     */
    public void readNamespacesUris(int depth,
                                   String[] uris,
                                   int off,
                                   int len)
        throws XmlPullParserException
    {
        if(depth <= 0)
            throw new XmlPullParserException(
                "element depth must be bigger than zero");
        int maxDepth = getDepth();
        if(depth > maxDepth) {
            throw new XmlPullParserException(
                "passed namespace URIs array of length "+depth+
                    " can not be bigger than current depth of "+maxDepth);
        }
        X2ElementContent el = elStack[ depth - 1 ];
        if(len > el.prefixesEnd) {
            throw new XmlPullParserException(
                "number of namespace URIs to copy "+len
                    +" is bigger than available "+el.prefixesEnd);
        }
        System.arraycopy(el.namespaceURIs, 0, uris, off, len);
    }


    private static int findFragment(int bufStart, char[] b, int start, int end) {
        //System.err.println("bufStart="+bufStart+" b="+escape(new String(b, start, end))+" start="+start+" end="+end);
        if(start < bufStart) {
            start = bufStart;
            if(start > end) start = end;
            return start;
        }
        if(end - start > 55) {
            start = end - 10; // try to find good location
        }
        int i = start + 1;
        while(--i > bufStart) {
            if((end - i) > 55) break;
            char c = b[i];
            if(c == '<' && (start - i) > 10) break;
        }
        return i;
    }


    /**
     * Return string describing current position of parser in input stream as
     * text 'at line %d (row) and column %d (colum) [seen %s...]'.
     */
    public String getPosDesc() {
        String fragment = "";
        int bufStart = 0;

        int posStart = -1;
        int posEnd =  - 1;
        try {
            if(locator != null) {
                posStart = 0;
                posEnd = getCurrentEntityAbsoluteOffset();
            }
            // try more precise location - but it may fail if Xerces2 is not patched...
            posStart = getEventStart();
            posEnd = getEventEnd();
        } catch(Exception ex) {
        }
        //System.err.println("bufStart="+bufStart+" posStart="+posStart+" posEnd="+posEnd);
        if(posStart >= 0 && posEnd >= 0 && posStart <= posEnd) {
            char[] buf = getEventBuffer();
            int start = findFragment(bufStart, buf, posStart, posEnd);
            //System.err.println("buf="+escape(new String(buf, posStart, posEnd - posStart))+" start="+start);
            if(start < posEnd) {
                fragment = new String(buf, start, posEnd - start);
                if(start > bufStart) fragment = "..." + fragment;
            }
        }
        String stateDesc = (state != -1) ? " (parser state "+state(state)+")" : "";
        String systemId = (locator != null) ? locator.getExpandedSystemId() : null;
        String publicId = (locator != null) ? locator.getPublicId() : null;
        String baseSystemId = (locator != null) ? locator.getBaseSystemId() : null;
        if("".equals(systemId)) systemId = null;
        if("".equals(baseSystemId)) baseSystemId = null;
        String input = "" + (systemId != null ? " " + systemId : "")
            +(baseSystemId != null ? " baseSystemId='"+baseSystemId+"'" : "")
            +(publicId != null ? " publicId='"+publicId+"'" : "");
        if(input.length() > 0) input = " in "+input;

        String position = "";
        if(locator != null) {
            int posRow = locator.getLineNumber();
            int posCol = locator.getColumnNumber();
            position = " at line "+posRow
                +" and column "+(posCol-0);
        }
        return position
            +(fragment != null ? " seen: "+escape(fragment)+"..." : "")
            +input+stateDesc;

    }

    public int getLineNumber() {
        //return tokenizer.getLineNumber();
        return locator != null ? locator.getLineNumber() : -1;
    }

    public int getColumnNumber() {
        //return tokenizer.getColumnNumber();
        return locator != null ? locator.getColumnNumber() : -1;
    }

    public byte getEventType()
    {
        return state;
    }

    /**
     * Return true if just read CONTENT contained only white spaces.
     */
    public boolean isWhitespaceContent() throws XmlPullParserException {
        if(state != CONTENT)
            throw new XmlPullParserException("no content available to read");
        //return tokenizer.seenContent == false;
        String content = readContent();
        for(int i = 0; i < content.length(); ++i) {
            char ch = content.charAt(i);
            if(ch != ' ' && ch != '\n' && ch != '\t' && ch != '\r') {
                return false;
            }
        }
        return true;
    }

    public int getContentLength() throws XmlPullParserException {
        if(state != CONTENT)
            throw new XmlPullParserException("no content available to read");
        return contentBuf.length();
    }

    /**
     * Return String that contains just read CONTENT.
     */
    public String readContent() throws XmlPullParserException {
        if(state != CONTENT)
            throw new XmlPullParserException("no content available to read");
        return contentBuf.toString();
    }

    /**
     * Read value of just read END_TAG into passed as argument EndTag.
     */
    public void readEndTag(XmlEndTag etag) throws XmlPullParserException
    {
        if(state != END_TAG)
            throw new XmlPullParserException(
                "no end tag available to read"+getPosDesc(),
                getLineNumber(), getColumnNumber());
        //etag.qName = elStack[elStackDepth].qName;
        //etag.uri = elStack[elStackDepth].uri;
        //etag.localName = elStack[elStackDepth].localName;
        X2ElementContent el = elStack[elStackDepth];

        etag.resetEndTag();

        etag.modifyTag(el.uri,
                       el.localName,
                       el.qName);
    }

    /**
     * Read value of just read START_TAG into passed as argument StartTag.
     */
    public void readStartTag(XmlStartTag stag)
        throws XmlPullParserException
    {
        if(state != START_TAG)
            throw new XmlPullParserException(
                "no start tag available to read"+getPosDesc(),
                getLineNumber(), getColumnNumber());
        //if(beforeAtts == false)
        //  throw new XmlPullParserException(
        //    "start tag was already read"+getPosDesc(), getLineNumber(), getColumnNumber());

        stag.resetStartTag();

        X2ElementContent el = elStack[elStackDepth - 1];
        //stag.qName = el.qName;
        //stag.uri = el.uri;
        //stag.localName = el.localName;
        stag.modifyTag(el.uri,
                       el.localName,
                       el.qName);

        // process atttributes
        stag.ensureAttributesCapacity(attrPosEnd);

        //stag.attEnd = attrPosEnd;
        for(int i = 0; i < attrPosEnd; ++i) {
            X2Attribute ap = attrPos[i];

            // place for next attribute value
            //Attribute att = stag.attArr[i];
            //att.qName =  ap.qName;
            //att.localName = ap.localName;
            //att.value = ap.value;
            //att.uri = ap.uri;
            stag.addAttribute(
                ap.uri,
                ap.localName,
                ap.qName,
                ap.value);

        }
    }

    public void readNodeWithoutChildren(XmlNode node)
        throws XmlPullParserException
    {
        readStartTag(node);
        if(supportNs) {
            X2ElementContent el = elStack[elStackDepth-1];
            node.setDefaultNamespaceUri(el.defaultNs);
            //System.err.println("adding my namespace size="+el.prefixesEnd);
            //    for(int i = 0; i < el.prefixesEnd; i++) {
            //    System.err.println("adding"+
            //      " el.prefixes["+i+"] = "+el.prefixes[i]+
            //      " el.namespaceURIs["+i+"] = "+el.namespaceURIs[i]);
            //    }
            node.addDeclaredNamespaces(
                el.prefixes, 0, el.prefixesEnd, el.namespaceURIs);
        }
        //       (state != END_TAG) ? elStackDepth : elStackDepth + 1;
    }

    public byte readNode(XmlNode node)
        throws XmlPullParserException, IOException
    {
        readNodeWithoutChildren(node);
        X2ElementContent el = elStack[elStackDepth-1];
        int level = elStackDepth;
        //byte type = PullParser.END_TAG;
        //Stack stack = new Stack();
        //stack.push(node);
        while(true) {
            byte state = next();
            switch(state) {
                case XmlPullParser.START_TAG:
                    XmlNode child = node.newNode();
                    readNodeWithoutChildren(child);
                    //System.err.println("read "+child);
                    //        readStartTag(child);
                    //        if(supportNs) {
                    //          child.setDefaultNamespaceUri(el.defaultNs);
                    //          //System.err.println("adding namespace size="+el.prefixesEnd);
                    //          child.addDeclaredNamespaces(
                    //            el.prefixes, 0, el.prefixesEnd, el.namespaceURIs);
                    //        }
                    node.appendChild(child);
                    el = elStack[elStackDepth-1];
                    el.node = node;
                    node = child;
                    break;
                case XmlPullParser.CONTENT:
                    // skip element content when it is empty string "" ...
                    //if(eventEnd > eventStart) {
                    if(DEBUG) debug("readNode() adding content "+escape(readContent()));
                    node.appendChild(readContent());
                    //}
                    break;
                case XmlPullParser.END_TAG:
                    if(elStackDepth >= level) {
                        node = elStack[elStackDepth].node;
                    } else {
                        return state;
                    }
                    break;
            }
        }
    }

    /**
     * If parser has just read start tag it allows to skip whoole
     * subtree contined in this element. Returns when encounters
     * end tag matching the start tag.
     */
    public byte skipNode() throws XmlPullParserException, IOException {
        if(state != START_TAG) {
            throw new XmlPullParserException(
                "start tag must be read before skiping subtree"+getPosDesc(),
                getLineNumber(), getColumnNumber());
        }
        int level = 1;
        byte type = XmlPullParser.END_TAG;
        while(level > 0) {
            type = next();
            switch(type) {
                case XmlPullParser.START_TAG:
                    ++level;
                    break;
                case XmlPullParser.END_TAG:
                    --level;
                    break;
            }
        }
        return type;
    }


    // low level API: interface to parser internal data - good for perf

    public int getHardLimit()
    {
        //return tokenizer.getHardLimit();
        throw new X2PullParserRuntimeException("not implemented");
    }

    public void setHardLimit(int value) throws XmlPullParserException {
        //tokenizer.setHardLimit(value);
        throw new X2PullParserRuntimeException("not implemented");
    }

    public int getSoftLimit()
    {
        //return tokenizer.getSoftLimit();
        throw new X2PullParserRuntimeException("not implemented");
    }

    public void setSoftLimit(int value) throws XmlPullParserException {
        //tokenizer.setSoftLimit(value);
        throw new X2PullParserRuntimeException("not implemented");
    }

    public int getBufferShrinkOffset()
    {
        //return tokenizer.getBufferShrinkOffset();
        //throw new X2PullParserRuntimeException("not implemented");
        return cumulativeReader.getCumulativeBufferAbsoluteEnd()
            - cumulativeReader.getCumulativeBufferEnd();
    }

    public void setBufferShrinkable(boolean shrinkable) throws XmlPullParserException
    {
        //tokenizer.setBufferShrinkable(shrinkable);
        //throw new X2PullParserRuntimeException("not implemented");
        this.shrinkable = shrinkable;
        cumulativeReader.setCumulative( ! shrinkable );
    }

    public boolean isBufferShrinkable()
    {
        //return tokenizer.isBufferShrinkable();
        //throw new X2PullParserRuntimeException("not implemented");
        return shrinkable;
    }


    public int getEventStart()
    {
        int pos = (state == CONTENT ? contentEventStart : eventStart);
        if( pos == -1) {
            throw new X2PullParserRuntimeException(
                "unpatched Xerces2 does not support event positioning"
                    +" - apply relevant patch or use patched xerces2 jar");
        }
        //throw new X2PullParserRuntimeException("not implemented");
        return pos;
    }

    // eventStart = tokenizer.pos, parsing..., eventEnd = tokenizer.pos }
    public int getEventEnd()
    {
        int pos = (state == CONTENT ? contentEventEnd : eventEnd);
        if( pos == -1) {
            throw new X2PullParserRuntimeException(
                "unpatched Xerces2 does not support event positioning"
                    +" - apply relevant patch or use patched xerces2 jar");
        }
        //throw new X2PullParserRuntimeException("not implemented");
        return pos;
        //return state == CONTENT ? contentEventEnd : eventEnd;
        //return eventEnd; //state == CONTENT ? contentBuf.length() : eventEnd;
        //throw new X2PullParserRuntimeException("not implemented");
    }

    // equivalent to tokenizer.buf ALWAYS (never tokenizer.pc)
    public char[] getEventBuffer()
    {
        //return tokenizer.buf;
        //throw new X2PullParserRuntimeException("not implemented");
        return cumulativeReader.getCumulativeBuffer();
        //return (state == CONTENT) ?
        //contentBuf.toString().toCharArray()
        //: cumulativeReader.getCumulativeBuffer();
    }


    // ====== utility methods
    /**
     * Make sure that we have enough space to keep element stack if passed size.
     */
    protected void ensureCapacity(int size) {
        int newSize = 2 * size;
        if(newSize == 0)
            newSize = 8; // = lucky 7 + 1 //25
        if(elStackSize < newSize) {
            if(TRACE_SIZING) {
                System.err.println("elStack "+elStackSize+" ==> "+newSize);
            }
            X2ElementContent[] newStack = new X2ElementContent[newSize];
            if(elStack != null) {
                System.arraycopy(elStack, 0, newStack, 0, elStackSize);
            }
            for(int i = elStackSize; i < newSize; ++i) {
                newStack[i] = new X2ElementContent();
            }
            elStack = newStack;
            elStackSize = newSize;
        }
    }

    /**
     * Make sure that in attributes temporary array is enough space.
     */
    protected  void ensureAttribs(int size) {
        int newSize = 2 * size;
        if(newSize == 0)
            newSize = 8; // = lucky 7 + 1 //25
        if(attrPosEnd < newSize) {
            if(TRACE_SIZING) {
                System.err.println("attrPos "+attrPosSize+" ==> "+newSize);
            }
            X2Attribute[] newAttrPos = new X2Attribute[newSize];
            if(attrPos != null) {
                System.arraycopy(attrPos, 0, newAttrPos, 0, attrPosSize);
            }
            for(int i = attrPosSize; i < newSize; ++i) {
                newAttrPos[i] = new X2Attribute(); //inner classes are weird  :-)
            }
            attrPos = newAttrPos;
            attrPosSize = newSize;
        }
    }

    protected void resetState() {
        //tokenizer.paramNotifyDoctype = true;
        state = -1;
        nextState = -1;
        eventStart = -1;
        elStackDepth = 0;
        prefix2Ns.clear();
        // 4. NC: Prefix Declared
        prefix2Ns.put("xml", "http://www.w3.org/XML/1998/namespace");
        //beforeAtts = false;
        //emptyElementContent =
        emptyElement = false;
        seenRootElement = false;
        //scache = new StringCache();
        needToSetInput = false;
    }


    protected class CumulativeReader extends Reader
    {
        private Reader source;

        private boolean cumulative;
        //private char[] cumulativeBuf;

        private int bufAbsoluteStart;
        private int bufAbsoluteEnd;

        private char[] buf = new char[10 * 1024];
        private int bufStart;
        private int bufEnd;



        /** Constructs this reader from another reader. */
        public CumulativeReader(Reader reader) {
            source = reader;
        }

        public void setCumulative(boolean value) {
            cumulative = value;
            if(cumulative) {
                char[] newBuf = new char[buf.length + 10 * 1024];
                if(bufEnd > bufStart) {
                    System.arraycopy(buf, bufStart, newBuf, 0, bufEnd - bufStart);
                }
                bufEnd = bufEnd - bufStart;
                bufStart = 0;
            }
        }
        public boolean getCumulative() { return cumulative; }

        public char[] getCumulativeBuffer() {
            return buf;
        }

        public int getCumulativeBufferAbsoluteStart() {
            return bufAbsoluteStart;
        }

        public int getCumulativeBufferAbsoluteEnd() {
            return bufAbsoluteEnd;
        }

        public int getCumulativeBufferStart() {
            return bufStart;
        }

        public int getCumulativeBufferEnd() {
            return bufEnd;
        }

        //
        // Reader methods
        //


        // ignore closing
        public void close() { }

        public int read(char[] ch, int offset, int length)
            throws IOException
        {

            // read form original
            int ret = source.read(ch, offset, length);

            if(ret > 0) {
                if(!cumulative) {
                    buf = ch;
                    bufStart = offset;
                    bufEnd = offset + ret;
                    bufAbsoluteStart = bufAbsoluteEnd;
                } else {
                    // append ch to buf at bufAbsoluteEnd
                    int newLen = bufEnd + length;
                    if(buf.length < newLen) {
                        char[] newBuf = new char[newLen + 10 * 1024];
                        System.arraycopy(buf, bufStart, newBuf, 0, bufEnd - bufStart);
                        bufEnd = bufEnd - bufStart;
                        bufStart = 0;
                    }
                    System.arraycopy(ch, offset, buf, bufEnd, ret);
                    bufEnd += ret;
                }
                bufAbsoluteEnd += ret;
            }

            return ret;

        }

    }

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


