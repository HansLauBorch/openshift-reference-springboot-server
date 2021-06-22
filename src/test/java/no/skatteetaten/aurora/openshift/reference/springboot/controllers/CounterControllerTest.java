package no.skatteetaten.aurora.openshift.reference.springboot.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import no.skatteetaten.aurora.openshift.reference.springboot.service.CounterDatabaseService;

@WebMvcTest(controllers = { CounterController.class })
class CounterControllerTest extends AbstractControllerTest {

    @MockBean
    private CounterDatabaseService databaseService;

    @Test
    @DisplayName("Example test for documenting the counter endpoint")
    void counterEndpoint() throws Exception {
        given(databaseService.getAndIncrementCounter()).willReturn(1L);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/counter"))
            .andExpect(status().isOk())
            .andDo(
                document("counter-get",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("value").type(JsonFieldType.NUMBER).
                            description("The current value of the counter")
                    )));
    }
}