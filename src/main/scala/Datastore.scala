package cromulent.datastore

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._

import cats._
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

trait EventstoreAlgebra[F[_]] {
  // Would it be relevant to expose some Doobie-bits here 
  // to make it possible to join transactions?
  def append[E : External](e : E): F[Unit]
  def stream[E : External](filter : Filter.Query): F[List[E]]
}


object Eventstore {
  def apply[F[_] : Monad] = new EventstoreAlgebra[F] {

  }
}