package kz.ncanode.unit.controller

import kz.ncanode.configuration.SystemConfiguration
import kz.ncanode.constants.MessageConstants
import kz.ncanode.controller.advice.ExceptionHandlerControllerAdvice
import kz.ncanode.exception.ClientException
import kz.ncanode.exception.ServerException
import org.springframework.http.converter.HttpMessageNotReadableException
import spock.lang.Specification

class ExceptionHandlerControllerAdviceTest extends Specification {

    SystemConfiguration systemConfiguration = new SystemConfiguration()

    ExceptionHandlerControllerAdvice advice() {
        new ExceptionHandlerControllerAdvice(systemConfiguration)
    }

    HttpMessageNotReadableException tooLargeBody() {
        new HttpMessageNotReadableException("JSON parse error",
            new IllegalStateException("String value length exceeds the maximum allowed"))
    }

    def "test unreadable request body is reported as a client error"() {
        when:
        def response = advice().handleHttpMessageNotReadableException(tooLargeBody(), null)

        then:
        response.statusCode.value() == 400
        response.body.status == 400
        response.body.message == MessageConstants.REQUEST_BODY_INVALID
        response.body.details == null
    }

    def "test parser details are exposed only with detailedErrors"() {
        given:
        systemConfiguration.setDetailedErrors(true)

        when:
        def response = advice().handleHttpMessageNotReadableException(tooLargeBody(), null)

        then:
        response.body.details == "String value length exceeds the maximum allowed"
    }

    def "test client exception keeps its own status"() {
        when:
        def response = advice().handleRuntimeException(new ClientException("bad input"), null)

        then:
        response.statusCode.value() == 400
        response.body.message == "bad input"
    }

    def "test server exception is reported as a server error"() {
        when:
        def response = advice().handleRuntimeException(new ServerException("boom"), null)

        then:
        response.statusCode.value() == 500
    }
}
