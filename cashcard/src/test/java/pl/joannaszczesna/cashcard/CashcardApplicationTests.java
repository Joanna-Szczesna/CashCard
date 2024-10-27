package pl.joannaszczesna.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashcardApplicationTests {

    private static final int EXIST_ID = 99;
    private static final int UNKNOWN_ID = 1000;
    private static final String PATH_CASHCARDS = "/cashcards";
    private static final double EXIST_AMOUNT = 123.45;
    private static final String OWNER_FIRST = "sarah1";
    private static final String PASSWORD_FIRST = "abc123";

    @Autowired
    TestRestTemplate restTemplate;

    @Nested
    class GetRead {
        @Test
        void whenCashCardExist_returnACashCard() {
            String url = PATH_CASHCARDS + "/" + EXIST_ID;
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity(url, String.class);

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
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity(url, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isBlank();
        }

        @Test
        void whenListIsRequested_returnAllCashCard() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity("/cashcards", String.class);

            DocumentContext documentContext = JsonPath.parse(response.getBody());
            int cashCardCount = documentContext.read("$.length()");

            JSONArray ids = documentContext.read("$..id");
            assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

            JSONArray amounts = documentContext.read("$..amount");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(cashCardCount).isEqualTo(3);
            assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
        }

        @Test
        void shouldReturnAPageOfCashCards() {
            ResponseEntity<String> getListResponse = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity("/cashcards?page=0&size=1", String.class);
            DocumentContext documentContext = JsonPath.parse(getListResponse.getBody());
            JSONArray page = documentContext.read("$[*]");

            assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(page.size()).isEqualTo(1);
        }

        @Test
        void whenRequestPageAndSortField_returnASortedPageOfCashCards() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);

            DocumentContext documentContext = JsonPath.parse(response.getBody());
            JSONArray read = documentContext.read("$[*]");
            double amount = documentContext.read("$[0].amount");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(read.size()).isEqualTo(1);
            assertThat(amount).isEqualTo(150.00);
        }

        @Test
        void whenListIsRequestedWithNoParameters_returnASortedPageOfCashCardsUsingDefaultValues() {
            ResponseEntity<String> getListResponse = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity("/cashcards", String.class);

            DocumentContext documentContext = JsonPath.parse(getListResponse.getBody());
            JSONArray page = documentContext.read("$[*]");
            JSONArray amounts = documentContext.read("$..amount");

            assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(page.size()).isEqualTo(3);
            assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
        }
    }

    @Nested
    class PostCreate {
        @Test
        void whenCreateANewCashCard_ReturnStatusCreated() {
            CashCard newCashCard = new CashCard(null, 250.00, null);
            ResponseEntity<Void> createResponse = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .postForEntity(PATH_CASHCARDS, newCashCard, Void.class);
            URI locationOfNewCashCard = createResponse.getHeaders().getLocation();

            ResponseEntity<String> getResponse = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity(locationOfNewCashCard, String.class);

            DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
            Number id = documentContext.read("$.id");
            Double amount = documentContext.read("$.amount");

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(id).isNotNull();
            assertThat(amount).isEqualTo(250.00);
        }
    }

    @Nested
    class Security {
        @Test
        void whenUsingBadCredentials_shouldNotReturnACashCard() {
            String badUsername = "BAD-USER";
            String badPassword = "BAD-PASSWORD";
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(badUsername, PASSWORD_FIRST)
                    .getForEntity("/cashcards/99", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

            response = restTemplate
                    .withBasicAuth(OWNER_FIRST, badPassword)
                    .getForEntity("/cashcards/99", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void whenUserNotOwnAnyCard_rejectUser() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth("hank-owns-no-cards", "qrs456")
                    .getForEntity("/cashcards/99", String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void whenUserIsNotOwnerToCashCard_notAllowAccess() {
            ResponseEntity<String> response = restTemplate
                    .withBasicAuth(OWNER_FIRST, PASSWORD_FIRST)
                    .getForEntity("/cashcards/102", String.class); // john2 data
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }
}
