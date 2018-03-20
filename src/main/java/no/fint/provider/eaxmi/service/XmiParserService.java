package no.fint.provider.eaxmi.service;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.metamodell.Pakke;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class XmiParserService {

    private Document doc;
    private XPath xpath;

    /*
    xmlns:uml="http://schema.omg.org/spec/UML/2.1"
    xmlns:xmi="http://schema.omg.org/spec/XMI/2.1"
     */

    @PostConstruct
    public void init() {

        HashMap<String, String> prefMap = new HashMap<String, String>() {{
            put("uml", "http://schema.omg.org/spec/UML/2.1");
            put("xmi", "http://schema.omg.org/spec/XMI/2.1");
        }};
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
        namespaces.setBindings(prefMap);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(namespaces);

    }

    public List<Pakke> getPackages() {
        List<Pakke> pakkeList = new ArrayList<>();
        try {
            XPathExpression expr = xpath.compile("//elements/element[@xmi:type=\"uml:Package\"]");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength() ; i++) {

                Node item = nodes.item(i);
                Pakke pakke = getFintPakke(item);
                pakkeList.add(pakke);

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return pakkeList;
    }

    private Pakke getFintPakke(Node item) throws XPathExpressionException {
        XPathExpression nameExpression = xpath.compile("@name");
        XPathExpression idExpression = xpath.compile("@xmi:idref");
        XPathExpression stereotypeExpression = xpath.compile("properties/@stereotype");
        String pakkeNavn = nameExpression.evaluate(item, XPathConstants.STRING).toString();
        String pakkeId = idExpression.evaluate(item, XPathConstants.STRING).toString();
        String stereotype = stereotypeExpression.evaluate(item, XPathConstants.STRING).toString();
        Pakke pakke = new Pakke();
        Identifikator id = new Identifikator();
        id.setIdentifikatorverdi(pakkeId);
        pakke.setId(id);
        pakke.setNavn(pakkeNavn);
        pakke.setStereotype(stereotype);
        return pakke;
    }

    public void getXmiDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("FINT-informasjonsmodell.xml").getFile());
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(file);


        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
