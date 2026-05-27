package com.saban

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test

class ApplicationTest {

    /**
     * Run db migrations
     */
    @BeforeSuite
    fun beforeSuite() {
        testApplication {
            application {
                module()
            }
            startApplication()
        }
    }

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").status shouldBe HttpStatusCode.OK
    }

}
