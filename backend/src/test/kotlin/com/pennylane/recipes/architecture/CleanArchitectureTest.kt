package com.pennylane.recipes.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.Test

class CleanArchitectureTest {
    @Test
    fun `layer dependencies follow clean architecture`() {
        layerDependencies.check(classes)
    }

    @Test
    fun `domain must not reach into config`() {
        domainMustNotReachIntoConfig.check(classes)
    }

    @Test
    fun `application must not reach into config`() {
        applicationMustNotReachIntoConfig.check(classes)
    }

    @Test
    fun `no cyclic dependencies`() {
        noCyclicDependencies.check(classes)
    }

    @Test
    fun `controllers must not depend on service implementations`() {
        controllersMustNotDependOnServiceImplementations.check(classes)
    }

    private val layerDependencies: ArchRule =
        Architectures
            .layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain")
            .definedBy("..domain..")
            .layer("Application")
            .definedBy("..application..")
            .layer("Adapter")
            .definedBy("..adapter..")
            .layer("Config")
            .definedBy("..config..")
            .whereLayer("Domain")
            .mayNotAccessAnyLayer()
            .whereLayer("Application")
            .mayOnlyAccessLayers("Domain")
            .whereLayer("Adapter")
            .mayOnlyAccessLayers("Application", "Domain")
            .withOptionalLayers(true)
            .because(
                "clean architecture: domain depends on nothing; application only on domain; " +
                    "adapters on application and domain; config wires everything",
            )

    private val domainMustNotReachIntoConfig: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..config..")
            .because("config wires the application; the domain must never depend on wiring")
            .allowEmptyShould(true)

    private val applicationMustNotReachIntoConfig: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..config..")
            .because("config wires the application; the application layer must never depend on wiring")
            .allowEmptyShould(true)

    private val noCyclicDependencies: ArchRule =
        SlicesRuleDefinition
            .slices()
            .matching("com.pennylane.recipes.(*)..")
            .should()
            .beFreeOfCycles()
            .because("cyclic dependencies cause architectural rot and make testing impossible")
            .allowEmptyShould(true)

    private val controllersMustNotDependOnServiceImplementations: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..adapter.inbound.web..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..application.service..")
            .because("controllers must inject use-case ports, never service implementations")

    private val classes: JavaClasses by lazy {
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.pennylane.recipes")
    }
}
