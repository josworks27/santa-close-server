plugins {
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("jvm")
  kotlin("plugin.spring")
  kotlin("plugin.jpa")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.expediagroup", "graphql-kotlin-spring-server", "5.3.2")
  implementation("org.hibernate:hibernate-spatial")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("mysql:mysql-connector-java")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
  testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.1"))
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:localstack")
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.MappedSuperclass")
  annotation("javax.persistence.Embeddable")
}
