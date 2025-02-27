package com.santaclose.app.file.service

import arrow.core.left
import arrow.core.right
import aws.smithy.kotlin.runtime.content.ByteStream
import com.santaclose.lib.web.req.UploadImageRequest
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono

internal class FileAppServiceTest {
  private val fileManager = mockk<FileManager>()
  private val fileAppService = FileAppService(fileManager)

  @Nested
  inner class UploadImage {
    @Test
    fun `파일 유효성 검사 실패시 left를 반환한다`() = runTest {
      // given
      val request = mockk<UploadImageRequest>(relaxed = true)
      val exception = IllegalArgumentException()
      every { request.fileData } returns ByteStream.fromString("").toMono()
      every { request.validateFile() } returns exception.left()

      // when
      val result = fileAppService.uploadImage(request)

      // then
      result shouldBeLeft exception
    }

    @Test
    fun `파일 업로드 실패 시 left를 반환한다`() = runTest {
      // given
      val request = mockk<UploadImageRequest>(relaxed = true)
      every { request.fileData } returns ByteStream.fromString("").toMono()
      every { request.validateFile() } returns Unit.right()

      val exception = Exception("file upload error")
      coEvery { fileManager.upload(request.path, any(), request.contentType) } returns
        exception.left()

      // when
      val result = fileAppService.uploadImage(request)

      // then
      result shouldBeLeft exception
    }

    @Test
    fun `정상적으로 파일이 업로드된다`() = runTest {
      // given
      val request = mockk<UploadImageRequest>(relaxed = true)
      every { request.fileData } returns ByteStream.fromString("").toMono()
      every { request.validateFile() } returns Unit.right()

      val url = "http://localhost/image.png"
      coEvery { fileManager.upload(request.path, any(), request.contentType) } returns url.right()

      // when
      val result = fileAppService.uploadImage(request)

      // then
      result shouldBeRight url
    }
  }
}
