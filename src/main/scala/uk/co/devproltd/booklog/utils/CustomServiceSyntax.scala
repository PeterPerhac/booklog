package uk.co.devproltd.booklog.utils

import io.circe.Json
import io.circe.generic.JsonCodec
import io.circe.syntax._

trait CustomServiceSyntax {

  @JsonCodec case class GenericResponse(message: String, success: Boolean)

  implicit class StringOps(string: String) {
    def asJsonError: Json = GenericResponse(message = string, success = false).asJson
    def asJsonSuccess: Json = GenericResponse(message = string, success = true).asJson
  }

}

object CustomServiceSyntax extends CustomServiceSyntax
