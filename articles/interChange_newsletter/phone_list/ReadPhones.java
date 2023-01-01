package phone_list;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class ReadPhones {

    final static String SAMPLE_XML =
        "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"+
        "<phone-list xmlns=\"http://example.com/2002/phone-list\">\n"+
        "  <phone type=\"work\">\n"+
        "    <phone-number>333.3333</phone-number>\n"+
        "  </phone>\n"+
        "  <phone type=\"fax\">\n"+
        "    <phone-number>444.4444</phone-number>\n"+
        "  </phone>\n"+
        "</phone-list>";

    public static void main (String args[])
        throws XmlPullParserException, IOException
    {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        //"org.xmlpull.mxp1.MXParserFactory", null);
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        XmlPullParser parser = factory.newPullParser();
        System.out.println("parser implementation class is "+parser.getClass());

        parser.setInput(new StringReader(SAMPLE_XML));
        final String NS_URI = "http://example.com/2002/phone-list";


        Vector phoneList = new Vector();
        //move input to <phone-list> start element
        parser.next();
        parser.require(parser.START_TAG, NS_URI, "phone-list");
        while(true) {
            //get next event and signal error if next event is not star or end tag
            parser.nextTag();
            //if (event is end element and has name 'phone-list') break;
            if(parser.getEventType() == parser.END_TAG) break;
            //if (event is not start element) signal error
            //if (start element has no name "phone") signal error
            parser.require(parser.START_TAG, NS_URI, "phone");
            Phone phone = new Phone();
            // phone.setType( get value of attribute named 'type' from current element )
            phone.setType( parser.getAttributeValue("", "type") );
            //read next start element skipping white spaces and signal error if next event is not start element
            //if(element name is not 'phone-number') signal error
            parser.nextTag();
            parser.require(parser.START_TAG, NS_URI, "phone-number");
            // phone.setPhoneNumber( read text content of current element)
            // move to end element 'phone-number'
            phone.setPhoneNumber( parser.nextText() );
            // nextText() already move parser to END_TAG
            parser.require(parser.END_TAG, NS_URI, "phone-number");
            // move to end element 'phone'
            parser.nextTag();
            parser.require(parser.END_TAG, NS_URI, "phone");
            phoneList.add(phone);
        }
        //check that input is on </phone-list> end element
        parser.require(parser.END_TAG, NS_URI, "phone-list");

        // print list of phone
        for (int i = 0; i < phoneList.size(); i++) {
                Phone phone = (Phone) phoneList.get(i);
                System.out.println("phone number="+phone.getPhoneNumber()+" type="+phone.getType());
        }

    }
}
