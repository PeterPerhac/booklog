package uk.co.devproltd.booklog

import java.time.LocalDateTime

import io.circe.generic.JsonCodec
import io.circe.java8.time._

case class Book(
  title: String,
  author: String,
  pages: Int,
  pagesRead: Int,
  active: Boolean,
  completed: Boolean,
  addedDatetime: LocalDateTime,
  deactivatedDatetime: Option[LocalDateTime]) {
  def withId(id: Book.Id): BookWithId =
    BookWithId(
      id = id,
      title = this.title,
      author = this.author,
      pages = this.pages,
      pagesRead = this.pagesRead,
      active = this.active,
      completed = this.completed,
      addedDatetime = this.addedDatetime,
      deactivatedDatetime = this.deactivatedDatetime
    )
}

object Book {
  type Id = Int
}
@JsonCodec case class BookPost(
  title: String,
  author: String,
  pages: Int
) {

  def toBook: Book =
    Book(
      title = this.title,
      author = this.author,
      pages = this.pages,
      pagesRead = 0,
      active = true,
      completed = false,
      addedDatetime = LocalDateTime.now,
      deactivatedDatetime = None
    )
}

@JsonCodec case class BookWithId(
  id: Book.Id,
  title: String,
  author: String,
  pages: Int,
  pagesRead: Int,
  active: Boolean,
  completed: Boolean,
  addedDatetime: LocalDateTime,
  deactivatedDatetime: Option[LocalDateTime])

@JsonCodec case class LogEntry(bookId: Book.Id, page: Int, timestamp: LocalDateTime)
