package cromulent.service

import java.util.UUID
import cromulent.domain._


trait ServiceAlgebra[F[_]] {
  // These two form a window of expected data.
  // How to deal with expected data not present?
  def register(client : Client) : F[Instrumentation]
  def unregister(client: UUID): F[Unit]

  def ingest(trace : CpuTrace) : F[Unit]

  // How does this signal abscent cpu trace data (as per the window?)
  def query(filter : Filter.Query): F[List[CpuTrace]]
}