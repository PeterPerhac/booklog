package uk.co.devproltd.booklog

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.implicits._
import fs2.StreamApp
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import uk.co.devproltd.booklog.utils.CustomServiceSyntax

import scala.language.higherKinds

object BooklogServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], onShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = app[IO]

  def app[F[_]: Effect]: fs2.Stream[F, StreamApp.ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new HelloService[F].service)
      .mountService(new BooklogService[F](new BookRepository[F], new LogEntryRepository[F]).service, "/")
      .serve

}

class HelloService[F[_]: Effect] extends Http4sDsl[F] {
  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj(("message", Json.fromString(s"Hello, $name!"))))
  }
}

class BooklogService[F[_]: Effect](bookRepository: BookRepository[F], logEntryRepository: LogEntryRepository[F])
    extends Http4sDsl[F] with CustomServiceSyntax {
  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "books" =>
      Ok(bookRepository.findAll().map(_.asJson))
    case GET -> Root / "books" / IntVar(bookId) =>
      for {
        lookupRes <- bookRepository.find(bookId.toInt)
        res       <- lookupRes.fold(NotFound(s"Book ID=$bookId was not found".asJsonError))(book => Ok(book.asJson))
      } yield res
    case DELETE -> Root / "books" / IntVar(bookId) =>
      for {
        _        <- logEntryRepository.deleteBookEntries(bookId)
        nDeleted <- bookRepository.delete(bookId)
        res      <- Ok(s"$nDeleted books deleted".asJsonSuccess)
      } yield res
  }
}

//irritating import must be here for the repositories to work
import uk.co.devproltd.booklog.utils.CustomDoobieFluff._

class BookRepository[F[_]: Effect] extends Repository[F, Book, Int]("book")

class LogEntryRepository[F[_]: Effect] extends Repository[F, LogEntry, Int]("log_entry") {

  def deleteBookEntries(bookId: Int): F[Int] =
    sql"delete from log_entry where book_id=$bookId".update.run.transact(transactor)

}
