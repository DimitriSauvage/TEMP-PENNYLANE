package com.pennylane.recipes.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class DomainIsolationTest {
    @Test
    fun `domain must not use Spring`() {
        domainMustNotUseSpring.check(classes)
    }

    @Test
    fun `domain must not use jakarta`() {
        domainMustNotUseJakarta.check(classes)
    }

    @Test
    fun `domain must not use Jackson`() {
        domainMustNotUseJackson.check(classes)
    }

    @Test
    fun `application must not use HTTP types`() {
        applicationMustNotUseHttpTypes.check(classes)
    }

    @Test
    fun `application must not use JPA`() {
        applicationMustNotUseJpa.check(classes)
    }

    private val domainMustNotUseSpring: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("domain layer must be framework-free")
            .allowEmptyShould(true)

    private val domainMustNotUseJakarta: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("jakarta..")
            .because("domain layer must have no persistence annotations")
            .allowEmptyShould(true)

    private val domainMustNotUseJackson: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.fasterxml.jackson..")
            .because("domain layer must have no serialization annotations")
            .allowEmptyShould(true)

    private val applicationMustNotUseHttpTypes: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("jakarta.servlet..", "org.springframework.http..", "org.springframework.web..")
            .because("application layer must not know about HTTP")
            .allowEmptyShould(true)

    private val applicationMustNotUseJpa: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..", "org.springframework.data..")
            .because("application layer must not use persistence framework")
            .allowEmptyShould(true)

    private val classes: JavaClasses by lazy {
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.pennylane.recipes")
    }
}
