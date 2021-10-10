package cromulent.datastore

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

import cats._
import cats.implicits._
import cats.effect._

import java.util.UUID
import java.time._

import cromulent.domain._


case class ExternalRepresentation
  ( at          : Instant,
    aggregateId : String,
    kind        : String,
    payload     : String )

trait External[A] {
  def externalRepresentation(x: A) : ExternalRepresentation
  def fromExternalRepresentation(e: ExternalRepresentation): Option[A]
}

object External {
  def apply[E : External] = implicitly[External[E]]
}

trait EventstoreAlgebra[F[_]] {
  // Would it be relevant to expose some Doobie-bits here 
  // to make it possible to join transactions?
  def append[E : External](e : E): F[Unit]

  def stream[E : External](filter : Filter.Query): F[List[E]]
}


object Eventstore {
  def apply[F[_]: Sync](xa: Transactor[F]) = new EventstoreAlgebra[F] {

    // I might just aswell return the ConnectionIO thing outside.
    def append[E: External](e: E): F[Unit] = {
      val rep = External[E].externalRepresentation(e)
      sql"".update.run.map(_ => ()).transact(xa)
    }

    def stream[E: External](filter: Filter.Query): F[List[E]] = {
      // This fails if External does not recognize `kind` as an 
      // Event. Use raiseError here. Don't use exception.
      def parse(e: ExternalRepresentation): F[E] =
        External[E].fromExternalRepresentation(e) match {
          case Some(x)   => ???
          case otherwise => Sync[F].raiseError(???)
        }

        for {
          xs <- sql"".query[ExternalRepresentation].to[List].transact(xa)
          ys <- xs.traverse(parse)
        } yield ys
    }
  }
}