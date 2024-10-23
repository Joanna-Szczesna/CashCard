package pl.joannaszczesna.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashcardApplicationTests {

    public static final int EXIST_ID = 99;
    public static final int UNKNOWN_ID = 1000;
    public static final String PATH_CASHCARDS = "/cashcards";
    public static final double EXIST_AMOUNT = 123.45;

    @Autowired
    TestRestTemplate restTemplate;

    @Nested
    class GetRead {
        @Test
        void whenCashCardExist_returnACashCard() {
            String url = PATH_CASHCARDS + "/" + EXIST_ID;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            DocumentContext documentContext = JsonPath.parse(response.getBody());
            Number id = documentContext.read("$.id");
            Double amount = documentContext.read("$.amount");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(id).isEqualTo(EXIST_ID);
            assertThat(amount).isEqualTo(EXIST_AMOUNT);
        }

        @Test
        void whenCashCardIdUnknown_notReturnAnyCashCard() {
            String url = PATH_CASHCARDS + "/" + UNKNOWN_ID;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isBlank();
        }
    }

    @Nested
    class PostCreate {

        @Test
        @DirtiesContext
        void whenCreateANewCashCard_ReturnStatusCreated() {
            CashCard newCashCard = new CashCard(null, 250.00);
            ResponseEntity<Void> createResponse = restTemplate.postForEntity(PATH_CASHCARDS, newCashCard, Void.class);
            URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
            ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);

            DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
            Number id = documentContext.read("$.id");
            Double amount = documentContext.read("$.amount");

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(id).isNotNull();
            assertThat(amount).isEqualTo(250.00);
        }
    }
}
