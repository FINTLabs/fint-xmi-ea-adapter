package no.fint.provider.eaxmi.service;

import org.springframework.util.xml.SimpleNamespaceContext;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

public class XPathEvaluator {
    private static final String DTM_MANAGER_NAME = "com.sun.org.apache.xml.internal.dtm.DTMManager";
    private static final String DTM_MANAGER_VALUE = "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault";
    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {

            return XPathFactory.newInstance();
        }
    };
    private static final Map<String, XPathExpression> expressions = new HashMap<>();

    static {
        // performance improvement: https://issues.apache.org/jira/browse/XALANJ-2540
        System.setProperty(DTM_MANAGER_NAME, DTM_MANAGER_VALUE);
    }

    public static Object query(String xPathExpression, Object document, QName resultType) {
        XPathExpression expression = expressions.get(xPathExpression);
        try {
            if (expression == null) {
                XPath xpath = XPATH_FACTORY.get().newXPath();
                HashMap<String, String> prefMap = new HashMap<String, String>() {{
                    put("uml", "http://schema.omg.org/spec/UML/2.1");
                    put("xmi", "http://schema.omg.org/spec/XMI/2.1");
                }};
                SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
                namespaces.setBindings(prefMap);
                xpath.setNamespaceContext(namespaces);

                expression = xpath.compile(xPathExpression);
                expressions.put(xPathExpression, expression);
                return expression.evaluate(document, resultType);
            } else {
                return expression.evaluate(document, resultType);
            }
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Error while executing XPath evaluation!", e);
        }
    }
} 