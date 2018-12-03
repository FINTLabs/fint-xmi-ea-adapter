package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import no.fint.model.metamodell.Klasse;
import no.fint.model.metamodell.Pakke;
import no.fint.model.metamodell.Relasjon;
import no.fint.model.metamodell.kompleksedatatyper.Attributt;
import no.fint.model.relation.FintResource;
import no.fint.model.relation.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        List packages = xmiParserService.getPackages();
        List<FintResource> pakkeList = new ArrayList<>();

        packages.forEach(node -> {
            try {

                TinyElementImpl element = (TinyElementImpl) node;
                Pakke pakke = getFintPakke(element);
                String parentId = xmiParserService.getParentPackageFromNode(element);

                List<TinyElementImpl> classesInPackageNodes = xmiParserService.getClassesInPackage(xmiParserService.getIdRefFromNode(element));
                List<Relation> relations = new ArrayList<>();

                classesInPackageNodes.forEach(pkg -> {
                    relations.add(
                            new Relation.Builder()
                                    .with(Pakke.Relasjonsnavn.KLASSE)
                                    .forType(Klasse.class)
                                    .field("id")
                                    .value(xmiParserService.getIdRefFromNode((TinyElementImpl) pkg))
                                    .build()
                    );

                });

                if (parentId.length() > 0) {
                    relations.add(new Relation.Builder()
                            .with(Pakke.Relasjonsnavn.OVERORDNET)
                            .forType(Pakke.class)
                            .field("id")
                            .value(xmiParserService.getParentPackageFromNode(element))
                            .build()
                    );
                }

                pakkeList.add(
                        FintResource.with(pakke)
                                .addRelations(relations)
                );

            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        });


        return pakkeList;
    }

    public List<FintResource> getClasses() {
        log.info("Start getting classes");
        List classes = xmiParserService.getClasses();
        List<FintResource> klasseList = new ArrayList<>();


        classes.forEach(clazz -> {
            try {
                TinyElementImpl element = (TinyElementImpl) clazz;
                Klasse klasse = getFintKlasse(element);
                List<Relation> relationList = new ArrayList<>();

                addInheritenFromRelation(element, relationList);


                addPackageRelation(element, relationList);


                addClassRelations(relationList, xmiParserService.getIdRefFromNode(element));

                klasseList.add(FintResource.with(klasse).addRelations(relationList));
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                //return null;
            }

        });

        log.info("End getting classes");

        return klasseList;
    }

    public List<FintResource> getRelations() {

        log.info("Start getting relations");
        List<TinyElementImpl> relations = xmiParserService.getAssociations();
        List<FintResource> relasjonList = new ArrayList<>();

        relations.forEach(relation -> {
            //TinyElementImpl element = relation;
            List<Relation> relationList = new ArrayList<>();


            Relasjon fintRelasjon = getFintRelasjon(relation);
            addRelationClasses(relationList, xmiParserService.getIdRefFromNode(relation));
            relasjonList.add(FintResource.with(fintRelasjon).addRelations(relationList));

        });

        log.info("End getting relations");

        return relasjonList;


    }

    public void addClassRelations(List<Relation> relationList, String idref) {

        List classRelations = xmiParserService.getClassRelations(idref);

        classRelations.forEach(relation -> {
            relationList.add(new Relation.Builder()
                    .with(Klasse.Relasjonsnavn.RELASJON)
                    .forType(Relasjon.class)
                    .field("id")
                    .value(getRelasjonId((TinyElementImpl) relation))
                    .build()
            );

        });

    }

    public void addRelationClasses(List<Relation> relationList, String idref) {

        relationList.add(new Relation.Builder()
                .with(Relasjon.Relasjonsnavn.KLASSE)
                .forType(Klasse.class)
                .field("id")
                .value(getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationSource(idref))))
                .build()
        );

        relationList.add(new Relation.Builder()
                .with(Relasjon.Relasjonsnavn.KLASSE)
                .forType(Klasse.class)
                .field("id")
                .value(getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationTarget(idref))))
                .build()
        );
    }

    private void addPackageRelation(TinyElementImpl node, List<Relation> relationList) {
        relationList.add(new Relation.Builder()
                .with(Klasse.Relasjonsnavn.PAKKE)
                .forType(Pakke.class)
                .field("id")
                .value(xmiParserService.getParentPackageByIdRef(xmiParserService.getIdRefFromNode(node)))
                .build()
        );
    }

    private void addInheritenFromRelation(TinyElementImpl node, List<Relation> relationList) throws XPathExpressionException {
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

    public Pakke getFintPakke(TinyElementImpl item) throws XPathExpressionException {

        Pakke pakke = new Pakke();
        pakke.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        pakke.setNavn(xpath.getStringValue(item, "@name"));
        pakke.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));
        return pakke;
    }

    public Klasse getFintKlasse(TinyElementImpl item) {


        List attributes = xpath.getNodeList(item, "attributes/attribute");

        List<Attributt> attributtList = new ArrayList<>();

        attributes.forEach(attribute -> {
            attributtList.add(getFintAttributt((TinyElementImpl) attribute));
        });


        Klasse klasse = new Klasse();

        klasse.setAbstrakt(Boolean.valueOf(xpath.getStringValue(item, "properties/@isAbstract")));
        klasse.setAttributter(attributtList);
        klasse.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(item, "properties/@documentation")));
        klasse.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        klasse.setNavn(xpath.getStringValue(item, "@name"));
        klasse.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));

        return klasse;
    }

    public Attributt getFintAttributt(TinyElementImpl attribute) {

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

    public Relasjon getFintRelasjon(TinyElementImpl relation) {

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
        String id = String.join("_", idElements).toLowerCase();

        return id.replace("model", "no");
    }

    public String getRelasjonId(TinyElementImpl relation) {
        return String.format("%s_relasjon_%s",
                getId(xpath.getStringValue(relation, "source/@xmi:idref")),
                xpath.getStringValue(relation, "target/role/@name")
        );
    }

}
