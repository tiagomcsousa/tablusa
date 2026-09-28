package io.github.tiagomcsousa.tablusa;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Dependency rules of the ports-and-adapters architecture (decisions 010 and 012).
 *
 * <p>Rules that select classes by layer allow an empty selection, so they pass before the
 * first feature package exists.
 */
@AnalyzeClasses(packages = "io.github.tiagomcsousa.tablusa", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domainDependsOnlyOnJavaAndJSpecify = classes()
		.that().resideInAPackage("..domain..")
		.should().onlyDependOnClassesThat()
		.resideInAnyPackage("java..", "org.jspecify.annotations..", "..domain..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule applicationDependsOnlyOnDomainJavaAndJSpecify = classes()
		.that().resideInAPackage("..application..")
		.should().onlyDependOnClassesThat()
		.resideInAnyPackage("java..", "org.jspecify.annotations..", "..domain..", "..application..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule adaptersDoNotDependOnEachOther = slices()
		.matching("io.github.tiagomcsousa.tablusa.(*).adapter.(*).(*)..")
		.should().notDependOnEachOther()
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule jakartaPersistenceOnlyInPersistenceAdapter = noClasses()
		.that().resideOutsideOfPackage("..adapter.out.persistence..")
		.should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

	@ArchTest
	static final ArchRule controllersDependOnlyOnInboundPorts = noClasses()
		.that().resideInAPackage("..adapter.in.web..")
		.should().dependOnClassesThat()
		.resideInAnyPackage("..domain..", "..application.port.out..", "..application.service..")
		.allowEmptyShould(true);

	// Undecorated services are wired only in config/, so nothing else can bypass the
	// transaction decorator.
	@ArchTest
	static final ArchRule servicesAreUsedOnlyByConfig = classes()
		.that().resideInAPackage("..application.service..")
		.should().onlyHaveDependentClassesThat()
		.resideInAnyPackage("..application.service..", "..config..")
		.allowEmptyShould(true);

}
