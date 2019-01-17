package no.fint.provider.eaxmi.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class XmiParserService {

    @Autowired
    XPathService xpath;

    @Value("${fint.eaxmi.uri:https://raw.githubusercontent.com/FINTprosjektet/fint-informasjonsmodell/master/FINT-informasjonsmodell.xml}")
    private String uri;

    private List<?> packages;
    private List<?> classes;
    private List<?> associations;
    private List<?> generalizations;

    public void getXmiDocument() {
        xpath.initializeSAXParser(uri);

        packages = xpath.getNodeList("//elements/element[@xmi:type=\"uml:Package\"][@name!=\"Model\"]");
        classes = xpath.getNodeList("//elements/element[@xmi:type=\"uml:Class\"]");
        associations = xpath.getNodeList("//connectors/connector/properties[@ea_type='Association']/..");
        generalizations = xpath.getNodeList("//connectors/connector/properties[@ea_type='Generalization']/..");
    }

    public String getInheritFromId(String idref) {
        return xpath.getStringValue(
                String.format("//connectors/connector/properties[@ea_type='Generalization']/../source[@xmi:idref='%s']/../target/@xmi:idref", idref)
        );
    }

    public String getIdRefFromNode(Object node) {
        return xpath.getStringValue(node, "@xmi:idref");
    }

    public List<?> getClassesInPackage(String idref) {
        return xpath.getNodeList(String.format("//element[@xmi:type=\"uml:Class\"]/model[@package=\"%s\"]/..", idref));
    }

    public String getParentPackageFromNode(Object node) {
        return xpath.getStringValue(node, "model/@package");
    }

    public String getParentPackageByIdRef(String idref) {
        return xpath.getStringValue(String.format("//element[@xmi:idref=\"%s\"]/model/@package", idref));
    }

    public List<?> getChildPackagesByIdRef(String idref) {
        return xpath.getNodeList(String.format("//elements/element[@xmi:type=\"uml:Package\"][model/@package=\"%s\"]", idref));
    }

    public String getName(String idref) {
        return xpath.getStringValue(String.format("//element[@xmi:idref=\"%s\"]/@name", idref));
    }

    public List<?> getClassForwardRelations(String idref) {
        return xpath.getNodeList(String.format("//connector[properties/@ea_type='Association'][source/@xmi:idref='%s'][target/role/@name]", idref));
    }

    public List<?> getClassReverseRelations(String idref) {
        return xpath.getNodeList(String.format("//connector[properties/@ea_type='Association'][target/@xmi:idref='%s'][source/role/@name]", idref));
    }

    public Object getRelationSource(String idref) {
        return xpath.getNode(String.format("//connector/properties[@ea_type='Association']/..[@xmi:idref='%s']/source", idref));
    }

    public Object getRelationTarget(String idref) {
        return xpath.getNode(String.format("//connector/properties[@ea_type='Association']/..[@xmi:idref='%s']/target", idref));
    }

}
