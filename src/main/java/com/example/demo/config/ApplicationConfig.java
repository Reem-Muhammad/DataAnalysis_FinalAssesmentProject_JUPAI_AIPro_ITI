package com.example.demo.config;

import com.example.demo.DataProcessing;
import com.example.demo.Reader;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.xml.crypto.Data;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    @Autowired
    private Environment env;

    @Value("${app.name:demo}")
    private String appName;

    @Value("${spark.home}")
    private String sparkHome;

    @Value("${master.uri:local}")
    private String masterUri;

    @Bean
    public SparkConf sparkConf() {
        SparkConf sparkConf = new SparkConf()
                .setAppName(appName)
                .setSparkHome(sparkHome)
                .setMaster(masterUri);

        return sparkConf;
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkConf());
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession
                .builder()
                .sparkContext(javaSparkContext().sc())
                .appName("Data")
                .getOrCreate();
    }

    @Bean
    public Dataset<Row> csvData() {
        Reader reader = new Reader();
        SparkSession sparksession = sparkSession();
        Dataset<Row> data = reader.readData("Wuzzuf_Jobs.csv", sparksession);
        data = DataProcessing.cleanData(data, sparksession);
        return data;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("landing-page");
    }
}