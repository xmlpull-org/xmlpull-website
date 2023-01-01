
import java.io.FileReader;
import java.io.IOException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class StAXEvent {
    public static void main(String[] args) throws Exception
    {
        XMLEventReader pp = XMLInputFactory.newInstance().createXMLEventReader(
            new FileReader("samples/sample.xml"));
        pp.nextTag(); // move to <person>
        Person person = (new StAXEvent()).parsePerson(pp);
        System.out.println(person+" "+pp.getClass().getName());
    }
    
    public Person parsePerson(XMLEventReader parser)
        throws ValidationException, IOException, XMLStreamException
    {
        Person person = new Person();
        while(true) {
            XMLEvent event = parser.nextTag();
            //if(event.getEventType() == XMLStreamConstants.START_ELEMENT) {
            if(event instanceof StartElement) {
                StartElement se = (StartElement) event;
                String tag = se.getName().getLocalPart();
                if("name".equals(tag)) {
                    if(person.name != null) {
                        throw new ValidationException(
                            "only one person name is allowed ");
                    }
                    //person.name = parser.getElementText(); //bug? it does not work in RI ...
                    person.name = getElementText(parser);
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
            //} else if(event.getEventType() == XMLStreamConstants.END_ELEMENT) {
            } else if(event instanceof EndElement) {
                break;
            }
            if(person.name == null) {
                throw new ValidationException(
                    "person name is required");
            }
            
        }
        return person;
    }
    
    
    public Address parseAddress(XMLEventReader parser)
        throws ValidationException, IOException, XMLStreamException
    {
        Address address = new Address();
        while(true) {
            XMLEvent event = parser.nextTag();
            if(event instanceof StartElement) {
                StartElement se = (StartElement) event;
                String tag = se.getName().getLocalPart();
                if("street".equals(tag)) {
                    address.street = getElementText(parser);
                } else if("phone".equals(tag)) {
                    address.phone = getElementText(parser);
                } else {
                    throw new ValidationException(
                        "unknown field "+tag+" in person record");
                }
            } else if(event instanceof EndElement) {
                break;
            } else {
                throw new ValidationException("unexpected XML");
            }
        }
        return address;
    }
    
    private String getElementText(XMLEventReader parser)
        throws ValidationException, IOException, XMLStreamException
    {
        StringBuffer buf = new StringBuffer();
        LOOP: while(true) {
            XMLEvent ev = parser.nextEvent();
            switch(ev.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    throw new ValidationException("unexpected start tag for text only content");
                case XMLStreamConstants.END_ELEMENT:
                    break LOOP;
                case XMLStreamConstants.CHARACTERS:
                    Characters cev = (Characters)ev;
                    buf.append(cev.getData());
                    break;
                default:
                    throw new ValidationException("unexpected event");
                    
            }
        }
        return buf.toString();
    }
    
}

