package uk.co.devproltd.booklog.repository

import java.time.{LocalDateTime, ZoneOffset}

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.meta.Meta
import uk.co.devproltd.booklog.models.json.Book

trait Repository[F[_], T] {

  def findAll: F[List[T]]

}

object BookRepository extends Repository[IO, Book] {

  lazy val xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql:booklog", "postgres", "")

  //fluff
  implicit val localDateTimeMeta: Meta[LocalDateTime] =
    Meta[java.time.Instant].xmap(LocalDateTime.ofInstant(_, ZoneOffset.UTC), _.toInstant(ZoneOffset.UTC))

  def findAll: IO[List[Book]] = sql"select * from book".query[Book].to[List].transact(xa)

}
