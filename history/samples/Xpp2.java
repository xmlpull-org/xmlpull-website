
import java.io.FileReader;
import java.io.IOException;
import org.gjt.xpp.XmlEndTag;
import org.gjt.xpp.XmlPullParser;
import org.gjt.xpp.XmlPullParserException;
import org.gjt.xpp.XmlPullParserFactory;
import org.gjt.xpp.XmlStartTag;

public class Xpp2 {
    public static void main(String[] args) throws Exception
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser pp = factory.newPullParser();
        pp.setInput(new FileReader("samples/sample.xml"));
        pp.next(); // move to <person>
        XmlStartTag startTag = factory.newStartTag();
        XmlEndTag endTag = factory.newEndTag();
        Person person = (new Xpp2()).parsePerson(pp, startTag, endTag);
        System.out.println(person+" "+pp.getClass().getName());
    }
    
    public Person parsePerson(XmlPullParser parser, XmlStartTag startTag, XmlEndTag endTag)
        throws ValidationException, IOException, XmlPullParserException
    {
        Person person = new Person();
        parser.setAllowedMixedContent(false);
        while(true) {
            int eventType = parser.next();
            if(eventType == XmlPullParser.START_TAG) {
                parser.readStartTag(startTag);
                String tag = startTag.getLocalName();
                if("name".equals(tag)) {
                    if(person.name != null) {
                        throw new ValidationException(
                            "only one person name is allowed ");
                    }
                    person.name = nextText(parser);
                } else if("home_address".equals(tag)) {
                    if(person.homeAddress != null) {
                        throw new ValidationException(
                            "only one home address is allowed ");
                    }
                    person.homeAddress = parseAddress(parser, startTag, endTag);
                } else if("work_address".equals(tag)) {
                    if(person.workAddress != null) {
                        throw new ValidationException(
                            "only one work address is allowed ");
                    }
                    person.workAddress = parseAddress(parser, startTag, endTag);
                } else {
                    throw new ValidationException(
                        "unknown field "+tag+" in person record");
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                break;
            }
            if(person.name == null) {
                throw new ValidationException(
                    "person name is required");
            }
        }
        return person;
    }
    
    public Address parseAddress(XmlPullParser parser, XmlStartTag startTag, XmlEndTag endTag)
        throws ValidationException, IOException, XmlPullParserException
    {
        Address address = new Address();
        while(true) {
            int eventType = parser.next();
            if(eventType == XmlPullParser.START_TAG) {
                parser.readStartTag(startTag);
                String tag = startTag.getLocalName();
                if("street".equals(tag)) {
                    address.street = nextText(parser);
                } else if("phone".equals(tag)) {
                    address.phone = nextText(parser);
                } else {
                    throw new ValidationException(
                        "unknown field "+tag+" in person record");
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                break;
            } else {
                throw new ValidationException("unexpected XML");
            }
        }
        return address;
    }
    
    private String nextText(XmlPullParser parser)
        throws ValidationException, IOException, XmlPullParserException
    {
        parser.next();
        String content = parser.readContent();
        if(parser.next() != XmlPullParser.END_TAG) {
            throw new ValidationException("expected end tag after text content");
        }
        return content;
        
        //        StringBuffer buf = new StringBuffer();
        //        LOOP: while(true) {
        //            switch(parser.next()) {
        //                case XmlPullParser.START_TAG:
        //                    throw new ValidationException("unexpected start tag for text only content");
        //                case XmlPullParser.END_TAG:
        //                    break LOOP;
        //                case XmlPullParser.END_DOCUMENT:
        //                    throw new ValidationException("unexpected XML end");
        //                case XmlPullParser.CONTENT:
        //                    buf.append(parser.readContent());
        //                    break;
        //                default:
        //                    throw new ValidationException("unexpected event");
        //            }
        //        }
        //        return buf.toString();
    }
    
    
}

