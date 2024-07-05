plugins {
    id("java")
    // Lombok
    id("io.freefair.lombok") version "8.6"


}

group = "dev.joseluisgs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Logger
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-simple:1.7.32")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.28") // usa la última versión de Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.28")

    // Project Reactor para programación reactiva
    implementation("io.projectreactor:reactor-core:3.6.7")
    // Testear Reactor
    testImplementation("io.projectreactor:reactor-test:3.6.7")

    // Vavr para programación funcional
    implementation("io.vavr:vavr:0.10.4")

    // Json con Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    // JDBI
    implementation("org.jdbi:jdbi3-core:3.45.2") // JDBI Core
    implementation("org.jdbi:jdbi3-sqlobject:3.45.2") // JDBI SQL Object
    // implementation("org.jdbi:jdbi3-sqlite:3.45.2") // JDBI SQLite
    // Driver para SQLite
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0") // Jackson con Retrofit
    implementation("com.jakewharton.retrofit:retrofit2-reactor-adapter:2.1.0") // Reactor con Retrofit


    // Test
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Mockito para nuestros test con JUnit 5
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito:mockito-core:5.12.0")


}

tasks.test {
    useJUnitPlatform()
}

// Hacer un Jar ejecutable
tasks.jar {
    manifest {
        // Clase principal
        attributes["Main-Class"] = "dev.joseluisgs.MainKt"
    }
    // Incluir dependencias
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    // Excluir duplicados
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


