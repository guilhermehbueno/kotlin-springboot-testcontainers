package org.example.app

import org.example.app.models.User
import org.example.app.repositories.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
@ExtendWith(SpringExtension::class)
@SpringBootTest
@DirtiesContext
class AppKtTest {

    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    lateinit var ds: DataSource

    @Autowired
    lateinit var repository: UserRepository

    companion object {
        @Container
        val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:latest").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @Container
        var rabbitMQContainer = RabbitMQContainer("rabbitmq:3.7.25-management-alpine").apply {
            withExposedPorts(5672)
            withEnv("RABBITMQ_DEFAULT_USER", "guest") // Optional: Set default user
            withEnv("RABBITMQ_DEFAULT_PASS", "guest") // Optional: Set default password
        }

        /**
         * Configures the dynamic properties for the test.
         * It will override the database properties with the ones from the container.
         */
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            rabbitMQContainer.waitingFor(
                Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofSeconds(30))
            )
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)

            registry.add("spring.rabbitmq.host") { rabbitMQContainer.host }
            registry.add("spring.rabbitmq.port") { rabbitMQContainer.getMappedPort(5672) }
        }
    }


    @Test
    fun test() {
        assertNotNull(context)
        assertNotNull(ds)
        assertNotNull(repository)


        ds.connection.use {
            val stmt = it.createStatement()
            val rs = stmt.executeQuery("SELECT 1")
            rs.next()
            Assertions.assertEquals(1, rs.getInt(1))
        }


        repository.findAll().forEach {
            println(it)
        }


        assertEquals(0, repository.count())
        repository.save(User(username = "Gui", email = "gui@gmail.com", password = ""))
        assertEquals(1, repository.count())

        val result = repository.findByEmail("gui@gmail.com")
        assertNotNull(result)
        assertEquals("Gui", result!!.username)

    }
}

