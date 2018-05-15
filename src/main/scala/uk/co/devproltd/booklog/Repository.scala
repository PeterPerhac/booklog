package uk.co.devproltd.booklog

import cats.effect.Effect
import doobie.implicits._
import doobie.util.composite.Composite
import doobie.util.fragment.Fragment.{const => fr}
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

abstract class Repository[F[_] : Effect, T: Composite, ID: Meta](protected val tableName: String) {

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
