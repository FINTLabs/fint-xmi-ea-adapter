package no.fint.provider.eaxmi.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.metamodell.Klasse;
import no.fint.model.metamodell.Kontekst;
import no.fint.model.metamodell.Relasjon;
import no.fint.model.metamodell.kompleksedatatyper.Attributt;
import no.fint.model.resource.Link;
import no.fint.model.resource.metamodell.KlasseResource;
import no.fint.model.resource.metamodell.KontekstResource;
import no.fint.model.resource.metamodell.RelasjonResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

    @Scheduled(initialDelay = 900000, fixedDelayString = "${fint.eaxmi.interval:900000}")
    public void update() {
        log.info("Parsing XMI document...");
        xmiParserService.getXmiDocument();
        log.info("Parsing XMI document complete.");
    }

    @PostConstruct
    public void init() {
        update();
    }

    public Stream<KontekstResource> getContexts() {

        return xmiParserService
                .getPackages()
                .stream()
                .map(element -> {
                    KontekstResource kontekst = getFintKontekst(element);

                    xmiParserService
                            .getClassesInPackage(
                                    xmiParserService.getIdRefFromNode(element))
                            .stream()
                            .map(xmiParserService::getIdRefFromNode)
                            .map(this::getId)
                            .map(Link.apply(Klasse.class, "id"))
                            .forEach(kontekst::addKlasse);

                    String parentId = xmiParserService.getParentPackageFromNode(element);
                    if (StringUtils.isNotEmpty(parentId)) {
                        kontekst.addOverordnet(Link.with(Kontekst.class, "id", getId(parentId)));
                    }

                    xmiParserService
                            .getChildPackagesByIdRef(xmiParserService.getIdRefFromNode(element))
                            .stream()
                            .map(xmiParserService::getIdRefFromNode)
                            .map(this::getId)
                            .map(Link.apply(Kontekst.class, "id"))
                            .forEach(kontekst::addUnderordnet);

                    return kontekst;
                });
    }

    public Stream<KlasseResource> getClasses() {
        try {
            log.info("Start getting classes");
            return xmiParserService
                    .getClasses()
                    .stream()
                    .map(element -> {
                        KlasseResource klasse = getFintKlasse(element);
                        addInheritanceFromRelation(element, klasse);
                        addContextRelation(element, klasse);
                        addClassRelations(klasse, xmiParserService.getIdRefFromNode(element));

                        return klasse;
                    });
        } finally {
            log.info("End getting classes");
        }

    }

    public Stream<RelasjonResource> getRelations() {

        try {
            log.info("Start getting relations");
            return xmiParserService
                    .getAssociations()
                    .stream()
                    .flatMap(this::getFintRelasjon);

        } finally {
            log.info("End getting relations");

        }
    }

    public void addClassRelations(KlasseResource klasse, String idref) {

        xmiParserService
                .getClassForwardRelations(idref)
                .stream()
                .map(this::getForwardRelasjonId)
                .map(Link.apply(Relasjon.class, "id"))
                .forEach(klasse::addRelasjon);

        xmiParserService
                .getClassReverseRelations(idref)
                .stream()
                .map(this::getReverseRelasjonId)
                .map(Link.apply(Relasjon.class, "id"))
                .forEach(klasse::addRelasjon);
    }

    private void addForwardRelationClasses(RelasjonResource relasjon, String idref) {
        relasjon.addKilde(Link.with(Klasse.class, "id",
                getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationSource(idref)))));

        relasjon.addMal(Link.with(Klasse.class, "id",
                getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationTarget(idref)))));
    }

    private void addReverseRelationClasses(RelasjonResource relasjon, String idref) {
        relasjon.addKilde(Link.with(Klasse.class, "id",
                getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationTarget(idref)))));

        relasjon.addMal(Link.with(Klasse.class, "id",
                getId(xmiParserService.getIdRefFromNode(xmiParserService.getRelationSource(idref)))));
    }

    private void addContextRelation(Object node, KlasseResource klasse) {
        klasse.addKontekst(
                Link.with(Kontekst.class, "id",
                        getId(xmiParserService.getParentPackageByIdRef(xmiParserService.getIdRefFromNode(node)))));
    }

    private void addInheritanceFromRelation(Object node, KlasseResource klasse) {
        String arverId = xmiParserService.getInheritFromId(xmiParserService.getIdRefFromNode(node));
        if (StringUtils.isNotEmpty(arverId)) {
            klasse.addArver(Link.with(Klasse.class, "id", getId(arverId)));
        }
    }

    public KontekstResource getFintKontekst(Object item) {

        KontekstResource kontekst = new KontekstResource();
        kontekst.setId(FintFactory.getIdentifikator(getId(xpath.getStringValue(item, "@xmi:idref"))));
        kontekst.setNavn(xpath.getStringValue(item, "@name"));
        kontekst.setStereotype(xpath.getStringValue(item, "properties/@stereotype"));
        return kontekst;
    }

    public KlasseResource getFintKlasse(Object item) {


        List<Attributt> attributtList =
                xpath.getNodeList(item, "attributes/attribute")
                        .stream()
                        .map(this::getFintAttributt)
                        .collect(Collectors.toList());


        KlasseResource klasse = new KlasseResource();

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

    public Stream<RelasjonResource> getFintRelasjon(Object relation) {

        Stream.Builder<RelasjonResource> result = Stream.builder();

        if (StringUtils.isNotEmpty(xpath.getStringValue(relation, "target/role/@name"))) {
            RelasjonResource relasjon = new RelasjonResource();
            relasjon.setNavn(xpath.getStringValue(relation, "target/role/@name"));
            relasjon.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(relation, "target/documentation/@value")));
            relasjon.setMultiplisitet(Collections.singletonList(FintFactory.getMultiplisitetFromString(xpath.getStringValue(relation, "target/type/@multiplicity"))));
            relasjon.setId(FintFactory.getIdentifikator(getForwardRelasjonId(relation)));
            addForwardRelationClasses(relasjon, xmiParserService.getIdRefFromNode(relation));
            result.accept(relasjon);
        }

        if (StringUtils.isNotEmpty(xpath.getStringValue(relation, "source/role/@name"))) {
            RelasjonResource relasjon = new RelasjonResource();
            relasjon.setNavn(xpath.getStringValue(relation, "source/role/@name"));
            relasjon.setDokumentasjon(FintFactory.getDokumentasjon(xpath.getStringValue(relation, "source/documentation/@value")));
            relasjon.setMultiplisitet(Collections.singletonList(FintFactory.getMultiplisitetFromString(xpath.getStringValue(relation, "source/type/@multiplicity"))));
            relasjon.setId(FintFactory.getIdentifikator(getReverseRelasjonId(relation)));
            addReverseRelationClasses(relasjon, xmiParserService.getIdRefFromNode(relation));
            result.accept(relasjon);
        }

        return result.build();
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

    public String getForwardRelasjonId(Object relation) {
        return String.format("%s_%s",
                getId(xpath.getStringValue(relation, "source/@xmi:idref")),
                stripNationalCharacters(xpath.getStringValue(relation, "target/role/@name"))
        );
    }

    public String getReverseRelasjonId(Object relation) {
        return String.format("%s_%s",
                getId(xpath.getStringValue(relation, "target/@xmi:idref")),
                stripNationalCharacters(xpath.getStringValue(relation, "source/role/@name"))
        );
    }

}
