package no.skatteetaten.aurora.openshift.reference.springboot.service

import java.util.function.Consumer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest

import io.findify.s3mock.S3Mock
import no.skatteetaten.aurora.openshift.reference.springboot.ApplicationConfig
import no.skatteetaten.aurora.openshift.reference.springboot.service.dto.S3Configuration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import spock.lang.Specification

@RestClientTest([ApplicationConfig, S3Service])
class S3ServiceTest extends Specification {
  @Autowired
  S3Configuration s3Config

  @Autowired
  S3Service s3Service

  def s3Mock = new S3Mock.Builder().withInMemoryBackend().withPort(9000).build()

  def "Verify is able to store and retrieve object from minio with default bucket"() {
    given:
      s3Mock.start()
      s3Config.s3Client.createBucket(
          CreateBucketRequest.builder().bucket("default").build() as CreateBucketRequest)
      def expectedFileContent = "my awesome test file"

    when:
      s3Service.putFileContent("myFile.txt", expectedFileContent, true)
      def fileContent = s3Service.getFileContent("myFile.txt", true)

    then:
      noExceptionThrown()
      fileContent == expectedFileContent
    cleanup:
      s3Mock.shutdown()
  }

  def "Verify is able to store and retrieve object from minio with other bucket"() {
    given:
      s3Mock.start()
      s3Config.s3Client.createBucket(CreateBucketRequest.builder().bucket("default").build() as CreateBucketRequest)
      def expectedFileContent = "my awesome test file2"

    when:
      s3Service.putFileContent("myFile.txt", expectedFileContent, false)
      def fileContent = s3Service.getFileContent("myFile.txt", false)

    then:
      noExceptionThrown()
      fileContent == expectedFileContent

    cleanup:
      s3Mock.shutdown()
  }

}
