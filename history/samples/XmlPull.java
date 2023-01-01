
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPull {
    public static void main(String[] args) throws Exception
    {
        XmlPullParser pp = XmlPullParserFactory.newInstance().newPullParser();
        pp.setInput(new FileReader("samples/sample.xml"));
        pp.nextTag(); // move to <person>
        Person person = (new XmlPull()).parsePerson(pp);
        System.out.println(person+" "+pp.getClass().getName());
    }
    
    public Person parsePerson(XmlPullParser parser)
        throws ValidationException, IOException, XmlPullParserException
    {
        Person person = new Person();
        while(true) {
            int eventType = parser.nextTag();
            if(eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if("name".equals(tag)) {
                    if(person.name != null) {
                        throw new ValidationException(
                            "only one person name is allowed ");
                    }
                    person.name = parser.nextText();
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
    
    public Address parseAddress(XmlPullParser parser)
        throws ValidationException, IOException, XmlPullParserException
    {
        Address address = new Address();
        while(true) {
            int eventType = parser.nextTag();
            if(eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if("street".equals(tag)) {
                    address.street = parser.nextText();
                } else if("phone".equals(tag)) {
                    address.phone = parser.nextText();
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
    
}

