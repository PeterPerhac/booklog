# Booklog Example cURL commands

## GET PLURAL
curl http://localhost:8080/books | jq .
## GET SINGULAR
curl http://localhost:8080/books/6 | jq .
curl http://localhost:8080/books/109 | jq .
## DELETE
curl -XDELETE http://localhost:8080/books/109 | jq .
## GET ENTRIES PLURAL
curl http://localhost:8080/books/110/entries | jq .

## CREATE A BOOK
### !!! - must not have verbose output enabled when piping out to jq tool

- verbose
curl -v -H "Content-Type: application/json" -H "Accept: application/json" --data '{"title": "Book Title4", "author": "Book Author2", "pages": 100 }' http://localhost:8080/books

- non-verbose / silent
curl -XPOST -H "Content-Type: application/json" -H "Accept: application/json" --data '{"title": "FooBar", "author": "Book Author3", "pages": 100 }' http://localhost:8080/books | jq .

