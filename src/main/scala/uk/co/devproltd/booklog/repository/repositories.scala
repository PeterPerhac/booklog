package uk.co.devproltd.booklog.repository

import cats.effect.Effect
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.composite.Composite
import doobie.util.fragment.Fragment.{const => fr}
import doobie.util.meta.Meta
import uk.co.devproltd.booklog.{Book, LogEntry}

sealed abstract class SimpleRepository[F[_]: Effect, T: Composite, ID: Meta](
  protected val tableName: String,
  protected val idColumnName: String) {

  def find(id: ID): ConnectionIO[Option[T]] =
    (fr"select * from" ++ fr(tableName) ++ fr"where " ++ fr(idColumnName) ++ fr"=$id").query[T].option

  def findAll: fs2.Stream[ConnectionIO, T] =
    (fr"select * from" ++ fr(tableName)).query[T].stream

  def delete(id: ID): ConnectionIO[Int] =
    (fr"delete from" ++ fr(tableName) ++ fr"where " ++ fr(idColumnName) ++ fr"=$id").update.run

}

class BookRepository[F[_]: Effect] extends SimpleRepository[F, Book, Int]("book", "id")

class LogEntryRepository[F[_]: Effect] extends SimpleRepository[F, LogEntry, Int]("log_entry", "id") {

  def deleteBookEntries(bookId: Int): ConnectionIO[Int] =
    sql"delete from log_entry where book_id=$bookId".update.run

}
