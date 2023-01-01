package yesnogame;

import java.io.FileInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
//import org.kxml2.io.*;

public class YesNoGame {

    public static Node parseAnswer(XmlPullParser p) throws IOException, XmlPullParserException {
        p.require(p.START_TAG, "", "answer");
        Node result = new Node (p.nextText());
        p.require(p.END_TAG, "", "answer");
        return result;
    }

    public static Node parseQuestion(XmlPullParser p) throws IOException, XmlPullParserException {
        p.require(p.START_TAG, "", "question");
        String text = p.getAttributeValue("", "text");
        Node yes = parseNode (p);
        Node no = parseNode (p);
        p.nextTag();
        p.require(p.END_TAG, "", "question");
        return new Node (text, yes, no);
    }

    public static Node parseNode (XmlPullParser p) throws IOException, XmlPullParserException {
        p.nextTag ();
        p.require(p.START_TAG, "", null);
        if (p.getName().equals("question"))
            return parseQuestion(p);
        else
            return parseAnswer(p);
    }


    public static void main(String[] args) throws IOException, XmlPullParserException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);

        XmlPullParser p = factory.newPullParser();

        //p.setInput (new FileInputStream ("gamedata.xml"), null);
        p.setInput (YesNoGame.class.getResourceAsStream("gamedata.xml"), null);

        Node game = parseNode (p);

        game.run();
    }
}
