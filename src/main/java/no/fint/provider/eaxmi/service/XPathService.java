package no.fint.provider.eaxmi.service;

import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.PostConstruct;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;

@Service
public class XPathService {

    private XPath xpath;
    private TreeInfo treeInfo;


    static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();


    @PostConstruct
    public void init() {

        HashMap<String, String> prefMap = new HashMap<String, String>() {{
            put("uml", "http://schema.omg.org/spec/UML/2.1");
            put("xmi", "http://schema.omg.org/spec/XMI/2.1");
        }};
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
        namespaces.setBindings(prefMap);
        //XPathFactory xpathFactory = XPathFactory.newInstance();
        xpath = XPATH_FACTORY.newXPath();
        //xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(namespaces);
    }

    public String getStringValue(Object document, String expression) throws XPathExpressionException {

        return xpath.compile(expression).evaluate(document, XPathConstants.STRING).toString();
    }

    public NodeList getNodeList(Object document, String expression) throws XPathExpressionException {
        return (NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);
    }


    public String getStringValue(Node document, String expression) {
        return XPathEvaluator.query(expression, document, XPathConstants.STRING).toString();
    }

    public NodeList getNodeList(Node document, String expression) {
        return (NodeList) XPathEvaluator.query(expression, document, XPathConstants.NODESET);
    }


    public String getStringValue(/*Object document,*/ String expression) {
        try {
            return xpath.compile(expression).evaluate(treeInfo, XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;

    }

    public NodeList getNodeList(/*Object document,*/ String expression) {

        try {
            return (NodeList) xpath.compile(expression).evaluate(treeInfo, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return null;

    }

    public void initializeSAXParser(String uri) {
        // Set DOM structures to null to release memory

        xpath = null;
        try {
            // The following initialization code is specific to Saxon
            // Please refer to SaxonHE documentation for details
            XPathFactory xpFactory = new net.sf.saxon.xpath.XPathFactoryImpl();
            xpath = xpFactory.newXPath();

            HashMap<String, String> prefMap = new HashMap<String, String>() {{
                put("uml", "http://schema.omg.org/spec/UML/2.1");
                put("xmi", "http://schema.omg.org/spec/XMI/2.1");
            }};
            SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
            namespaces.setBindings(prefMap);

            xpath.setNamespaceContext(namespaces);

            // Build the source document.
            InputSource inputSrc = new InputSource(uri);
            SAXSource saxSrc = new SAXSource(inputSrc);
            net.sf.saxon.Configuration config =
                    ((net.sf.saxon.xpath.XPathFactoryImpl)
                            xpFactory).getConfiguration();
            treeInfo = config.buildDocumentTree(saxSrc);


        } catch (XPathException e) {
            System.out.println("Exception in initialize():  " + e.getMessage());
            e.printStackTrace();
        }
    }


}
