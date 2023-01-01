
import java.io.FileReader;
import java.io.IOException;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;

public class RunAll {
    public static void main(String[] args) throws Exception
    {
        StAXCursor.main(args);
        StAXEvent.main(args);
        Xpp1.main(args);
        Xpp2.main(args);
        NekoPull.main(args);
        XmlPull.main(args);
        Kxml1.main(args);
    }
    
    
}

