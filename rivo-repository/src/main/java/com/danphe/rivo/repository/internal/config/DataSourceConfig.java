package com.danphe.rivo.repository.internal.config;

import com.danphe.rivo.common.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan("com.danphe.rivo.repository.internal")
public class DataSourceConfig {

  private final AppConfig appConfig;

  public DataSourceConfig(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @PostConstruct
  void migrate() {
    if (appConfig.isFlywayEnabled()) {
      Flyway.configure()
          .dataSource(
              appConfig.getDbUrl(),
              appConfig.getDbUsername(),
              appConfig.getDbPassword())
          .schemas(appConfig.getFlywaySchemas())
          .defaultSchema(appConfig.getFlywayDefaultSchema())
          .locations(appConfig.getFlywayLocations())
          .baselineOnMigrate(appConfig.isFlywayBaselineOnMigrate())
          .baselineVersion(appConfig.getFlywayBaselineVersion())
          .load()
          .migrate();
    }
  }

  @Bean
  @Primary
  public DataSource dataSource() {
    HikariConfig hikari = new HikariConfig();
    hikari.setDriverClassName("org.postgresql.Driver");
    hikari.setJdbcUrl(appConfig.getDbUrl());
    hikari.setUsername(appConfig.getDbUsername());
    hikari.setPassword(appConfig.getDbPassword());
    hikari.setMaximumPoolSize(appConfig.getDbPoolMaxSize());
    hikari.setMinimumIdle(appConfig.getDbPoolMinIdle());
    hikari.setConnectionTimeout(appConfig.getDbPoolConnectionTimeout());
    hikari.setIdleTimeout(appConfig.getDbPoolIdleTimeout());
    hikari.setMaxLifetime(appConfig.getDbPoolMaxLifetime());
    hikari.addDataSourceProperty("stringtype", "unspecified");
    return new HikariDataSource(hikari);
  }

  @Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setMapperLocations(
        new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
    org.apache.ibatis.session.Configuration mybatis = new org.apache.ibatis.session.Configuration();
    mybatis.setMapUnderscoreToCamelCase(true);
    mybatis.getTypeHandlerRegistry().register(LocalDateTime.class, new RobustLocalDateTimeTypeHandler());
    factory.setConfiguration(mybatis);
    return factory.getObject();
  }
}
