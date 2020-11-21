package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query("select userId from User users")
    ArrayList<Long> getAllIds();
}