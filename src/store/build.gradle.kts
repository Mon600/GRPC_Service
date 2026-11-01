plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.5"
}

group = "com.shastage"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

// Берём версии из auth сервиса в pom.xml, чтобы начать с той же пары генераторов.
val grpcVersion = "1.84.0"
val protobufVersion = "4.36.2"

dependencies {
	implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("io.grpc:grpc-stub:$grpcVersion")
	implementation("com.google.protobuf:protobuf-java:$protobufVersion")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.mapstruct:mapstruct:1.6.3")

	compileOnly("org.projectlombok:lombok")
	compileOnly("javax.annotation:javax.annotation-api:1.3.2") // Для protobuf

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("me.paulschwarz:springboot4-dotenv:5.1.0")

	runtimeOnly("org.postgresql:postgresql")

	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}

sourceSets {
	named("main") {
		proto {
			setSrcDirs(
				listOf("../contracts/proto")
			)
		}
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protobufVersion"
	}

	plugins {
		create("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
		}
	}

	generateProtoTasks {
		all().forEach { task ->
			task.plugins {
				maybeCreate("grpc")
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
