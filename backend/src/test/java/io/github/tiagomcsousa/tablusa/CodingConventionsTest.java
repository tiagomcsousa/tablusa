package io.github.tiagomcsousa.tablusa;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Idioms from backend/CLAUDE.md that can be checked on bytecode (decision 005).
 */
@AnalyzeClasses(packages = "io.github.tiagomcsousa.tablusa", importOptions = DoNotIncludeTests.class)
class CodingConventionsTest {

	// MapStruct annotates generated constructors with @Autowired, even with constructor injection.
	private static final DescribedPredicate<JavaClass> notMapStructImplementations = describe(
		"are not MapStruct implementations",
		javaClass -> javaClass.getAllRawInterfaces().stream()
			.noneMatch(type -> type.isAnnotatedWith("org.mapstruct.Mapper"))
			&& javaClass.getAllRawSuperclasses().stream()
				.noneMatch(type -> type.isAnnotatedWith("org.mapstruct.Mapper")));

	@ArchTest
	static final ArchRule noInjectionAnnotations = noClasses()
		.that(notMapStructImplementations)
		.should().dependOnClassesThat()
		.haveFullyQualifiedName("org.springframework.beans.factory.annotation.Autowired")
		.orShould().dependOnClassesThat().haveFullyQualifiedName("jakarta.inject.Inject")
		.orShould().dependOnClassesThat().haveFullyQualifiedName("jakarta.annotation.Resource")
		.because("only constructor injection is used, and it needs no annotation");

	@ArchTest
	static final ArchRule noValueFieldInjection = noFields()
		.should().beAnnotatedWith("org.springframework.beans.factory.annotation.Value")
		.because("only constructor injection is used")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule noJavaxEnterpriseApis = noClasses()
		.should().dependOnClassesThat()
		.resideInAnyPackage("javax.persistence..", "javax.validation..", "javax.servlet..", "javax.annotation..")
		.because("Spring Boot 4 uses jakarta.*");

	@ArchTest
	static final ArchRule noJackson2Databind = noClasses()
		.should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson.databind..")
		.because("Jackson 3 lives in tools.jackson.*");

	@ArchTest
	static final ArchRule noRestTemplate = noClasses()
		.should().dependOnClassesThat()
		.haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
		.because("RestClient replaces RestTemplate");

	@ArchTest
	static final ArchRule noWebClient = noClasses()
		.should().dependOnClassesThat().resideInAPackage("org.springframework.web.reactive..")
		.because("the application is Spring MVC; use RestClient");

}
