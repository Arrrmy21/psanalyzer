package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Page<Publisher> findAll(Pageable page);

    Optional<Publisher> findByName(String name);

    Optional<Publisher> findById(long id);
}