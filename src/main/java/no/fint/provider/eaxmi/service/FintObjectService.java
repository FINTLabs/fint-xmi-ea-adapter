package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.metamodell.Klasse;
import no.fint.model.metamodell.Pakke;
import no.fint.model.metamodell.Relasjon;
import no.fint.model.metamodell.kompleksedatatyper.Attributt;
import no.fint.model.relation.FintResource;
import no.fint.model.relation.Relation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FintObjectService {

    @Autowired
    XPathService xpath;

    @Autowired
    XmiParserService xmiParserService;

    @Scheduled(initialDelay = 900000, fixedRateString = "${fint.eaxmi.interval:900000}")
    public void update() {
        log.info("Parsing XMI document...");
        xmiParserService.getXmiDocument();
        log.info("Parsing XMI document complete.");
    }

    public Stream<FintResource> getPackages() {

        return xmiParserService
                .getPackages()
                .stream()
                .map(element -> {
                    Pakke pakke = getFintPakke(element);
                    FintResource<Pakke> resource = FintResource.with(pakke);

                    xmiParserService
                            .getClassesInPackage(
                                    xmiParserService.getIdRefFromNode(element))
                            .stream()
                            .map(xmiParserService::getIdRefFromNode)
                            .map(this::getId)
                            .map(id -> new Relation.Builder()
                                    .with(Pakke.Relasjonsnavn.KLASSE)
                                    .forType(Klasse.class)
                                    .field("id")
                                    .value(id)
                                    .build())
                            .forEach(resource::addRelations);

                    String parentId = xmiParserService.getParentPackageFromNode(element);
                    if (StringUtils.isNotEmpty(parentId)) {
                        resource.addRelations(new Relation.Builder()
                                .with(Pakke.Relasjonsnavn.OVERORDNET)
                                .forType(Pakke.class)
                                .field("id")
                                .value(getId(parentId))
                                .build()
                        );
                    }
                    xmiParserService
                            .getChildPackagesByIdRef(xmiParserService.getIdRefFromNode(element))
                            .stream()
                            .map(xmiParserService::getIdRefFromNode)
                            .map(this::getId)
                            .map(id -> new Relation.Builder()
                                    .with(Pakke.Relasjonsnavn.UNDERORDNET)
                                    .forType(Pakke.class)
                                    .field("id")
                                    .value(id)
                                    .build())
                            .forEach(resource::addRelations);
                    return resource;

                });
    }

    public Stream<FintResource> getClasses() {
        try {
            log.info("Start getting classes");
            return xmiParserService
                    .getClasses()
                    .stream()
                    .map(element -> {
                        Klasse klasse = getFintKlasse(element);
                        List<Relation> relationList = new ArrayList<>();

                        addInheritanceFromRelation(element, relationList);
                        addPackageRelation(element, relationList);
                        addClassRelations(relationList, xmiParserService.getIdRefFromNode(element));

                        return FintResource.with(klasse).addRelations(relationList);
                    });
        } finally {
            log.info("End getting classes");
        }

    }

    public Stream<FintResource> getRelations() {

        try {
            log.info("Start getting relations");
            return xmiParserService
                    .getAssociations()
                    .stream()
                    .map(relation ->
                            FintResource
                                    .with(getFintRelasjon(relation))
                                    .addRelations(
                                            addRelationClasses(
                                                    xmiParserService.getIdRefFromNode(relation))));

        } finally {
            log.info("End getting relations");

        }


    }

    public void addClassRelations(List<Relation> relationList, String idref) {

        xmiParserService
                .getClassRelations(idref)
                .forEach(relation -> relationList.add(new Relation.Builder()
                        .with(Klasse.Relasjonsnavn.RELASJON)
                        .forType(Relasjon.class)
                        .field("id")
                        .value(getRelasjonId(relation))
                        .build()
                ));

    }

    private List<Relation> addRelationClasses(String idref) {
        return Arrays.asList(
                new Relation.Builder()
                        .with(Relasjon.Relasjonsnavn.KILDE)
                        .forType(Klasse.class)
                        .field("id")
                        .value(getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationSource(idref))))
                        .build(),

                new Relation.Builder()
                        .with(Relasjon.Relasjonsnavn.MAL)
                        .forType(Klasse.class)
                        .field("id")
                        .value(getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationTarget(idref))))
                        .build()
        );
    }

    private void addPackageRelation(Object node, List<Relation> relationList) {
        relationList.add(new Relation.Builder()
                .with(Klasse.Relasjonsnavn.PAKKE)
                .forType(Pakke.class)
                .field("id")
                .value(getId(xmiParserService.getParentPackageByIdRef(xmiParserService.getIdRefFromNode(node))))
                .build()
        );
    }

    private void addInheritanceFromRelation(Object node, List<Relation> relationList) {
        String arverId = xmiParserService.getInheritFromId(xmiParserService.getIdRefFromNode(node));
        if (StringUtils.isNotEmpty(arverId)) {
            Relation arverRelation = new Relation.Builder().
                    with(Klasse.Relasjonsnavn.ARVER)
                    .forType(Klasse.class)
                    .field("id")
                    .value(getId(arverId))
                    .build();
            relationList.add(arverRelation);
        }
    }

    public Pakke getFintPakke(Object item) {

        Pakke pakke = new Pakke();
        pakke.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        pakke.setNavn(xpath.getStringValue(item, "@name"));
        pakke.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));
        return pakke;
    }

    public Klasse getFintKlasse(Object item) {


        List<Attributt> attributtList =
                xpath.getNodeList(item, "attributes/attribute")
                        .stream()
                        .map(this::getFintAttributt)
                        .collect(Collectors.toList());


        Klasse klasse = new Klasse();

        klasse.setAbstrakt(Boolean.valueOf(xpath.getStringValue(item, "properties/@isAbstract")));
        klasse.setAttributter(attributtList);
        klasse.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(item, "properties/@documentation")));
        klasse.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        klasse.setNavn(xpath.getStringValue(item, "@name"));
        klasse.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));

        return klasse;
    }

    private Attributt getFintAttributt(Object attribute) {

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

    public Relasjon getFintRelasjon(Object relation) {

        Relasjon relasjon = new Relasjon();

        relasjon.setNavn(xpath.getStringValue(relation, "target/role/@name"));
        relasjon.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(relation, "target/documentation/@value")));
        relasjon.setMultiplisitet(Collections.singletonList(FintFactory.getMultiplisitetFromString(xpath.getStringValue(relation, "target/type/@multiplicity"))));
        relasjon.setId(FintFactory.getIdentifikator(getRelasjonId(relation)));

        return relasjon;
    }

    private String getId(String idref) {
        List<String> idElements = new ArrayList<>();

        idElements.add(xmiParserService.getName(idref));

        String parentPackageId = xmiParserService.getParentPackageByIdRef(idref);
        idElements.add(xmiParserService.getName(parentPackageId));
        while (!xmiParserService.getParentPackageByIdRef(parentPackageId).isEmpty()) {
            parentPackageId = xmiParserService.getParentPackageByIdRef(parentPackageId);
            idElements.add(xmiParserService.getName(parentPackageId));
        }

        Collections.reverse(idElements);
        String id = String.join(".", idElements).toLowerCase();

        return stripNationalCharacters(id)
                .replace("model", "no")
                .replace(" ", "");
    }

    private String stripNationalCharacters(String id) {
        return StringUtils.stripAccents(id)
                .replace('æ', 'a')
                .replace('ø', 'o');
    }

    public String getRelasjonId(Object relation) {
        return String.format("%s_%s",
                getId(xpath.getStringValue(relation, "source/@xmi:idref")),
                stripNationalCharacters(xpath.getStringValue(relation, "target/role/@name"))
        );
    }

}
