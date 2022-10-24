package com.acme.arzt

import com.acme.arzt.config.ProfilesBanner.banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Die Klasse, die beim Start des Hauptprogramms verwendet wird, um zu
 * konfigurieren, dass es sich um eine Anwendung mit _Spring Boot_ handelt.
 * Dadurch werden auch viele von Spring Boot gelieferte Konfigurationsklassen
 * automatisch konfiguriert.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
// https://spring.io/blog/2019/01/21/manual-bean-definitions-in-spring-boot
// https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-disabling-specific-auto-configuration
// https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/resources/META-INF/spring.factories
@SpringBootApplication(proxyBeanMethods = false)
class Application

/**
 * Hauptprogramm, um den Microservice zu starten.
 *
 * @param args Evtl. zusätzliche Argumente für den Start des Microservice
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<Application>(*args) { setBanner(banner) }
}
