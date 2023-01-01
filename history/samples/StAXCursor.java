
import java.io.FileReader;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StAXCursor {
    public static void main(String[] args) throws Exception
    {
        XMLStreamReader pp = XMLInputFactory.newInstance().createXMLStreamReader(
         new FileReader("samples/sample.xml"));
        pp.nextTag(); // move to <person>
        Person person = (new StAXCursor()).parsePerson(pp);
        System.out.println(person+" "+pp.getClass().getName());
    }
    
    public Person parsePerson(XMLStreamReader parser)
        throws ValidationException, IOException, XMLStreamException
    {
        Person person = new Person();
        while(true) {
            int eventType = parser.nextTag();
            if(eventType == XMLStreamConstants.START_ELEMENT) {
                String tag = parser.getLocalName();
                if("name".equals(tag)) {
                    if(person.name != null) {
                        throw new ValidationException(
                            "only one person name is allowed ");
                    }
                    person.name = parser.getElementText();
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
            } else if(eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if(person.name == null) {
                throw new ValidationException(
                    "person name is required");
            }
            
        }
        return person;
    }
        
    public Address parseAddress(XMLStreamReader parser)
        throws ValidationException, IOException, XMLStreamException
    {
        Address address = new Address();
        while(true) {
            int eventType = parser.nextTag();
            if(eventType == XMLStreamConstants.START_ELEMENT) {
                String tag = parser.getLocalName();
                if("street".equals(tag)) {
                    address.street = parser.getElementText();
                } else if("phone".equals(tag)) {
                    address.phone = parser.getElementText();
                } else {
                    throw new ValidationException(
                        "unknown field "+tag+" in person record");
                }
            } else if(eventType == XMLStreamConstants.END_ELEMENT) {
                break;
            } else {
                throw new ValidationException("unexpected XML");
            }
        }
        return address;
    }
    
}

