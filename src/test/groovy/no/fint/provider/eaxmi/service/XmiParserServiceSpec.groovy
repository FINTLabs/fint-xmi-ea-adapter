package no.fint.provider.eaxmi.service

import spock.lang.Specification

class XmiParserServiceSpec extends Specification {

    private XmiParserService xmiParserService
    private XPathService xpath

    void setup() {
        xpath = new XPathService()
        xpath.init()
        xmiParserService = new XmiParserService(xpath: xpath)
        xmiParserService.getXmiDocument()
    }

    def "Check to see if document is parsed"() {

        when:
        xmiParserService.getXmiDocument()

        then:
        xmiParserService.getClasses().length > 0
        xmiParserService.getPackages().length > 0
        xmiParserService.getAssociations().length > 0
    }

    def "Get idref from node"() {
        given:
        def classesInPackage = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF)
        def classPerson = classesInPackage.item(0)

        when:
        def personIdref = xmiParserService.getIdRefFromNode(classPerson)

        then:
        personIdref == Constants.CLASS_PERSON_IDREF

    }

    def "Get parent package from node"() {
        given:
        def classesInPackage = xmiParserService.getClassesInPackage(Constants.PACKAGE_FELLES_IDREF)
        def classPerson = classesInPackage.item(0)

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
        classesInPackage.length == 1
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
        def relations = xmiParserService.getClassRelations(Constants.CLASS_PERSON_IDREF)

        then:
        relations.getLength() > 0


    }

}
