package no.fint.provider.eaxmi.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;

@Service
@Data
public class XmiParserService {

    @Autowired
    XPathService xpath;

    private Document doc;
    private NodeList packages;
    private NodeList classes;
    private NodeList associations;
    private NodeList generalizations;


    public void getXmiDocument() {
        //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setNamespaceAware(true);
        //DocumentBuilder builder;
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("FINT-informasjonsmodell.xml").getFile());
        xpath.initializeSAXParser(file);


        //builder = factory.newDocumentBuilder();
        //doc = builder.parse(file);

        packages = xpath.getNodeList(doc, "//elements/element[@xmi:type=\"uml:Package\"][@name!=\"Model\"]");
        classes = xpath.getNodeList(doc, "//elements/element[@xmi:type=\"uml:Class\"]");
        associations = xpath.getNodeList(doc, "//connectors/connector/properties[@ea_type='Association']/..");
        generalizations = xpath.getNodeList(doc, "//connectors/connector/properties[@ea_type='Generalization']/..");


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
