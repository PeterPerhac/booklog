package uk.co.devproltd.booklog

import java.time.LocalDateTime

import io.circe.generic.JsonCodec
import io.circe.java8.time._

@JsonCodec case class Book(
  id: Int,
  title: String,
  author: String,
  pages: Int,
  pagesRead: Int,
  active: Boolean,
  completed: Boolean,
  addedDatetime: LocalDateTime,
  deactivatedDatetime: Option[LocalDateTime])

@JsonCodec case class LogEntry(bookId: Int, page: Int, timestamp: LocalDateTime)
