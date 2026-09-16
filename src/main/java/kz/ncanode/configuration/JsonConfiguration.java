package kz.ncanode.configuration;

import com.fasterxml.jackson.core.StreamReadConstraints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ограничения разбора JSON. Jackson начиная с 2.15 отказывается читать строки длиннее
 * 20 000 000 символов, а подпись уровня LT/LTA с вшитым боевым CRL в этот предел не помещается:
 * NCANode не мог принять на проверку подпись, которую сам же и создал.
 */
@Configuration
public class JsonConfiguration {

    /**
     * Поднимает предел длины строки для всех ObjectMapper'ов Spring.
     * Значение приходит параметром, а не полем с @ConfigurationProperties: настройщик Jackson
     * создаётся раньше, чем отрабатывает привязка свойств, и в лимит попадал бы 0
     *
     * @param maxStringLength Предельная длина строки JSON в символах
     * @return Настройщик Jackson
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonReadConstraintsCustomizer(
        @Value("${ncanode.json.maxStringLength:67108864}") int maxStringLength) {

        StreamReadConstraints constraints = StreamReadConstraints.builder()
            .maxStringLength(maxStringLength)
            .build();

        return builder -> builder.postConfigurer(mapper -> mapper.getFactory().setStreamReadConstraints(constraints));
    }
}
