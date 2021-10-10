import Dependencies._

name         := "Cromulent"
version      := "1.0"
scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
    postgresJdbc("postgresql"),

    cats("cats-core"),
    catsEffect("cats-effect"),

    http4s("http4s-blaze-server"),
    http4s("http4s-circe"),
    http4s("http4s-dsl"),

    circe("circe-generic"),

    doobie("doobie-core"),
    doobie("doobie-postgres"),
)