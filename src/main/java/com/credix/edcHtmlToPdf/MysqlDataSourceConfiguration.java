package com.credix.edcHtmlToPdf;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "mySqlEDCEntityManagerFactory",
        transactionManagerRef = "mySqlEDCTransactionManager",
        basePackages = "com.credix.edcHtmlToPdf.mysqlEDC.repositories"
)
public class MysqlDataSourceConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "credix.edc.connecmysql.datasource.mysql")
    public DataSource mySqlEDCDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mySqlEDCEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mySqlEDCEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mySqlEDCDataSource())
                .packages("com.credix.edcHtmlToPdf.mysqlEDC.entities")
                .persistenceUnit("mySqlPU")
                .build();
    }

    @Bean(name = "mySqlEDCTransactionManager")
    public PlatformTransactionManager mySqlEDCTransactionManager(
            @Qualifier("mySqlEDCEntityManagerFactory") EntityManagerFactory mySqlEDCEntityManagerFactory) {
        return new JpaTransactionManager(mySqlEDCEntityManagerFactory);
    }
}
