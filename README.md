# CashCard
SpringBoot, TDD, Security



## REST API Contract

| Method | Operation       | Endpoint        | Use Cases                                                                         |
|--------|-----------------|-----------------|-----------------------------------------------------------------------------------|
| POST   | Create          | /cashcards      | create a new resource                                                             |  
| PUT    | Update          | /cashcards/{id} | Replaces the resource: the entire record is replaced by the object in the Request |
| GET    | Read    | /cashcards/{id} | Get resource                                                                      | 
| DELETE | Delete| /cashcards/{id} | Hard delete resource                                                              | 



### POST

Possible responses HTTP Status

| Response status code | Use Cases                     | Marks                                  |
|----------------------|-------------------------------|----------------------------------------|
| 201 CREATED          | resource successfully created | returned URI to newly created resource |
| 409 CONFLICT         | resource already exist        |                                        |



### GET
Possible responses HTTP Status

| Response status code | Use Cases                                                                    |
|----------------------|------------------------------------------------------------------------------|
| 200 OK               | The user is authorized and the Cash Card was successfully retrieved          |
| 401 UNAUTHORIZED     | the user is unauthenticated or unauthorized                                  | 
| 404 NOT FOUND        | the user is authenticated and authorized but the Cash Card cannot be found   | 


Example Response Body for successfully operation:
body type: JSON

```json
{
  "id": 97,
  "amount": 100.25
}
```

full Response:

Status Code: 200
Body:
```json
{
  "id": 97,
  "amount": 100.25
}
```

### PUT
Possible responses HTTP Status

| Response status code | Use Cases                                                                  |
|----------------------|----------------------------------------------------------------------------|
| 204 NO CONTENT       | resource successfully updated                                              |
| 404 NOT FOUND        | the user is authenticated and authorized but the Cash Card cannot be found | 


