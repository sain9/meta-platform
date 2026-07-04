package com.hussain.definition_api.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JooqConfig {

    private final DataSource dataSource;

    @Bean
    public DSLContext dslContext() {
        log.info("Initializing jOOQ DSLContext with PostgreSQL dialect");

        Settings settings = new Settings()
                .withStatementType(StatementType.PREPARED_STATEMENT)
                .withExecuteLogging(true)
                .withRenderSchema(true)
                .withRenderFormatted(true)
                .withRenderNameStyle(org.jooq.conf.RenderNameStyle.QUOTED)
                .withBackslashEscaping(org.jooq.conf.BackslashEscaping.OFF);

        var configuration = new DefaultConfiguration()
                .set(new TransactionAwareDataSourceProxy(dataSource))
                .set(SQLDialect.POSTGRES)
                .set(settings);

        return new DefaultDSLContext(configuration);
    }
}