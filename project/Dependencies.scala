object Dependencies {
  import sbt._

  object Version {
    val Http4s       = "1.0.0-M21"
    val Circe        = "0.14.0-M5"
    val Cats         = "2.3.0"
    val CatsEffect   = "3.2.9"
    val Doobie       = "1.0.0-RC1"
    val PostgresJdbc = "42.2.24"
  }

  def cats(artifact: String) =
    "org.typelevel" %% artifact % Version.Cats withSources() withJavadoc()

  def catsEffect(artifact: String) =
    "org.typelevel" %% artifact % Version.CatsEffect withSources() withJavadoc()

  def http4s(artifact: String) =
    "org.http4s" %% artifact % Version.Http4s withSources() withJavadoc()

  def circe(artifact: String) =
    "io.circe" %% artifact % Version.Circe withSources() withJavadoc()

  def doobie(artifact: String) =
    "org.tpolecat" %% artifact % Version.Doobie withSources() withJavadoc()

  def postgresJdbc(artifact: String) =
    "org.postgresql" % artifact % Version.PostgresJdbc
}
