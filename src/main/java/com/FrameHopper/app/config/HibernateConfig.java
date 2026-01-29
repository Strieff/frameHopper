package com.FrameHopper.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {
    private final Environment environment;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public HibernateConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "entityManagerFactory")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.FrameHopper.app.Model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        try{
            final String prefix = "jdbc:sqlite:";

            String pathPart = dbUrl.substring(prefix.length());
            var dbPath = Path.of(pathPart).toAbsolutePath().normalize();
            var parent = dbPath.getParent();

            if (!Files.exists(parent))
                Files.createDirectories(parent);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return DataSourceBuilder.create()
                .driverClassName(environment.getProperty("spring.datasource.driver-class-name"))
                .url(environment.getProperty("spring.datasource.url"))
                .username(environment.getProperty("spring.datasource.username"))
                .password(environment.getProperty("spring.datasource.password"))
                .build();
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", environment.getProperty("hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.show_sql", environment.getProperty("hibernate.format_sql"));
        properties.put("hibernate.connection.autocommit", environment.getProperty("hibernate.connection.autocommit"));
        properties.put("hibernate.jdbc.time_zone", environment.getProperty("hibernate.jdbc.time_zone"));
        properties.put("hibernate.jdbc.batch_size", environment.getProperty("hibernate.jdbc.batch_size"));
        properties.put("hibernate.order_inserts", environment.getProperty("hibernate.order_inserts"));
        properties.put("hibernate.order_updates", environment.getProperty("hibernate.order_updates"));

        return properties;
    }

    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
