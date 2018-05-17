package uk.co.devproltd.booklog

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import fs2.StreamApp
import io.circe.Json
import io.circe.generic.JsonCodec
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import uk.co.devproltd.booklog.repository.{BookRepository, LogEntryRepository}

object BooklogServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], onShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = app[IO]
  def tx[F[_]: Effect]: Aux[F, Unit] =
    Transactor.fromDriverManager[F]("org.postgresql.Driver", "jdbc:postgresql:booklog", "booklog", "booklog")

  def app[F[_]: Effect]: fs2.Stream[F, StreamApp.ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new BooklogService[F](new BookRepository[F], new LogEntryRepository[F], tx).service, "/")
      .serve

}

class BooklogService[F[_]: Effect](
  bookRepository: BookRepository[F],
  logEntryRepository: LogEntryRepository[F],
  transactor: Transactor[F])
    extends ServiceBase[F] {

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "books" =>
      Ok(bookRepository.findAll.transact(transactor).compile.toList.map(_.asJson))
    case GET -> Root / "books" / IntVar(bookId) =>
      for {
        lookupRes <- bookRepository.find(bookId.toInt).transact(transactor)
        res       <- lookupRes.fold(NotFound(s"Book ID=$bookId was not found".asJsonError))(book => Ok(book.asJson))
      } yield res
    case DELETE -> Root / "books" / IntVar(bookId) =>
      val delete = for {
        _ <- logEntryRepository.deleteBookEntries(bookId)
        n <- bookRepository.delete(bookId)
      } yield n
      Ok(delete.transact(transactor).map(n => s"$n book deleted".asJsonSuccess))
  }

}

abstract class ServiceBase[F[_]: Effect] extends Http4sDsl[F] {

  @JsonCodec case class GenericResponse(message: String, success: Boolean)

  implicit class StringOps(string: String) {
    def asJsonError: Json = GenericResponse(message = string, success = false).asJson

    def asJsonSuccess: Json = GenericResponse(message = string, success = true).asJson
  }

}
