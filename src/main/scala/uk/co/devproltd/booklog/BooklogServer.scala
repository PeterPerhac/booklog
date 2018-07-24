package uk.co.devproltd.booklog

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fs2.StreamApp
import io.circe.Json
import io.circe.generic.JsonCodec
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{HttpService, Uri}
import uk.co.devproltd.booklog.repository.{BookRepository, LogEntryRepository}

import scala.language.higherKinds

object BooklogServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], onShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = app[IO]
  def tx[F[_]: Effect]: Aux[F, Unit] =
    Transactor.fromDriverManager[F]("org.postgresql.Driver", "jdbc:postgresql:booklog", "booklog", "booklog")

  def app[F[_]: Effect]: fs2.Stream[F, StreamApp.ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(new BooklogService[F](new BookRepository, new LogEntryRepository, tx).bookService, "/")
      .mountService(new BooklogEntryService[F](new LogEntryRepository, tx).logEntryService, "/books")
      .serve

}

class BooklogService[F[_]: Effect](bookRepo: BookRepository, entryRepo: LogEntryRepository, transactor: Transactor[F])
    extends ServiceBase[F] {

  val bookService: HttpService[F] = HttpService[F] {
    case GET -> Root / "books" =>
      Ok(bookRepo.findAll.transact(transactor).compile.toList.map(_.asJson))
    case GET -> Root / "books" / IntVar(bookId) =>
      for {
        lookupRes <- bookRepo.find(bookId.toInt).transact(transactor)
        res       <- lookupRes.fold(NotFound(s"Book ID=$bookId was not found".asJsonError))(book => Ok(book.asJson))
      } yield res
    case req @ POST -> Root / "books" =>
      for {
        bookPost <- req.decodeJson[BookPost]
        book = bookPost.toBook
        createdBookId <- bookRepo.createBook(book).transact(transactor)
        res <- createdBookId.fold(
                err => Conflict(s"${err.message} - book title: ${book.title}, author: ${book.author}".asJsonError),
                bookId => Created(book.withId(bookId).asJson, Location(Uri.unsafeFromString(s"/books/$bookId")))
              )
      } yield res
    case DELETE -> Root / "books" / IntVar(bookId) =>
      val delete = for {
        _ <- entryRepo.deleteBookEntries(bookId)
        n <- bookRepo.delete(bookId)
      } yield n
      Ok(delete.transact(transactor).map(n => s"$n book deleted".asJsonSuccess))
  }
}

class BooklogEntryService[F[_]: Effect](entryRepo: LogEntryRepository, transactor: Transactor[F])
    extends ServiceBase[F] {

  val logEntryService: HttpService[F] = HttpService[F] {
    case GET -> Root / IntVar(bookId) / "entries" =>
      Ok(entryRepo.findByBookId(bookId).transact(transactor).compile.toList.map(_.asJson))
  }

}

abstract class ServiceBase[F[_]: Effect] extends Http4sDsl[F] {

  @JsonCodec case class GenericResponse(message: String, success: Boolean)

  implicit class StringOps(string: String) {
    def asJsonError: Json = GenericResponse(message = string, success = false).asJson

    def asJsonSuccess: Json = GenericResponse(message = string, success = true).asJson
  }

}
