package no.fint.provider.eaxmi.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

@Service
@Data
@Slf4j
public class XmiParserService {

    @Autowired
    private XPathService xpath;

    @Value("${fint.eaxmi.uri:https://raw.githubusercontent.com/FINTprosjektet/fint-informasjonsmodell-metamodell/master/FINT-metamodell.xml}")
    private String uri;

    private Document doc;
    private NodeList packages;
    private NodeList classes;
    private NodeList associations;
    private NodeList generalizations;

    public void getXmiDocument() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        xpath.initializeSAXParser(uri);


        builder = factory.newDocumentBuilder();
        doc = builder.parse(uri);

        packages = xpath.getNodeList(doc, "//elements/element[@xmi:type=\"uml:Package\"][@name!=\"Model\"]");
        classes = xpath.getNodeList(doc, "//elements/element[@xmi:type=\"uml:Class\"]");
        associations = xpath.getNodeList(doc, "//connectors/connector/properties[@ea_type='Association']/..");
        generalizations = xpath.getNodeList(doc, "//connectors/connector/properties[@ea_type='Generalization']/..");

        log.info("XMI loaded, {} packages, {} classes, {} generalizations, {} associations",
                packages.getLength(),
                classes.getLength(),
                generalizations.getLength(),
                associations.getLength());

    }

    public String getInheritFromId(String idref) throws XPathExpressionException {

        return xpath.getStringValue(doc,
                String.format("//connectors/connector/properties[@ea_type='Generalization']/../source[@xmi:idref='%s']/../target/@xmi:idref", idref)
        );
    }


    public String getIdRefFromNode(Node node) {
        //try {
        return xpath.getStringValue(node, "@xmi:idref");
        //} catch (XPathExpressionException e) {
        //    e.printStackTrace();
        //    return null;
        //}
    }

    public NodeList getClassesInPackage(String idref) throws XPathExpressionException {
        return xpath.getNodeList(doc, String.format("//element[@xmi:type=\"uml:Class\"]/model[@package=\"%s\"]/..", idref));
    }

    public String getParentPackageFromNode(Node node) {
        //try {
        return xpath.getStringValue(node, "model/@package");
        //} catch (XPathExpressionException e) {
        //    return null;
        //}
    }

    public String getParentPackageByIdRef(String idref) {

        //try {
        return xpath.getStringValue(doc, String.format("//element[@xmi:idref=\"%s\"]/model/@package", idref));
        //} catch (XPathExpressionException e) {
        //    e.printStackTrace();
        //    return null;
        //}

    }

    public String getName(String idref) {
        //try {
        return xpath.getStringValue(doc, String.format("//element[@xmi:idref=\"%s\"]/@name", idref));
        //} catch (XPathExpressionException e) {
        //    e.printStackTrace();
        //    return null;
        //}
    }

    public NodeList getClassRelations(String idref) {
        //try {
        return xpath.getNodeList(doc, String.format("//connector/properties[@ea_type='Association']/../source[@xmi:idref='EAID_3918321E_C706_4469_892B_CA90C03B4378']/..", idref));
        //} catch (XPathExpressionException e) {
        //    e.printStackTrace();
        //    return null;
        //}
    }

}
