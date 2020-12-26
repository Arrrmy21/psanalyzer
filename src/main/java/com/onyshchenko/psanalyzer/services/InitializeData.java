package com.onyshchenko.psanalyzer.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InitializeData {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeData.class);

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {

        LOGGER.info("Loading predefined data to database.");
        ResourceDatabasePopulator resourceDatabasePopulator =
                new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
        resourceDatabasePopulator.execute(dataSource);
        LOGGER.info("Loading predefined data to database FINISHED.");
    }
}