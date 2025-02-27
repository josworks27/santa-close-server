package com.santaclose.lib.web.req

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import aws.smithy.kotlin.runtime.content.ByteStream
import java.time.LocalDate
import java.util.*
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

data class UploadImageRequest(
  val file: FilePart,
  var date: LocalDate = LocalDate.now(),
  var code: UUID = UUID.randomUUID(),
) {
  companion object {
    private val imageExtensions = arrayOf("jpg", "png", "jpeg")
    private val imageMimeTypes = arrayOf("image/jpg", "image/png", "image/jpeg")
  }

  private val fileName: String
    get() = file.filename()

  val fileData: Mono<ByteStream>
    get() =
      file.content().toMono().map { it?.asByteBuffer()?.array() ?: byteArrayOf() }.map {
        ByteStream.fromBytes(it)
      }

  val contentType: String
    get() = file.headers().contentType.toString()

  val path: String
    get() = "image/$date/$code-$fileName"

  fun validateFile(): Either<IllegalArgumentException, Unit> {

    if (!imageMimeTypes.contains(contentType.lowercase())) {
      return IllegalArgumentException("${imageExtensions.joinToString(",")} 파일만 업로드 가능합니다.").left()
    }

    val extension = this.fileName.split('.').last()
    if (!imageExtensions.contains(extension)) {
      return IllegalArgumentException("${imageExtensions.joinToString(",")} 파일만 업로드 가능합니다.").left()
    }

    return Unit.right()
  }
}
