package kz.ncanode.unit.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import kz.ncanode.configuration.JsonConfiguration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import spock.lang.Specification

class JsonConfigurationTest extends Specification {

    private final static int JACKSON_DEFAULT_LIMIT = 20_000_000

    ObjectMapper mapper(int maxStringLength) {
        def builder = new Jackson2ObjectMapperBuilder()
        new JsonConfiguration().jsonReadConstraintsCustomizer(maxStringLength).customize(builder)

        return builder.build()
    }

    String jsonWithString(int length) {
        def json = new StringBuilder(length + 16)
        json.append('{"xml":"')
        length.times { json.append('x' as char) }
        json.append('"}')

        return json.toString()
    }

    def "test configured limit is applied to the object mapper"() {
        expect:
        mapper(64 * 1024 * 1024).factory.streamReadConstraints().maxStringLength == 64 * 1024 * 1024
    }

    def "test a string longer than the default Jackson limit is read"() {
        given: 'строка длиннее дефолтного предела Jackson — такую даёт подпись LT/LTA'
        def payload = jsonWithString(JACKSON_DEFAULT_LIMIT + 100)

        when:
        def value = mapper(64 * 1024 * 1024).readTree(payload).get('xml').asText()

        then:
        noExceptionThrown()
        value.length() == JACKSON_DEFAULT_LIMIT + 100
    }

    def "check a string over the configured limit is rejected"() {
        given:
        def payload = jsonWithString(2000)

        when:
        mapper(1000).readTree(payload)

        then:
        def e = thrown(Exception)
        e.message.contains('maximum allowed')
    }
}
