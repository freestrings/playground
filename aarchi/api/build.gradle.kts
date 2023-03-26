group = "fs.playground"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    //implementation("ch.qos.logback:logback-classic:1.3.5")
    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")
    //implementation("com.fasterxml.jackson.core:jackson-databind:2.9.3")
    //implementation("org.slf4j:slf4j-log4j12:1.6.6")
}