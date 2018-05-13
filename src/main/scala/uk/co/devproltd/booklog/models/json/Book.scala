package uk.co.devproltd.booklog.models.json

import java.time.LocalDateTime
import io.circe.java8.time._
import io.circe.generic.JsonCodec

@JsonCodec case class Book(
  id: Int,
  title: String,
  author: String,
  pages: Int,
  pagesRead: Int,
  active: Boolean,
  completed: Boolean,
  addedDatetime: LocalDateTime,
  deactivatedDatetime: Option[LocalDateTime]
)
