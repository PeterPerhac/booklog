CREATE TABLE book (
    id serial primary key,
    title text NOT NULL,
    author text NOT NULL,
    pages smallint NOT NULL DEFAULT 1,
    pages_read smallint NOT NULL DEFAULT 0,
    active boolean NOT NULL DEFAULT true,
    completed boolean NOT NULL DEFAULT false,
    added_datetime timestamp without time zone NOT NULL,
    deactivated_datetime timestamp without time zone,
    unique (title, author)
);

CREATE TABLE log_entry (
  book_id serial references book(id),
  page smallint NOT NULL DEFAULT 0,
  "timestamp" timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (book_id, page)
);

ALTER SEQUENCE public.book_id_seq RESTART WITH 1000;

