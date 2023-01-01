
import java.io.IOException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.pull.XMLEvent;
import org.cyberneko.pull.XMLPullParser;
import org.cyberneko.pull.event.CharactersEvent;
import org.cyberneko.pull.event.ElementEvent;
import org.cyberneko.pull.parsers.Xerces2;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;
import org.apache.xerces.xni.XMLString;

public class NekoPull {
    public static void main(String[] args) throws Exception
    {
        XMLPullParser pp = new Xerces2();
        //XmlParser pp = new XmlParser(new FileReader("samples/sample.xml"));
        XMLInputSource source = new XMLInputSource(null, "samples/sample.xml", null);
        pp.setInputSource(source);
        
        
        // move to the first start tag
        XMLEvent event;
        while ((event = pp.nextEvent()) != null) {
            if (event.type == XMLEvent.ELEMENT) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    break;
                }
            }
        }
        if(event == null) throw new ValidationException("unexpected end of XML");
        
        Person person = (new NekoPull()).parsePerson(pp);
        System.out.println(person+" "+pp.getClass().getName());
        
    }
    
    private Person parsePerson(XMLPullParser parser)
        throws ValidationException, IOException
    {
        Person person = new Person();
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            if (event.type == XMLEvent.ELEMENT) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    String tag = elementEvent.element.localpart;
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
                        person.homeAddress = parseAddress(parser);
                    } else if("work_address".equals(tag)) {
                        if(person.workAddress != null) {
                            throw new ValidationException(
                                "only one work address is allowed ");
                        }
                        person.workAddress = parseAddress(parser);
                    } else {
                        throw new ValidationException(
                            "unknown field "+tag+" in person record");
                    }
                }
                else {
                    break;
                }
            } else if (event.type == XMLEvent.CHARACTERS) {
                CharactersEvent charsEvent = (CharactersEvent)event;
                if(!charsEvent.ignorable && !isWhitespace(charsEvent.text)) {
                    throw new ValidationException(
                        "only ignorable whitespace content allowed in <person> '"+charsEvent.text+"'");
                }
            } else {
                throw new ValidationException(
                    "unexpected XML event "+event);
            }
            
        }
        if(event == null) throw new ValidationException("unexpected end of XML");
        if(person.name == null) {
            throw new ValidationException(
                "person name is required");
        }
        return person;
    }
    
    private Address parseAddress(XMLPullParser parser)
        throws ValidationException, IOException
    {
        Address address = new Address();
        XMLEvent event;
        while ((event = parser.nextEvent()) != null) {
            if (event.type == XMLEvent.ELEMENT) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    String tag = elementEvent.element.localpart;
                    if("street".equals(tag)) {
                        address.street = nextText(parser);
                    } else if("phone".equals(tag)) {
                        address.phone = nextText(parser);
                    } else {
                        throw new ValidationException(
                            "unknown field "+tag+" in person record");
                    }
                }
                else {
                    break;
                }
            } else if (event.type == XMLEvent.CHARACTERS) {
                CharactersEvent charsEvent = (CharactersEvent)event;
                if(!charsEvent.ignorable && !isWhitespace(charsEvent.text)) {
                    throw new ValidationException(
                        "only ignorable whitespace content allowed in <person> '"+charsEvent.text+"'");
                }
            } else {
                throw new ValidationException(
                    "unexpected XML event "+event);
            }
        }
        if(event == null) throw new ValidationException("unexpected end of XML");
        return address;
    }
    
    private String nextText(XMLPullParser parser)
        throws ValidationException, IOException
    {
        // read element content and move parser to element end tag
        StringBuffer buf = new StringBuffer();
        XMLEvent  event;
        while ((event = parser.nextEvent()) != null) {
            if (event.type == XMLEvent.ELEMENT) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (elementEvent.start) {
                    throw new ValidationException("expected text only element content not "+event);
                }
                else {
                    break;
                }
            } else if (event.type == XMLEvent.CHARACTERS) {
                CharactersEvent charsEvent = (CharactersEvent)event;
                buf.append(charsEvent.text);
            } else {
                throw new ValidationException(
                    "unexpected XML event "+event);
            }
        }
        if(event == null) throw new ValidationException("unexpected end of XML");
        return buf.toString();
    }
    
    private boolean isWhitespace(XMLString text) {
        for (int i = text.offset; i < text.length; i++)
        {
            char c = text.ch[i];
            if(c != ' ' && c != '\r' && c!='\n' && c!='\t') {
                return false;
            }
        }
        return true;
    }
}

