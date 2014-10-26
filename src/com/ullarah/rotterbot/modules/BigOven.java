package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static com.ullarah.rotterbot.Client.pluginKey;

public class BigOven {

    private static String title;
    private static String description;
    private static String cuisine;
    private static String category;
    private static String url;

    public static String recipe() {

        try {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder docB = docBF.newDocumentBuilder();
            Document doc = docB.parse("http://api.bigoven.com/recipe/" + Utility.randInt(1, 250550) + "?api_key=" + pluginKey("bigoven"));

            doc.getDocumentElement().normalize();

            if (!doc.getDocumentElement().getTextContent().contains("403")) {
                title = doc.getElementsByTagName("Title").item(0).getTextContent();
                description = doc.getElementsByTagName("Description").item(0).getTextContent().equals("")
                        ? "No Description" : doc.getElementsByTagName("Description").item(0).getTextContent();
                cuisine = doc.getElementsByTagName("Cuisine").item(0).getTextContent();
                category = doc.getElementsByTagName("Category").item(0).getTextContent();
                url = doc.getElementsByTagName("WebURL").item(0).getTextContent();

            } else recipe();
            return "[BIGOVEN] " + title + " | " + description + " | " + cuisine + " | " + category + " | " + url;

        } catch (IOException | ParserConfigurationException | SAXException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}