package uk.co.devproltd.booklog

import java.time.ZoneOffset.UTC
import java.time.{Instant, LocalDateTime}

import doobie.util.meta.Meta

package object repository {

  implicit val ldtMeta: Meta[LocalDateTime] = Meta[Instant].xmap(LocalDateTime.ofInstant(_, UTC), _.toInstant(UTC))

}
