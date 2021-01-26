package com.onyshchenko.psanalyzer.dao;

import com.onyshchenko.psanalyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query(
            value = "select distinct users.user_id as id from users " +
                    "inner JOIN users_wish_list on users_wish_list.user_id = users.user_id " +
                    "left outer join games on games.game_id = users_wish_list.wish_list " +
                    "left outer join prices on prices.id = games.prices_id " +
                    "where (prices.current_discount > 0)", nativeQuery = true
    )
    ArrayList<Long> getIdOfUsersThatHaveDiscountOnGameInWishlist();
}