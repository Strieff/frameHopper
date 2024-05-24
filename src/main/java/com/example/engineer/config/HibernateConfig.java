package com.example.engineer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Autowired
    private Environment environment;

    @Bean(name = "entityManagerFactory")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.example.engineer.Model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        /*DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty("spring.datasource.driver-class-name")));
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));*/
        return DataSourceBuilder.create()
                .driverClassName(/*environment.getProperty("spring.datasource.driver-class-name")*/"org.h2.Driver")
                .url(/*environment.getProperty("spring.datasource.url")*/"jdbc:h2:./data/videoFrameDatabase;AUTO_SERVER=TRUE")
                .username(/*environment.getProperty("spring.datasource.username")*/ "sa")
                .password(/*environment.getProperty("spring.datasource.password")*/"")
                .build();
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", /*environment.getProperty("spring.jpa.properties.hibernate.dialect")*/"org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", /*environment.getProperty("spring.jpa.hibernate.ddl-auto")*/"update");
        properties.put("hibernate.show_sql", /*environment.getProperty("spring.jpa.show-sql")*/true);
        return properties;
    }

    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
