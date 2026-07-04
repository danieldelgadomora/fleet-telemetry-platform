package com.simon.fleet.ingestion.infrastructure.persistence.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Quita el campo {@code _class} que Spring Data MongoDB agrega por defecto a cada documento
 * guardado. Ese campo solo es necesario para deserialización polimórfica (varias subclases
 * posibles en una misma colección); {@code TelemetryDocument} es una única clase concreta, así
 * que guardarlo es ruido innecesario en cada lectura GPS.
 */
@Configuration
public class MongoConverterConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory,
                                                         MongoMappingContext context,
                                                         MongoCustomConversions conversions) {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), context);
        converter.setCustomConversions(conversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
