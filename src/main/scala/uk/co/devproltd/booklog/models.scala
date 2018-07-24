package uk.co.devproltd.booklog

import java.time.LocalDateTime

import io.circe.generic.JsonCodec
import io.circe.java8.time._

@JsonCodec case class Book(
  id: Option[Book.Id],
  title: String,
  author: String,
  pages: Int,
  pagesRead: Int,
  active: Boolean,
  completed: Boolean,
  addedDatetime: LocalDateTime,
  deactivatedDatetime: Option[LocalDateTime])

object Book {
  type Id = Int
}

@JsonCodec case class LogEntry(bookId: Book.Id, page: Int, timestamp: LocalDateTime)
