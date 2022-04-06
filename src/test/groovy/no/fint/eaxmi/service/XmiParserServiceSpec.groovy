package no.fint.eaxmi.service

import spock.lang.Specification

class XmiParserServiceSpec extends Specification {

    private XmiParserService xmiParserService
    private XPathService xpath

    void setup() {
        def uri = getClass().getResource('/FINT-informasjonsmodell.xml').toURI()
        xpath = new XPathService()
        //xpath.init()
        xmiParserService = new XmiParserService(xpath: xpath, uri: uri)
        xmiParserService.getXmiDocument()
    }

    def "Check to see if document is parsed"() {

        when:
        xmiParserService.getXmiDocument()

        then:
        xmiParserService.getClasses().size > 0
        xmiParserService.getPackages().size > 0
        xmiParserService.getAssociations().size > 0
    }

    def "Get idref from node"() {
        given:
        def classesInPackage = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF)
        def classPerson = classesInPackage.get(0)

        when:
        def personIdref = xmiParserService.getIdRefFromNode(classPerson)

        then:
        personIdref == Constants.CLASS_PERSON_IDREF

    }

    def "Get parent package from node"() {
        given:
        def classesInPackage = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF)
        def classPerson = classesInPackage.get(0)

        when:
        def parentId = xmiParserService.getParentPackageFromNode(classPerson)

        then:
        parentId == Constants.PACKAGE_FELLES_IDREF
    }

    def "Get parent package from idref"() {
        when:
        def parentId = xmiParserService.getParentPackageByIdRef(Constants.CLASS_PERSON_IDREF)

        then:
        parentId == Constants.PACKAGE_FELLES_IDREF
    }


    def "Get child packages from idref"() {
        when:
        def children = xmiParserService.getChildPackagesByIdRef(Constants.PACKAGE_FELLES_IDREF)
        children.each { println(it.getAttributeValue('', 'name') )}

        then:
        children.any { it.getAttributeValue('', 'name') == 'Basisklasser' }
    }

    def "Get inherit from id"() {
        when:
        def inheritFromId = xmiParserService.getInheritFromId(Constants.CLASS_PERSON_IDREF)

        then:
        inheritFromId == Constants.CLASS_AKTOR_IDREF
    }

    def "Get classes in package"() {
        when:
        def classesInPackage = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF)

        then:
        classesInPackage.size() == 1
    }

    def "Get parent package id by idref"() {
        when:
        def id = xmiParserService.getParentPackageByIdRef(Constants.CLASS_PERSON_IDREF)

        then:
        id == Constants.PACKAGE_FELLES_IDREF
    }

    def "Get name"() {

        when:
        def packageName = xmiParserService.getName(Constants.PACKAGE_FELLES_IDREF)

        then:
        packageName == "Felles"
    }

    def "Get relations for a class"() {

        when:
        def relations = xmiParserService.getClassForwardRelations(Constants.CLASS_PERSON_IDREF)
        relations.each { println('-> ' + xmiParserService.getIdRefFromNode(it)) }

        then:
        relations.size() > 0

        when:
        relations = xmiParserService.getClassReverseRelations(Constants.CLASS_PERSON_IDREF)
        relations.each { println('<- ' + xmiParserService.getIdRefFromNode(it)) }

        then:
        relations.size() > 0

    }

    def "Get relation source node"() {

        when:
        def source = xmiParserService.getRelationSource(Constants.CONNECTOR_IDREF)

        then:
        source != null

    }

    def "Get relation target node"() {

        when:
        def target = xmiParserService.getRelationTarget(Constants.CONNECTOR_IDREF)

        then:
        target != null
    }

}
