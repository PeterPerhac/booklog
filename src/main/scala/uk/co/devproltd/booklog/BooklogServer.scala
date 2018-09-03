package uk.co.devproltd.booklog

import cats.effect.{Effect, IO}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fs2.StreamApp
import fs2.StreamApp.ExitCode
import io.circe.generic.JsonCodec
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{HttpService, Response, Uri}
import uk.co.devproltd.booklog.repository.{BookRepository, LogEntryRepository}

import scala.language.higherKinds

object BooklogServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], onShutdown: IO[Unit]): fs2.Stream[IO, ExitCode] = app[IO]

  def app[F[_]: Effect]: fs2.Stream[F, ExitCode] =
    ((tx: Aux[F, Unit]) =>
      BlazeBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .mountService(new BooklogService(new BookRepository, new LogEntryRepository, tx).service, "/")
        .mountService(new BooklogEntryService(new LogEntryRepository, tx).service, "/books")
        .serve)(Transactor.fromDriverManager("org.postgresql.Driver", "jdbc:postgresql:booklog", "booklog", "booklog"))

}

class BooklogService[F[_]: Effect](bookRepo: BookRepository, entryRepo: LogEntryRepository, transactor: Transactor[F])
    extends ServiceBase[F] {

  private def deleteBook(bookId: Book.Id): ConnectionIO[Int] =
    entryRepo.deleteBookEntries(bookId) >> bookRepo.delete(bookId)

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "books" =>
      Ok(bookRepo.findAll.transact(transactor).compile.toList.map(_.asJson))
    case GET -> Root / "books" / IntVar(bookId) =>
      val notFound = NotFound(s"Book ID=$bookId was not found".asJsonError)
      bookRepo.find(bookId).transact(transactor) >>= (_.fold(notFound)(okJson))
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
      Ok(deleteBook(bookId).transact(transactor).map(n => s"$n book deleted".asJsonSuccess))
  }
}

class BooklogEntryService[F[_]: Effect](entryRepo: LogEntryRepository, transactor: Transactor[F])
    extends ServiceBase[F] {

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / IntVar(bookId) / "entries" =>
      Ok(entryRepo.findByBookId(bookId).transact(transactor).compile.toList.map(_.asJson))
  }

}

abstract class ServiceBase[F[_]: Effect] extends Http4sDsl[F] {

  protected def okJson[T: Encoder](t: T): F[Response[F]] = Ok(t.asJson)

  @JsonCodec case class GenericResponse(message: String, success: Boolean)

  implicit class StringOps(string: String) {
    def asJsonError: Json = GenericResponse(message = string, success = false).asJson

    def asJsonSuccess: Json = GenericResponse(message = string, success = true).asJson
  }

}
