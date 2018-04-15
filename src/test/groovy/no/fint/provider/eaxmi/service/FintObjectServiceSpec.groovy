package no.fint.provider.eaxmi.service

import no.fint.model.metamodell.Relasjon
import spock.lang.Specification

class FintObjectServiceSpec extends Specification {

    FintObjectService fintObjectService
    XmiParserService xmiParserService
    XPathService xpath


    void setup() {
        xpath = new XPathService()
        //xpath.init()
        //xmiParserService = new XmiParserService(xpath: xpath)
        //xmiParserService.getXmiDocument()
        //fintObjectService = new FintObjectService(xmiParserService: xmiParserService, xpath: xpath)
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

    def "Get packages"() {

        when:
        def packages = fintObjectService.getPackages()

        then:
        packages.size() > 0
        packages.get(0).getRelations().size() > 0
    }

    def "Get classes"() {

        when:
        def classes = fintObjectService.getClasses()

        then:
        classes.size() > 0
        classes.get(0).getRelations().size() > 0
    }

    def "Transform XMI package to FINT pakke"() {

        given:
        def packages = xmiParserService.getPackages()

        when:
        def pakke = fintObjectService.getFintPakke(packages.item(0))

        then:
        pakke.getNavn() == "FINT"
        pakke.getId().identifikatorverdi == "no.fint"
    }
    def "Transform XMI class to FINT klasse"() {
        given:
        def personItem = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF).item(0)

        when:
        def personKlasse = fintObjectService.getFintKlasse(personItem)

        then:
        personKlasse.getNavn() == "Person"
        personKlasse.getId().identifikatorverdi == "no.fint.felles.person"
        personKlasse.getAttributter().size() > 0
    }

    def "Transform XMI attribute to FINT attributt"() {
        given:
        def personItem = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF).item(0)
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
        def relation = xmiParserService.getAssociations().item(0)

        when:
        def relasjon = fintObjectService.getFintRelasjon(relation)

        then:
        relasjon.getNavn() == "statsborgerskap"
        relasjon.getMultiplisitet() != null
        relasjon.dokumentasjon.size() > 0
    }

    def "Get relasjon id"() {
        given:
        def relation = xmiParserService.getAssociations().item(0)

        when:
        def id = fintObjectService.getRelasjonId(relation)

        then:
        id == "no.fint.felles.person.relasjon.statsborgerskap"

    }

    def "Add klasse relasjoner"() {
        given:
        def classRelations = new ArrayList<>()

        when:
        fintObjectService.addClassRelations(classRelations, Constants.CLASS_PERSON_IDREF)

        then:
        classRelations.size() > 0

    }

    def "See if saxon rules"() {
        given:
        def xpath = new XPathService()
        ClassLoader classLoader = getClass().getClassLoader()
        File file = new File(classLoader.getResource("FINT-informasjonsmodell.xml").getFile())
        xpath.initializeSAXParser(file)

        when:
        def value = xpath.getStringValue(null, "//element[@xmi:idref='EAPK_10BA7EEB_F77B_4f6d_B1DF_0FEFDFD6D73F']/@name")

        then:
        value == "Basisklasser"

    }
}
