package com.santaclose.lib.auth.kakao

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

internal class KakaoAuthTest {
    companion object {
        private lateinit var kakaoAuth: KakaoAuth
        private val server = MockWebServer()

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            server.start()
            kakaoAuth = KakaoAuth(
                builder = WebClient.builder(),
                clientId = "clientId",
                redirectUri = "http://localhost:8080",
                tokenUri = "http://localhost:${server.port}"
            )
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            server.shutdown()
        }
    }

    @Nested
    inner class GetAccessToken {
        @Test
        fun `토큰 발급에 실패한 경우 에러가 발생한다`() = runTest {
            // given
            server.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setBody("error")
            )

            // when
            val result = kakaoAuth.getAccessToken("code")

            // then
            result.shouldBeLeft().let {
                it.message shouldContain "토큰 발급 실패"
            }
        }

        @Test
        fun `토큰 요청의 응답이 올바르지 않으면 에러가 발생한다`() = runTest {
            // given
            server.enqueue(
                MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"foo\":\"bar\"}")
            )

            // when
            val result = kakaoAuth.getAccessToken("code")

            // then
            result.shouldBeLeft().let {
                it.message shouldContain "토큰 정보가 없습니다"
            }
        }

        @Test
        fun `응답이 올바르면 토큰을 반환한다`() = runTest {
            // given
            val token = "1234abcd"
            server.enqueue(
                MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"access_token\":\"$token\"}")
            )

            // when
            val result = kakaoAuth.getAccessToken("code")

            // then
            result shouldBeRight token
        }
    }
}
