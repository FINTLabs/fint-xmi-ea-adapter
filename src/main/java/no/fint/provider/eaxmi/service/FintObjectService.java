package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.metamodell.Klasse;
import no.fint.model.metamodell.Pakke;
import no.fint.model.metamodell.Relasjon;
import no.fint.model.metamodell.kompleksedatatyper.Attributt;
import no.fint.model.relation.FintResource;
import no.fint.model.relation.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FintObjectService {

    @Autowired
    XPathService xpath;

    @Autowired
    XmiParserService xmiParserService;

    public List<FintResource> getPackages() {

        NodeList packages = xmiParserService.getPackages();
        List<FintResource> pakkeList = new ArrayList<>();

        try {

            for (int i = 0; i < packages.getLength(); i++) {

                Node node = packages.item(i);
                Pakke pakke = getFintPakke(node);

                String parentId = xmiParserService.getParentPackageFromNode(node);

                NodeList classesInPackageNodes = xmiParserService.getClassesInPackage(xmiParserService.getIdRefFromNode(node));
                List<Relation> relations = new ArrayList<>();
                for (int j = 0; j < classesInPackageNodes.getLength(); j++) {
                    relations.add(
                            new Relation.Builder()
                                    .with(Pakke.Relasjonsnavn.KLASSE)
                                    .forType(Klasse.class)
                                    .field("id")
                                    .value(xmiParserService.getIdRefFromNode(classesInPackageNodes.item(j)))
                                    .build()
                    );
                }
                if (parentId.length() > 0) {
                    relations.add(new Relation.Builder()
                            .with(Pakke.Relasjonsnavn.OVERORDNET)
                            .forType(Pakke.class)
                            .field("id")
                            .value(xmiParserService.getParentPackageFromNode(node))
                            .build()
                    );
                }

                pakkeList.add(
                        FintResource.with(pakke)
                                .addRelations(relations)
                );

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return pakkeList;
    }

    public List<FintResource> getClasses() {
        log.info("Start getting classes");
        NodeList classes = xmiParserService.getClasses();
        List<FintResource> klasseList = new ArrayList<>();

        for (int i = 0; i < classes.getLength(); i++) {


            try {
                Node node = classes.item(i);
                Klasse klasse = getFintKlasse(node);
                List<Relation> relationList = new ArrayList<>();

                addInheritenFromRelation(node, relationList);


                addPackageRelation(node, relationList);

                addClassRelations(relationList, klasse.getId().getIdentifikatorverdi());

                klasseList.add(FintResource.with(klasse).addRelations(relationList));
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return null;
            }

        }
        log.info("End getting classes");

        return klasseList;
    }

    public void addClassRelations(List<Relation> relationList, String idref) throws XPathExpressionException {

        NodeList classRelations = xmiParserService.getClassRelations(idref);

        for (int i = 0; i < classRelations.getLength(); i++) {
            Node relasjon = classRelations.item(i);
            relationList.add(new Relation.Builder()
                    .with(Klasse.Relasjonsnavn.RELASJON)
                    .forType(Klasse.class)
                    .field("id")
                    .value(getRelasjonId(relasjon))
                    .build()
            );
        }




    }

    private void addPackageRelation(Node node, List<Relation> relationList) {
        relationList.add(new Relation.Builder()
                .with(Klasse.Relasjonsnavn.PAKKE)
                .forType(Pakke.class)
                .field("id")
                .value(xmiParserService.getParentPackageByIdRef(xmiParserService.getIdRefFromNode(node)))
                .build()
        );
    }

    private void addInheritenFromRelation(Node node, List<Relation> relationList) throws XPathExpressionException {
        String arverId = xmiParserService.getInheritFromId(xmiParserService.getIdRefFromNode(node));
        if (arverId.length() > 0) {
            Relation arverRelation = new Relation.Builder().
                    with(Klasse.Relasjonsnavn.ARVER)
                    .forType(Klasse.class)
                    .field("id")
                    .value(arverId)
                    .build();
            relationList.add(arverRelation);
        }
    }

    public Pakke getFintPakke(Node item) throws XPathExpressionException {

        Pakke pakke = new Pakke();
        pakke.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        pakke.setNavn(xpath.getStringValue(item, "@name"));
        pakke.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));
        return pakke;
    }

    public Klasse getFintKlasse(Node item) throws XPathExpressionException {


        NodeList attributes = xpath.getNodeList(item, "attributes/attribute");

        List<Attributt> attributtList = new ArrayList<>();
        for (int i = 0; i < attributes.getLength(); i++) {

            Node attribute = attributes.item(i);
            attributtList.add(getFintAttributt(attribute));

        }

        Klasse klasse = new Klasse();

        klasse.setAbstrakt(Boolean.valueOf(xpath.getStringValue(item, "properties/@isAbstract")));
        klasse.setAttributter(attributtList);
        klasse.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(item, "properties/@documentation")));
        klasse.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        klasse.setNavn(xpath.getStringValue(item, "@name"));
        klasse.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));

        return klasse;
    }

    public Attributt getFintAttributt(Node attribute) throws XPathExpressionException {

        Attributt fintAttributt = new Attributt();

        fintAttributt.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(attribute, "documentation/@value")));
        fintAttributt.setMultiplisitet(
                FintFactory.getMultiplisitet(
                        xpath.getStringValue(attribute, "bounds/@lower"),
                        xpath.getStringValue(attribute, "bounds/@upper")
                )
        );
        fintAttributt.setNavn(xpath.getStringValue(attribute, "@name"));
        fintAttributt.setType(xpath.getStringValue(attribute, "properties/@type"));
        fintAttributt.setStereotype(xpath.getStringValue(attribute, "stereotype/@stereotype"));

        return fintAttributt;
    }

    public Relasjon getFintRelasjon(Node relation) throws XPathExpressionException {

        Relasjon relasjon = new Relasjon();

        relasjon.setNavn(xpath.getStringValue(relation, "target/role/@name"));
        relasjon.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(relation, "target/documentation/@value")));
        relasjon.setMultiplisitet(Arrays.asList(FintFactory.getMultiplisitetFromString(xpath.getStringValue(relation, "target/type/@multiplicity"))));
        relasjon.setId(FintFactory.getIdentifikator(getRelasjonId(relation)));

        return relasjon;
    }

    private String getId(String idref) {

        List<String> idElements = new ArrayList<>();
        String parentPackageId;

        idElements.add(xmiParserService.getName(idref));

        parentPackageId = xmiParserService.getParentPackageByIdRef(idref);
        idElements.add(xmiParserService.getName(parentPackageId));
        while (!xmiParserService.getParentPackageByIdRef(parentPackageId).isEmpty()) {
            parentPackageId = xmiParserService.getParentPackageByIdRef(parentPackageId);
            idElements.add(xmiParserService.getName(parentPackageId));
        }

        Collections.reverse(idElements);
        String id = String.join(".", idElements).toLowerCase();

        return id.replace("model", "no");
    }

    public String getRelasjonId(Node relation) throws XPathExpressionException {
        return String.format("%s.relasjon.%s",
                getId(xpath.getStringValue(relation, "source/@xmi:idref")),
                xpath.getStringValue(relation, "target/role/@name")
        );
    }

}
