package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import no.skatteetaten.aurora.AuroraMetrics;
import no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto.S3FileContentRequest;
import no.skatteetaten.aurora.openshift.reference.springboot.controllers.errorhandling.ErrorHandler;
import no.skatteetaten.aurora.openshift.reference.springboot.service.ExampleService;
import no.skatteetaten.aurora.openshift.reference.springboot.service.S3Service;

@WebMvcTest(controllers = { ExampleController.class, ErrorHandler.class })
@Import(value = { ExampleControllerTest.Config.class, AuroraMetrics.class })
@AutoConfigureWebClient(registerRestTemplate = true)
@AutoConfigureMockRestServiceServer
class ExampleControllerTest extends AbstractControllerTest {
    static class Config {
        @Bean
        MeterRegistry meterRegistry() {
            return Metrics.globalRegistry;
        }
    }

    @MockBean
    private S3Service s3Service;

    @MockBean
    private ExampleService exampleService;

    @Autowired
    private MockRestServiceServer server;

    @Test
    @DisplayName("Exammple test for documenting the ip endpoint")
    public void ipEndpoint() throws Exception {
        server.expect(MockRestRequestMatchers.requestTo("http://httpbin.org/ip"))
            .andRespond(
                MockRestResponseCreators.withSuccess("{ \"origin\": \"154.127.163.2\" }", MediaType.APPLICATION_JSON
                ));

        mockMvc
            .perform(RestDocumentationRequestBuilders.get("/api/example/ip"))
            .andExpect(status().isOk())
            .andDo(
                document("example-ip-get",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("ip")
                            .type(JsonFieldType.STRING).description("The ip of this service as seen from the Internet")
                    )));
    }

    @Test
    @DisplayName("Example test for documenting the sometimes failing endpoint")
    public void sometimesFailingEndpoint() throws Exception {
        given(exampleService.performOperationThatMayFail()).willReturn(false);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/example/sometimes"))
            .andExpect(status().is5xxServerError())
            .andDo(
                document("example-sometimes-fail-get",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("errorMessage").type(JsonFieldType.STRING).
                            description("The error message describing the error that occurred")
                    )));
    }

    @Test
    @DisplayName("Example test for documenting the somtimes success endpoint")
    public void sometimesSuccessEndpoint() throws Exception {
        given(exampleService.performOperationThatMayFail()).willReturn(true);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/example/sometimes"))
            .andExpect(status().isOk())
            .andDo(
                document("example-sometimes-success-get",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("result").type(JsonFieldType.STRING).
                            description("The result of a successful operation")
                        )));
    }

    @Test
    @DisplayName("Example test for documenting the s3 endpoint")
    void s3Endpoint() throws Exception {
        given(s3Service.getFileContent(anyString(), anyBoolean())).willReturn("Content from file");

        S3FileContentRequest request = new S3FileContentRequest(
            "myFile.txt",
            "Content from file",
            true
        );

        mockMvc.perform(
            post("/api/example/s3").content(new ObjectMapper().writeValueAsBytes(request)).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(
                document(
                    "example-s3-storefile",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("content")
                            .type(JsonFieldType.STRING)
                            .description("The content of the file that was stored")
                    )
                )
            );
    }
}