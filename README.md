# CashCard
SpringBoot, TDD, Security



## REST API Contract

| Method | Operation       | Endpoint        | Use Cases                                                                         |
|--------|-----------------|-----------------|-----------------------------------------------------------------------------------|
| POST   | Create          | /cashcards      | create a new resource                                                             |  
| PUT    | Update          | /cashcards/{id} | Replaces the resource: the entire record is replaced by the object in the Request |
| GET    | Read    | /cashcards/{id} | Get single resource                                                               | 
| GET    | Read    | /cashcards | Get resources                                                                     | 
| DELETE | Delete| /cashcards/{id} | Hard delete resource                                                              | 



### POST

Possible responses HTTP Status

| Response status code | Use Cases                     | Marks                                  |
|----------------------|-------------------------------|----------------------------------------|
| 201 CREATED          | resource successfully created | returned URI to newly created resource |
| 409 CONFLICT         | resource already exist        |                                        |

Example Response:

Status Code: 201 CREATED
Header: Location=/cashcards/42



### GET
Possible responses HTTP Status

| Response status code | Use Cases                                                                  |
|----------------------|----------------------------------------------------------------------------|
| 200 OK               | The user is authorized and the CashCard(s) was successfully retrieved      |
| 401 UNAUTHORIZED     | the user is unauthenticated or unauthorized                                | 
| 404 NOT FOUND        | the user is authenticated and authorized but the Cash Card cannot be found | 


Example Response Body for successfully operation:
Single Resource:
Status Code: 200 </br>
body type: JSON

```json
{
  "id": 97,
  "amount": 100.25
}
```

#### Pagination and Sorting

1. Get the second page </br>
/cashcards?page=1

2. where a page has length of 3 sorted by the current Cash Card balance </br>
/cashcards?page=1&size=3&sort=amount

 defaults provided by Spring: page=0, size=20

```json
{
  "content": [
    {
      "id": 1,
      "amount": 10.0
    },
    {
      "id": 2,
      "amount": 0.19
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 3,
    "pageNumber": 1,
    "pageSize": 3,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalElements": 5,
  "totalPages": 2,
  "first": false,
  "size": 3,
  "number": 1,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 2,
  "empty": false
}
```


### PUT
Possible responses HTTP Status

| Response status code | Use Cases                                                                  |
|----------------------|----------------------------------------------------------------------------|
| 204 NO CONTENT       | resource successfully updated                                              |
| 404 NOT FOUND        | the user is authenticated and authorized but the Cash Card cannot be found | 


