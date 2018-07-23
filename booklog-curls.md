# Booklog Example cURL commands

## GET PLURAL
curl http://localhost:8080/books | jq .
## GET SINGULAR
curl http://localhost:8080/books/110 | jq .
curl http://localhost:8080/books/109 | jq .
## DELETE
curl -XDELETE http://localhost:8080/books/109 | jq .
## GET ENTRIES PLURAL
curl http://localhost:8080/books/110/entries | jq .

