package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.dao.PublisherRepository;
import com.onyshchenko.psanalyzer.model.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PublisherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherService.class);

    @Autowired
    private PublisherRepository publisherRepository;

    public Page<Publisher> getListOfAllPublishers(PageRequest pageRequest) {
        LOGGER.info("Getting list of all publishers.");
        return publisherRepository.findAll(pageRequest);
    }

    public Optional<Publisher> findById(long id) {
        LOGGER.info("Getting Publisher by id [{}].", id);
        return publisherRepository.findById(id);
    }

    public Optional<Publisher> findByName(String name) {
        LOGGER.info("Getting Publisher by name [{}].", name);
        return publisherRepository.findByName(name);
    }
}
