package cromulent.server

import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import org.typelevel.ci.CIString

import java.time.Year
import java.util.UUID
import scala.collection.mutable
import scala.util.Try


object Server extends IOApp {
  type Actor = String
  case class Movie(id: String, 
                   title: String, 
                   year: Int, 
                   actors: List[String],
                   director: String)
  case class Director(firstName: String, lastName: String) {
    override def toString = s"$firstName $lastName"
  }

  case class DirectorDetails(firstName: String, 
                             lastName: String,
                             genre: String)

  val snjl: Movie = Movie(
    "6bcbca1e-efd3-411d-9f7c-14b872444fce",
    "Zack Snyder's Justice League",
    2021,
    List("Henry Cavill", "Gal Godot", "Ezra Miller", "Ben Affleck", "Ray Fisher", "Jason Momoa"),
    "Zack Snyder"
  )

  val movies: Map[String, Movie] = Map(snjl.id -> snjl)

  private def findMovieById(movieId: UUID) =
    movies.get(movieId.toString)

  private def findMoviesByDirector(director: String): List[Movie] =
    movies.values.filter(_.director == director).toList

  val directorDb: mutable.Map[Director, DirectorDetails] = 
    mutable.Map(Director("Zack", "Snyder") -> DirectorDetails("Zack", "Snyder", "Super heroes"))

  class Routing[F[_]: Monad] extends Http4sDsl[F] {
    object DirectorParam 
      extends QueryParamDecoderMatcher[String]("director")

    object YearParam
      extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

    implicit val decodeYear: QueryParamDecoder[Year] =
      QueryParamDecoder[Int].emap { year =>
        Try(Year.of(year)).toEither.leftMap { t =>
          ParseFailure(t.getMessage(), t.getMessage())
        }
      }

    object DirectorPath {
      def unapply(text: String) : Option[Director] =
        Try {
          val tokens = text split " "
          Director(tokens(0), tokens(1))
        }.toOption
    }

    def search(director: String, year: Year) =
      Ok(findMoviesByDirector(director).filter(_.year == year.getValue).asJson)

    def movieRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "movies" :? DirectorParam(director) +& YearParam(yearParam) => 
        yearParam match {
          case Some(validated) =>
            validated.fold(_ => BadRequest("Bad year"), 
                          year => search(director, year))
          case None => Ok(findMoviesByDirector(director).asJson)
        }

      case GET -> Root / "movies" / UUIDVar(movieId) / "actors" => 
        findMovieById(movieId).map(_.actors) match {
          case Some(xs) => Ok(xs.asJson)
          case _        => NotFound(s"No such movie `$movieId`.")
        }
    }

    def directorRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "directors" / DirectorPath(director) => 
        directorDb.get(director) match {
          case Some(details) => 
            for { t <- "Hello, world".pure[F] 
                  r <- Ok(t)
                }
            yield r
            //Ok(details.asJson)
          case _             => NotFound(s"No director `$director` found.")
        }
    }

    def allRoutes: HttpRoutes[F] =
        movieRoutes <+> directorRoutes

    def configuration: HttpApp[F] =
      allRoutes.orNotFound
  }

  import cats.data._

  type App[A] = ReaderT[IO, String, A]

  def env: App[String] = ReaderT.ask[IO, String] >>= (e => "".pure[App])

  def printEnv: App[Unit] =
    for { env <- env }
    yield println(env)

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(new Routing[IO].configuration)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}