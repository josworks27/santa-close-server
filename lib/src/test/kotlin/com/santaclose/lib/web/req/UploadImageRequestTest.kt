package com.santaclose.lib.web.req

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart

internal class UploadImageRequestTest {

  @Test
  fun `path를 생성한다`() {
    // given
    var file = mockk<FilePart>()
    every { file.filename() } returns "image.png"
    val givenDate = LocalDate.now()

    // when
    val request = UploadImageRequest(file, givenDate)

    // then
    request.path shouldContain "image/${givenDate}"
    request.path shouldContain file.filename()
  }

  @Nested
  inner class ValidateFile {
    @Test
    fun `파일에 대한 유효성 검사를 한다`() {
      // given
      var file = mockk<FilePart>()
      every { file.filename() } returns "image.png"
      every { file.headers() } returns HttpHeaders().apply { contentType = MediaType.IMAGE_PNG }
      val request = UploadImageRequest(file)

      // when
      val result = request.validateFile()

      // then
      result.shouldBeRight()
    }

    @Test
    fun `유효하지 않은 contentType 일시 에러를 발생한다`() {
      // given
      var file = mockk<FilePart>()
      every { file.headers() } returns
        HttpHeaders().apply { contentType = MediaType.APPLICATION_PDF }
      val request = UploadImageRequest(file)

      // when
      val result = request.validateFile()

      // then
      result.shouldBeLeft().apply { message shouldContain "파일만 업로드 가능합니다." }
    }

    @Test
    fun `유효하지 않은 확장자 일시 에러를 발생한다`() {
      // given
      var file = mockk<FilePart>()
      every { file.filename() } returns "image.pdf"
      every { file.headers() } returns HttpHeaders().apply { contentType = MediaType.IMAGE_PNG }
      val request = UploadImageRequest(file)

      // when
      val result = request.validateFile()

      // then
      result.shouldBeLeft().apply { message shouldContain "파일만 업로드 가능합니다." }
    }
  }
}
