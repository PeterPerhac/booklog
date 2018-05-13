package uk.co.devproltd.booklog

import cats.effect.Effect
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import uk.co.devproltd.booklog.repository.BookRepository

import scala.language.higherKinds

class HelloWorldService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService[F] {
    case GET                -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name! Welcome to my blazing fast site!!!")))
    case GET                -> Root / "books" =>
      Ok(BookRepository.findAll.unsafeRunSync().asJson)
  }
}
