/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/

package org.gjt.xpp.sax2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

// not J2ME classes
import java.net.URL;
import java.net.MalformedURLException;


// not J2ME classes
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.gjt.xpp.*;

public class Driver implements Locator, XMLReader, Attributes
{
    
    protected static final String DECLARATION_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/declaration-handler";
    
    protected static final String LEXICAL_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/lexical-handler";
    
    protected static final String NAMESPACES_FEATURE =
        "http://xml.org/sax/features/namespaces";
    
    protected static final String NAMESPACE_PREFIXES_FEATURE =
        "http://xml.org/sax/features/namespace-prefixes";
    
    protected static final String VALIDATION_FEATURE =
        "http://xml.org/sax/features/validation";
    
    protected static final String APACHE_SCHEMA_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/schema";
    
    protected static final String APACHE_DYNAMIC_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/dynamic";
    
    protected ContentHandler contentHandler;
    protected ErrorHandler errorHandler;
    
    protected String systemId;
    
    protected XmlPullParser pp;
    protected XmlEndTag etag;
    protected XmlStartTag stag;
    
    private final static boolean DEBUG = false;
    
    // use in parse sub-tree - exposed to resue more efficiently
    private char[] buf = new char[1024];
    private String[] namespaces = new String[5];
    private String[] prefixes = new String[5];
    
    /**
     */
    public Driver() throws XmlPullParserException {
        contentHandler = new DefaultHandler();
        errorHandler = new DefaultHandler();
        
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        pp = factory.newPullParser();
        etag = factory.newEndTag();
        stag = factory.newStartTag();
    }
    
    // -- Attributes interface
    
    public int getLength() { return stag.getAttributeCount(); }
    public String getURI(int index) { return stag.getAttributeNamespaceUri(index); }
    public String getLocalName(int index) { return stag.getAttributeLocalName(index); }
    public String getQName(int index) { return stag.getAttributeRawName(index); }
    public String getType(int index) { return "CDATA"; }
    public String getValue(int index) { return stag.getAttributeValue(index); }
    
    public int getIndex(String uri, String localName) {
        for (int i = 0; i < stag.getAttributeCount(); i++)
        {
            if(stag.getAttributeNamespaceUri(i).equals(uri)
               && stag.getAttributeLocalName(i).equals(localName))
            {
                return i;
            }
            
        }
        return -1;
    }
    
    public int getIndex(String qName) {
        for (int i = 0; i < stag.getAttributeCount(); i++)
        {
            if(stag.getAttributeRawName(i).equals(qName))
            {
                return i;
            }
            
        }
        return -1;
    }
    
    public String getType(String uri, String localName) { return "CDATA"; }
    public String getType(String qName) { return "CDATA"; }
    public String getValue(String uri, String localName) {
        return stag.getAttributeValueFromName(uri, localName);
    }
    public String getValue(String qName) {
        return stag.getAttributeValueFromRawName(qName);
    }
    
    // -- Locator interface
    
    public String getPublicId() { return null; }
    public String getSystemId() { return systemId; }
    public int getLineNumber() { return pp.getLineNumber(); }
    public int getColumnNumber() { return pp.getColumnNumber(); }
    
    // --- XMLReader interface
    
    public boolean getFeature (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(NAMESPACES_FEATURE.equals(name)) {
            return pp.isNamespaceAware();
        } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
            return pp.isNamespaceAttributesReporting();
        } else if(VALIDATION_FEATURE.equals(name)) {
            return false;
        } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
            return false;
        } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
            return false;
        } else {
            throw new SAXNotRecognizedException("unrecognized feature "+name);
        }
    }
    
    public void setFeature (String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        try {
            if(NAMESPACES_FEATURE.equals(name)) {
                pp.setNamespaceAware(value);
            } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
                pp.setNamespaceAttributesReporting(value);
            } else if(VALIDATION_FEATURE.equals(name)) {
                if(true == value) {
                    throw new SAXNotSupportedException("validation is not supported");
                }
            } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
                // can ignore as validation must be false ...
                //              if(true == value) {
                //                  throw new SAXNotSupportedException("schema validation is not supported");
                //              }
            } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
                if(true == value) {
                    throw new SAXNotSupportedException("dynamic validation is not supported");
                }
            } else {
                throw new SAXNotRecognizedException("unrecognized feature "+name);
            }
        } catch(XmlPullParserException ex) {
            throw new SAXNotSupportedException("problem with setting feature "+name+": "+ex);
        }
    }
    
    public Object getProperty (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else {
            throw new SAXNotRecognizedException("not recognized get property "+name);
        }
    }
    
    public void setProperty (String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        //
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
        } else {
            throw new SAXNotRecognizedException("not recognized set property "+name);
        }
    }
    
    public void setEntityResolver (EntityResolver resolver) {}
    
    public EntityResolver getEntityResolver () { return null; }
    
    public void setDTDHandler (DTDHandler handler) {}
    
    public DTDHandler getDTDHandler () { return null; }
    
    public void setContentHandler (ContentHandler handler)
    {
        this.contentHandler = handler;
    }
    
    public ContentHandler getContentHandler() { return contentHandler; }
    
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }
    
    public ErrorHandler getErrorHandler() { return errorHandler; }
    
    public void parse(InputSource source) throws SAXException, IOException
    {
        
        systemId = source.getSystemId();
        contentHandler.setDocumentLocator(this);
        
        Reader reader = source.getCharacterStream();
        if (reader == null) {
            InputStream stream = source.getByteStream();
            String encoding = source.getEncoding();
            
            if (stream == null) {
                systemId = source.getSystemId();
                if(systemId == null) {
                    SAXParseException saxException = new SAXParseException(
                        "null source systemId" , this);
                    errorHandler.fatalError(saxException);
                    return;
                }
                try {
                    URL url = new URL(systemId);
                    stream = url.openStream();
                } catch (MalformedURLException nue) {
                    try {
                        stream = new FileInputStream(systemId);
                    } catch (FileNotFoundException fnfe) {
                        SAXParseException saxException = new SAXParseException(
                            "could not open file with systemId "+systemId, this, fnfe);
                        errorHandler.fatalError(saxException);
                        return;
                    }
                }
            }
            
            if(encoding == null) {
                reader = new InputStreamReader(stream);
            } else {
                try {
                    // TODO use more efficient reader for UTF8 encoding
                    reader = new InputStreamReader(stream, encoding);
                } catch (UnsupportedEncodingException une) {
                    SAXParseException saxException = new SAXParseException(
                        "cant create input stream reader for encoding "+encoding, this, une);
                    errorHandler.fatalError(saxException);
                    return;
                }
            }
        }
        try {
            if(DEBUG) {
                StringWriter sw = new StringWriter();
                char[] buf = new char[1024];
                int i;
                while((i = reader.read(buf)) > 0) {
                    sw.write(buf, 0, i);
                }
                String s = sw.toString();
                System.out.println("read:---\n"+s+"---\n");
                reader = new StringReader(s);
            }
            pp.setInput(reader);
            contentHandler.startDocument();
            // get first event
            pp.next();
            // it should be start tag...
            if(pp.getEventType() != pp.START_TAG) {
                SAXParseException saxException = new SAXParseException(
                    "expected start tag not"+pp.getPosDesc(), this);
                //throw saxException;
                errorHandler.fatalError(saxException);
                return;
            }
        } catch (XmlPullParserException ex)  {
            SAXParseException saxException = new SAXParseException(
                "parsing initialization error: "+ex, this, ex);
            //ex.printStackTrace();
            errorHandler.fatalError(saxException);
            return;
        }
        parseSubTree(pp);
        contentHandler.endDocument();
    }
    
    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }
    
    
    public void parseSubTree(XmlPullParser pp) throws SAXException, IOException {
        //      if(false == pp.isNamespaceAware()) {
        //          throw new SAXException("namespaces must be enabled"+pp.getPosDesc());
        //      }
        //pp.isNamespaceAware();
        try {
            if(pp.getEventType() != pp.START_TAG) {
                throw new SAXException(
                    "start tag must be read before skiping subtree"+pp.getPosDesc());
            }
            //      XmlPullParserEventPosition eventPosition = null;
            //      if(pp instanceof XmlPullParserEventPosition) {
            //          XmlPullParserEventPosition ep = (XmlPullParserEventPosition) pp;
            //          // make sure it really works
            //          try {
            //              if(ep.getEventStart() != -1) {
            //                  eventPosition = ep;
            //              }
            //          } catch(Exception ex) {
            //          }
            //      }
            int level = 0;
            byte type = pp.START_TAG;
            
            LOOP:
            while(true) {
                switch(type) {
                    case XmlPullParser.START_TAG: {
                            pp.readStartTag(stag);
                            int count = pp.getNamespacesLength(pp.getDepth());
                            if(count > 0) {
                                if(namespaces.length < count) {
                                    namespaces = new String[count];
                                    prefixes = new String[count];
                                }
                                pp.readNamespacesPrefixes(pp.getDepth(), prefixes, 0, count);
                                pp.readNamespacesUris(pp.getDepth(), namespaces, 0, count);
                                for (int i = 0; i < count; i++)
                                {
                                    contentHandler.startPrefixMapping(prefixes[i],
                                                                      namespaces[i]);
                                }
                            }
                            contentHandler.startElement(stag.getNamespaceUri(),
                                                        stag.getLocalName(),
                                                        stag.getRawName(),
                                                        this);
                            ++level;
                        }
                        break;
                    case XmlPullParser.CONTENT:
                        //                      // this is very efficient zero copy - directly from tokenizer
                        //                      if(eventPosition != null) {
                        //                          int end = eventPosition.getEventEnd();
                        //                          int start = eventPosition.getEventStart();
                        //                          contentHandler.characters(eventPosition.getEventBuffer(),
                        //                                                    start, end - start);
                        //                      } else {
                        String content = pp.readContent();
                        int len = content.length();
                        if(len > buf.length) {
                            buf = new char[len];
                        }
                        content.getChars(0, len, buf, 0);
                        contentHandler.characters(buf, 0, len);
                        //                      }
                        
                        break;
                    case XmlPullParser.END_TAG:
                        pp.readEndTag(etag);
                        int count = pp.getNamespacesLength(pp.getDepth());
                        contentHandler.endElement(etag.getNamespaceUri(),
                                                  etag.getLocalName(),
                                                  etag.getRawName());
                        --level;
                        if(count > 0) {
                            if(namespaces.length < count) {
                                namespaces = new String[count];
                                prefixes = new String[count];
                            }
                            pp.readNamespacesPrefixes(pp.getDepth(), prefixes, 0, count);
                            pp.readNamespacesUris(pp.getDepth(), namespaces, 0, count);
                            for (int i = count - 1; i >= 0; i--)
                            {
                                contentHandler.endPrefixMapping(prefixes[i]);
                            }
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break LOOP;
                }
                //              if(level > 0) {
                //                  type = pp.next();
                //              } else {
                //                  break;
                //              }
                type = pp.next();
            }
        } catch (XmlPullParserException ex)  {
            SAXParseException saxException = new SAXParseException("parsing error: "+ex, this, ex);
            errorHandler.fatalError(saxException);
        }
    }
    
}

