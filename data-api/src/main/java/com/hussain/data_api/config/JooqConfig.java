package com.hussain.data_api.config;

import org.jooq.ExecuteListener;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    public DefaultConfiguration jooqConfiguration(DataSource dataSource) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.setDataSource(new TransactionAwareDataSourceProxy(dataSource));
        configuration.setSQLDialect(SQLDialect.POSTGRES);
        configuration.setSettings(settings());
        configuration.setExecuteListenerProvider(new DefaultExecuteListenerProvider(executeListener()));
        return configuration;
    }

    @Bean
    public Settings settings() {
        Settings settings = new Settings();
        settings.setRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
        settings.setRenderSchema(false);
        settings.setRenderFormatted(true);
        return settings;
    }

    @Bean
    public ExecuteListener executeListener() {
        return new JooqExecuteListener();
    }

    static class JooqExecuteListener implements ExecuteListener {
        @Override
        public void executeStart(org.jooq.ExecuteContext ctx) {
            if (ctx.query() != null) {
                org.slf4j.LoggerFactory.getLogger(JooqExecuteListener.class)
                        .debug("Executing SQL: {}", ctx.query().getSQL());
            }
        }
    }
}