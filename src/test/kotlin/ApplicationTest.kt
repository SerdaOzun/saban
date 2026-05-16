package com.saban

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.testng.annotations.Test

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").status shouldBe HttpStatusCode.OK
    }

}
