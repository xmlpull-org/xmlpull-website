
import java.io.FileReader;
import java.io.IOException;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;

public class Kxml1 {
    public static void main(String[] args) throws Exception
    {
        XmlParser pp = new XmlParser(new FileReader("samples/sample.xml"));
        pp.read(); // move to <person>
        Person person = (new Kxml1()).parsePerson(pp);
        System.out.println(person+" "+pp.getClass().getName());
    }
    
    private Person parsePerson(XmlParser parser)
        throws ValidationException, IOException
    {
        Person person = new Person();
        while(true) {
            ParseEvent event = parser.read();
            if(event.getType() == Xml.START_TAG) {
                String tag = event.getName();
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
            } else if(event.getType() == Xml.END_TAG) {
                break;
            } else if(event.getType() == Xml.WHITESPACE) {
                continue; //skip whitespcaces
            } else {
                throw new ValidationException(
                    "unexppected XML event "+event);
            }
            if(person.name == null) {
                throw new ValidationException(
                    "person name is required");
            }
            
        }
        return person;
    }
    
    private Address parseAddress(XmlParser parser)
        throws ValidationException, IOException
    {
        Address address = new Address();
        while(true) {
            ParseEvent event = parser.read();
            if(event.getType() == Xml.START_TAG) {
                String tag = event.getName();
                if("street".equals(tag)) {
                    address.street = nextText(parser);
                } else if("phone".equals(tag)) {
                    address.phone = nextText(parser);;
                } else {
                    throw new ValidationException(
                        "unknown field "+tag+" in person record");
                }
            } else if(event.getType() == Xml.END_TAG) {
                break;
            } else if(event.getType() == Xml.WHITESPACE) {
                continue; //skip whitespcaces
            } else {
                throw new ValidationException("unexpected XML event "+event);
            }
        }
        return address;
    }
    
    private String nextText(XmlParser parser)
        throws ValidationException, IOException
    {
        // read element content and move parser to element end tag
        StringBuffer buf = new StringBuffer();
        while(true) {
            ParseEvent event = parser.read();
            if(event.getType() == Xml.START_TAG) {
                throw new ValidationException("expected text only element content");
            } else if(event.getType() == Xml.END_TAG) {
                break;
            } else if(event.getType() == Xml.TEXT || event.getType() == Xml.WHITESPACE) {
                buf.append(event.getText());
            } else {
                throw new ValidationException(
                    "unexppected XML event "+event);
            }
        }
        return buf.toString();
    }
    
}

