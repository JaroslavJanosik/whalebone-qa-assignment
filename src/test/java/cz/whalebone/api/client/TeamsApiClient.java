package cz.whalebone.api.client;

import cz.whalebone.api.model.TeamsResponse;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class TeamsApiClient {

    private final RequestSpecification spec;

    public TeamsApiClient(String baseUrl) {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setBasePath("/api")
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    /**
     * GET /api/teams — asserts HTTP 200 and deserializes body.
     */
    @Step("GET /api/teams")
    public TeamsResponse getTeams() {
        return given(spec)
                .when()
                .get("/teams")
                .then()
                .statusCode(200)
                .extract()
                .as(TeamsResponse.class);
    }

    /**
     * GET /api/teams — returns raw Response for status-code / negative tests.
     */
    @Step("GET /api/teams (raw)")
    public Response getTeamsRaw() {
        return given(spec)
                .when()
                .get("/teams");
    }
}
