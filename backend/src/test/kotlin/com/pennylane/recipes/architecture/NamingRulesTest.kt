package com.pennylane.recipes.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import jakarta.persistence.Entity
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.RestController

class NamingRulesTest {
    @Test
    fun `use case interfaces end with UseCase`() {
        useCaseInterfacesMustHaveSuffix.check(classes)
    }

    @Test
    fun `application services end with Service`() {
        applicationServicesMustHaveSuffix.check(classes)
    }

    @Test
    fun `commands and queries end with Command or Query`() {
        commandsAndQueriesMustHaveSuffix.check(classes)
    }

    @Test
    fun `controllers end with Controller`() {
        controllersMustHaveSuffix.check(classes)
    }

    @Test
    fun `repository adapters end with Adapter`() {
        repositoryAdaptersMustHaveSuffix.check(classes)
    }

    @Test
    fun `Spring Data repositories end with Repository`() {
        springDataRepositoriesMustHaveSuffix.check(classes)
    }

    @Test
    fun `JPA entities end with Entity`() {
        jpaEntitiesMustHaveSuffix.check(classes)
    }

    @Test
    fun `domain outbound ports end with Port or Repository`() {
        domainPortsMustHaveSuffix.check(classes)
    }

    private val useCaseInterfacesMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..application.port.inbound..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("UseCase")
            .because("use case interfaces end with UseCase")
            .allowEmptyShould(true)

    private val applicationServicesMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .haveSimpleNameEndingWith("Service")
            .because("application services end with Service")
            .allowEmptyShould(true)

    private val commandsAndQueriesMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..application.command..")
            .should()
            .haveSimpleNameEndingWith("Command")
            .orShould()
            .haveSimpleNameEndingWith("Query")
            .because("use case inputs end with Command or Query")
            .allowEmptyShould(true)

    private val controllersMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..adapter.inbound.web.controller..")
            .and()
            .areAnnotatedWith(RestController::class.java)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .because("REST controllers end with Controller")
            .allowEmptyShould(true)

    private val repositoryAdaptersMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..adapter.outbound.persistence.adapter..")
            .should()
            .haveSimpleNameEndingWith("Adapter")
            .because("repository adapters end with Adapter, e.g. RecipeRepositoryAdapter")
            .allowEmptyShould(true)

    private val springDataRepositoriesMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..adapter.outbound.persistence.repository..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("Repository")
            .because("Spring Data JPA repositories end with Repository, e.g. RecipeJpaRepository")
            .allowEmptyShould(true)

    private val jpaEntitiesMustHaveSuffix: ArchRule =
        classes()
            .that()
            .areAnnotatedWith(Entity::class.java)
            .should()
            .haveSimpleNameEndingWith("Entity")
            .because("JPA entities end with Entity")
            .allowEmptyShould(true)

    private val domainPortsMustHaveSuffix: ArchRule =
        classes()
            .that()
            .resideInAPackage("..domain.port.outbound..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("Port")
            .orShould()
            .haveSimpleNameEndingWith("Repository")
            .because("domain outbound ports end with Port or Repository")
            .allowEmptyShould(true)

    private val classes: JavaClasses by lazy {
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.pennylane.recipes")
    }
}
