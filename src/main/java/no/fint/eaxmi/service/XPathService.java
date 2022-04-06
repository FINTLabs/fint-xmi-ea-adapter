package no.fint.eaxmi.service;

import com.google.common.collect.ImmutableMap;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;

@Service
public class XPathService {

    private XPath xpath;
    private TreeInfo treeInfo;


    String getStringValue(Object document, String expression) {
        try {
            return xpath.compile(expression).evaluate(document, XPathConstants.STRING).toString();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    String getStringValue(String expression) {
        return getStringValue(treeInfo, expression);
    }

    List<?> getNodeList(Object document, String expression) {

        try {
            return (List) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    List getNodeList(String expression) {
        return getNodeList(treeInfo, expression);
    }

    Object getNode(String expression) {

        try {
            return xpath.compile(expression).evaluate(treeInfo, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    void initializeSAXParser(String uri) {
        xpath = null;
        try {
            XPathFactoryImpl xpFactory = new net.sf.saxon.xpath.XPathFactoryImpl();
            xpath = xpFactory.newXPath();

            xpath.setNamespaceContext(new SimpleNamespaceContext() {{
                setBindings(ImmutableMap.of(
                        "uml", "http://schema.omg.org/spec/UML/2.1",
                        "xmi", "http://schema.omg.org/spec/XMI/2.1"
                ));
            }});

            InputSource inputSrc = new InputSource(uri);
            SAXSource saxSrc = new SAXSource(inputSrc);
            net.sf.saxon.Configuration config =
                    xpFactory.getConfiguration();
            treeInfo = config.buildDocumentTree(saxSrc);

        } catch (XPathException e) {
            throw new RuntimeException(e);
        }
    }


}
