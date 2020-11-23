package no.skatteetaten.aurora.openshift.reference.springboot.controllers

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.ResultActions

import no.skatteetaten.aurora.openshift.reference.springboot.service.CounterDatabaseService

class CounterControllerTest extends AbstractControllerTest {

  def counterDatabaseService = Mock(CounterDatabaseService)

  def "Example test for documenting the counter endpoint"() {

    given:
      counterDatabaseService.getAndIncrementCounter() >> BigDecimal.valueOf(1)

    when:
      ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get('/api/counter'))

    then:
      result
          .andExpect(status().isOk())
          .andDo(
          document('counter-get',
              preprocessResponse(prettyPrint()),
              responseFields(
                  fieldWithPath("value").type(JsonFieldType.NUMBER).
                      description("The current value of the counter"),
              )))
  }

  @Override
  protected List<Object> getControllersUnderTest() {

    return [new CounterController(counterDatabaseService)]
  }
}
