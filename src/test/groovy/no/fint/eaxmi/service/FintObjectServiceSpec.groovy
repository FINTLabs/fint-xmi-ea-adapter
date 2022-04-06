package no.fint.eaxmi.service

import no.fint.model.resource.metamodell.KlasseResource
import spock.lang.Specification

import java.util.stream.Collectors

class FintObjectServiceSpec extends Specification {

    FintObjectService fintObjectService
    XmiParserService xmiParserService
    XPathService xpath


    void setup() {
        def uri = getClass().getResource('/FINT-informasjonsmodell.xml').toURI()
        xpath = new XPathService()
        //xpath.init()
        xmiParserService = new XmiParserService(xpath: xpath, uri: uri)
        xmiParserService.getXmiDocument()
        fintObjectService = new FintObjectService(xmiParserService: xmiParserService, xpath: xpath)
    }

    def "Get klasse id"() {

        when:
        def id = fintObjectService.getId(Constants.CLASS_PERSON_IDREF)

        then:
        id == "no.fint.felles.person"
    }

    def "Get pakke id"() {

        when:
        def id = fintObjectService.getId(Constants.PACKAGE_FELLES_IDREF)

        then:
        id == "no.fint.felles"
    }

    def "Get contexts"() {

        when:
        def contexts = fintObjectService.getContexts()

        then:
        contexts.noneMatch { it.getLinks().isEmpty() }
    }

    def "Get classes"() {

        when:
        def classes = fintObjectService.getClasses()

        then:
        classes.noneMatch { it.getLinks().isEmpty() }
    }

    def "Get relations"() {

        when:
        def relations = fintObjectService.getRelations()

        then:
        relations.count() > 0
    }

    def "Transform XMI package to FINT pakke"() {

        given:
        def packages = xmiParserService.getPackages()

        when:
        def pakke = fintObjectService.getFintKontekst(packages.get(0))

        then:
        pakke.getNavn() == "FINT"
    }

    def "Transform XMI class to FINT klasse"() {
        given:
        def personItem = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF).get(0)

        when:
        def personKlasse = fintObjectService.getFintKlasse(personItem)

        then:
        personKlasse.getNavn() == "Person"
        personKlasse.getAttributter().size() > 0
    }

    def "Transform XMI attribute to FINT attributt"() {
        given:
        def personItem = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF).get(0)
        def personKlasse = fintObjectService.getFintKlasse(personItem)

        when:
        def attributt = personKlasse.getAttributter().get(0)

        then:
        attributt.getNavn() == "bilde"
        attributt.getDokumentasjon().size() > 0
        attributt.getMultiplisitet() != null
        attributt.getType() == "string"

    }

    def "Transform XMI connector to FINT relasjon"() {
        given:
        def relation = xmiParserService.getAssociations().find { (xmiParserService.getIdRefFromNode(it) == Constants.CONNECTOR_IDREF) }

        when:
        def relasjon = fintObjectService.getFintRelasjon(relation).collect(Collectors.toList())
        relasjon.each { println(it) }

        then:
        relasjon.every { it.navn == "statsborgerskap" }
        relasjon.every { it.multiplisitet != null }
        relasjon.every { it.dokumentasjon.size() > 0 }
    }

    def "Get relasjon id"() {
        given:
        def relation = xmiParserService.getAssociations().get(0)

        when:
        def id = fintObjectService.getForwardRelasjonId(relation)

        then:
        id == 'no.fint.felles.person_statsborgerskap'

    }

    def "Add klasse relasjoner"() {
        given:
        def klasse = new KlasseResource()

        when:
        fintObjectService.addClassRelations(klasse, Constants.CLASS_PERSON_IDREF)
        klasse.each { println(it) }

        then:
        !klasse.getLinks().isEmpty()

    }

}
