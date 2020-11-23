package no.skatteetaten.aurora.openshift.reference.springboot.controllers

import static org.mockito.ArgumentMatchers.anyBoolean
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.BDDMockito.given
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.client.RestTemplate
import org.xml.sax.ErrorHandler

import com.fasterxml.jackson.databind.ObjectMapper

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import no.skatteetaten.aurora.AuroraMetrics
import no.skatteetaten.aurora.openshift.reference.springboot.controllers.dto.S3FileContentRequest
import no.skatteetaten.aurora.openshift.reference.springboot.service.S3Service

@WebMvcTest(controllers = [ExampleController, ErrorHandler])
@Import(value = [Config, AuroraMetrics])
@AutoConfigureWebClient(registerRestTemplate = true)
@AutoConfigureMockRestServiceServer
class ExampleControllerTest extends AbstractControllerTest {

  static class Config {
    @Bean
    MeterRegistry meterRegistry() {
      Metrics.globalRegistry
    }
  }

  @Autowired
  AuroraMetrics auroraMetrics

  @Autowired
  RestTemplate restTemplate

  @MockBean
  S3Service s3Service

  @Autowired
  MockRestServiceServer server

  def controller

  Boolean shouldSucceed

  def "Example test for documenting the ip endpoint"() {
    given:
      server.expect(MockRestRequestMatchers.requestTo("http://httpbin.org/ip"))
          .andRespond(MockRestResponseCreators.withSuccess(
              """{ "origin": "154.127.163.2" }""",
              MediaType.APPLICATION_JSON))

    when:
      ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get('/api/example/ip'))

    then:
      result
          .andExpect(status().isOk())
          .andDo(
              document('example-ip-get',
                  preprocessResponse(prettyPrint()),
                  responseFields(
                      fieldWithPath("ip").type(JsonFieldType.STRING).
                          description("The ip of this service as seen from the Internet"),
                  )))
  }

  def "Example test for documenting the sometimes endpoint"() {

    given:
      def url = '/api/example/sometimes'

    when:
      shouldSucceed = false
      ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get(url))

    then:
      result
          .andExpect(status().is5xxServerError())
          .andDo(
              document('example-sometimes-fail-get',
                  preprocessResponse(prettyPrint()),
                  responseFields(
                      fieldWithPath("errorMessage").type(JsonFieldType.STRING).
                          description("The error message describing the error that occurred"),
                  )))

    when:
      shouldSucceed = true
      result = mockMvc.perform(RestDocumentationRequestBuilders.get(url))

    then:
      result
          .andExpect(status().isOk())
          .andDo(
              document('example-sometimes-success-get',
                  preprocessResponse(prettyPrint()),
                  responseFields(
                      fieldWithPath("result").type(JsonFieldType.STRING).
                          description("The result of a successful operation"),
                  )))
  }

  def "Example test for documenting the s3 endpoint"() {
    given:
      given(s3Service.putFileContent(anyString(), anyString(), anyBoolean())).willAnswer {}
      given(s3Service.getFileContent(anyString(), anyBoolean())).willReturn("Content from file")
      def apiUrl = "/api/example/s3"

      def request = new S3FileContentRequest(
          "myFile.txt",
          "Content from file",
          true
      )

    when:
      ObjectMapper objectMapper = new ObjectMapper()
      ResultActions result = mockMvc.perform(
          post(apiUrl).content(objectMapper.writeValueAsBytes(request)).contentType(MediaType.APPLICATION_JSON_VALUE))

    then:
      result
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
          )
  }

  @Override
  protected List<Object> getControllersUnderTest() {

    controller = new ExampleController(restTemplate, auroraMetrics, s3Service) {
      @Override
      protected boolean performOperationThatMayFail() {
        return shouldSucceed
      }
    }
    return [controller]
  }
}
