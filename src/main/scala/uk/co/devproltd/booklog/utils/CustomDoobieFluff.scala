package uk.co.devproltd.booklog.utils

import java.time.{Instant, LocalDateTime}
import java.time.ZoneOffset.UTC

import doobie.util.meta.Meta

trait CustomDoobieFluff {

  implicit val ldtMeta: Meta[LocalDateTime] = Meta[Instant].xmap(LocalDateTime.ofInstant(_, UTC), _.toInstant(UTC))

}

object CustomDoobieFluff extends CustomDoobieFluff
