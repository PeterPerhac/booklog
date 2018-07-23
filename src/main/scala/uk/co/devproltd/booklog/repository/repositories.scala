package uk.co.devproltd.booklog.repository

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.composite.Composite
import doobie.util.fragment.Fragment.{const => fr}
import doobie.util.meta.Meta
import uk.co.devproltd.booklog.{Book, LogEntry}

sealed abstract class SimpleRepository[T: Composite, ID: Meta](
  protected val tableName: String,
  protected val idColumnName: String) {

  def findAll: fs2.Stream[ConnectionIO, T] =
    (fr"select * from" ++ fr(tableName)).query[T].stream

  def find(id: ID): ConnectionIO[Option[T]] =
    (fr"select * from" ++ fr(tableName) ++ fr"where " ++ fr(idColumnName) ++ fr"=$id").query[T].option

  def delete(id: ID): ConnectionIO[Int] =
    (fr"delete from" ++ fr(tableName) ++ fr"where " ++ fr(idColumnName) ++ fr"=$id").update.run
}

class BookRepository extends SimpleRepository[Book, Int]("book", "id")

class LogEntryRepository extends SimpleRepository[LogEntry, Int]("log_entry", "id") {

  def findByBookId(bookId: Book.Id): fs2.Stream[ConnectionIO, LogEntry] =
    sql"select * from log_entry WHERE book_id=$bookId".query[LogEntry].stream

  def deleteBookEntries(bookId: Book.Id): ConnectionIO[Int] =
    sql"delete from log_entry where book_id=$bookId".update.run

}
