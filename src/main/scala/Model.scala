package cromulent.domain

import java.util.UUID
import java.time._

/* A client records a CPU trace.
 * A client posts CPU traces to the service at a cromulent interval.
 * 
 */
case class Version
  ( major : Int,
    minor : Int,
    fix   : Int,
    build : Int )

case class OperatingSystem
  ( family  : String,
    name    : String,
    version : Version )

case class Client
  ( id      : UUID, 
    name    : String, 
    version : Version, 
    os      : OperatingSystem )

case class Datum
  ( min  : Float, 
    max  : Float, 
    mean : Float )

case class CpuTrace
  ( clientId   : UUID,
    start      : Instant,
    resolution : Duration,
    data       : Array[Datum] )

case class Instrumentation
  ( samplingResolution : Duration,
    callbackEvery      : Duration ) // URI too?


// These don't even have to have a common type
sealed trait Event
case class ClientRegistered(at: Instant, client: Client)  extends Event
case class ClientUnregistered(at: Instant, client: UUID)  extends Event
case class CpuTraceIngested(at: Instant, trace: CpuTrace) extends Event


sealed trait Term[+A]
case class This[A](x: A) extends Term[A]
case object Star         extends Term[Nothing]

object Filter {
  // Pagination?
  case class Query
    ( from    : Term[Instant],
      through : Term[Instant],
      client  : Term[Client],
      os      : Term[OperatingSystem] )

  case class Client
    ( id      : Term[UUID],
      version : Term[Version] )

  case class OperatingSystem
    ( family  : Term[String],
      name    : Term[String],
      version : Term[Version] )

  case class Version
    ( major : Term[Int],
      minor : Term[Int],
      fix   : Term[Int],
      build : Term[Int] )
}