package com.pennylane.recipes.recipe.adapter.outbound.persistence

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Shared real-Postgres base for integration tests: one container per JVM run.
 * The container is started manually instead of via @Testcontainers/@Container —
 * the JUnit extension would stop it after every test class while Spring keeps
 * contexts cached across classes, leaving cached datasources pointing at a
 * dead port. The default seed source is a classpath fixture so no test context
 * can ever reach the network; classes override it with their own fixture.
 */
@Tag("Integration")
@SpringBootTest(properties = ["app.seed.url=classpath:seeding/search-fixture.json"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class AbstractPostgresIT {
    companion object {
        @JvmStatic
        @ServiceConnection
        val postgres = PostgreSQLContainer<Nothing>("postgres:17").apply { start() }
    }
}
