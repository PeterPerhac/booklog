package uk.co.devproltd.booklog.repository

import cats.effect.Effect
import doobie.implicits._
import doobie.util.composite.Composite
import doobie.util.fragment.Fragment.{const => fr}
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import uk.co.devproltd.booklog.{Book, LogEntry}

sealed abstract class Repository[F[_]: Effect, T: Composite, ID: Meta](protected val tableName: String) {

  protected val transactor: Aux[F, Unit] =
    Transactor.fromDriverManager[F]("org.postgresql.Driver", "jdbc:postgresql:booklog", "booklog", "booklog")

  def find(id: ID): F[Option[T]] =
    (fr"select * from" ++ fr(tableName) ++ fr"where id = $id").query[T].option.transact(transactor)

  def findAll(): F[Seq[T]] =
    (fr"select * from" ++ fr(tableName)).query[T].to[Seq].transact(transactor)

  def save(t: T): F[Unit] = ??? //TODO implement

  def delete(id: ID): F[Int] =
    (fr"delete from" ++ fr(tableName) ++ fr"where id = $id").update.run.transact(transactor)

}

class BookRepository[F[_]: Effect] extends Repository[F, Book, Int]("book")

class LogEntryRepository[F[_]: Effect] extends Repository[F, LogEntry, Int]("log_entry") {

  def deleteBookEntries(bookId: Int): F[Int] =
    sql"delete from log_entry where book_id=$bookId".update.run.transact(transactor)

}
