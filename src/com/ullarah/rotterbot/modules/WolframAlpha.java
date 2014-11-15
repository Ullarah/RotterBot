package com.ullarah.rotterbot.modules;

import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static com.ullarah.rotterbot.Client.pluginKey;
import static com.ullarah.rotterbot.Utility.urlEncode;

public class WolframAlpha {

    public static String calculate(String input) {

        try {

            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder docB = docBF.newDocumentBuilder();
            Document doc = docB.parse("http://api.wolframalpha.com/v2/query?appid="
                    + pluginKey("wolframalpha") + "&input=" + urlEncode(input) + "&format=plaintext");

            doc.getDocumentElement().normalize();

            Element docElement = doc.getDocumentElement();
            boolean success = Boolean.parseBoolean(docElement.getAttribute("success"));

            if (success) {
                return (docElement.getElementsByTagName("pod").item(1).getTextContent().trim());
            } else {
                return ("Sorry, couldn't quite understand that...");
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

}
