package com.onyshchenko.psanalyzer.services.searchutils;

import com.onyshchenko.psanalyzer.model.Category;
import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Genre;
import com.onyshchenko.psanalyzer.model.Price;
import com.onyshchenko.psanalyzer.model.Publisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

public class GameSpecification implements Specification<Game> {

    public static final String CURRENT_PRICE = "currentPrice";
    public static final String CURRENT_DISCOUNT = "currentDiscount";

    private final transient SearchCriteria criteria;

    public GameSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Game> root, @Nullable CriteriaQuery<?> criteriaQuery, @Nullable CriteriaBuilder criteriaBuilder) {

        Join<Game, Price> priceJoin = root.join("price");
        Join<Game, Publisher> publisherJoin = root.join("publisher");

        if (criteriaBuilder == null) {
            return null;
        }

        switch (criteria.getKey()) {
            case PRICE:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.equal(priceJoin.get(CURRENT_PRICE), criteria.getValue());
                } else if (criteria.getOperation().equalsIgnoreCase(">")) {
                    return criteriaBuilder.greaterThan(priceJoin.get(CURRENT_PRICE), criteria.getValue().toString());
                } else if (criteria.getOperation().equalsIgnoreCase("<")) {
                    return criteriaBuilder.lessThan(
                            priceJoin.get(CURRENT_PRICE), criteria.getValue().toString());
                }
                break;
            case CATEGORY:
                String filterName = criteria.getKey().getFilterName();

                var fullGamesPredicate = criteriaBuilder.equal(
                        root.get(filterName), Category.valueOf(Category.FULL.toString()));
                var gameBundlePredicate = criteriaBuilder.equal(
                        root.get(filterName), Category.valueOf(Category.PREMIUM.toString()));
                var premiumGamePredicate = criteriaBuilder.equal(
                        root.get(filterName), Category.valueOf(Category.GAME_BUNDLE.toString()));

                var notFullGamesPredicate = criteriaBuilder.notEqual(
                        root.get(filterName), Category.valueOf(Category.FULL.toString()));
                var notGameBundlePredicate = criteriaBuilder.notEqual(
                        root.get(filterName), Category.valueOf(Category.PREMIUM.toString()));
                var notPremiumGamePredicate = criteriaBuilder.notEqual(
                        root.get(filterName), Category.valueOf(Category.GAME_BUNDLE.toString()));

                if (criteria.getValue().equals("games")) {
                    return criteriaBuilder.or(fullGamesPredicate, gameBundlePredicate, premiumGamePredicate);
                } else if (criteria.getValue().equals("otherProducts")) {
                    return criteriaBuilder.and(notFullGamesPredicate, notGameBundlePredicate, notPremiumGamePredicate);
                } else {
                    return criteriaBuilder.equal(
                            root.get(criteria.getKey().getFilterName()), Category.valueOf(criteria.getValue().toString().toUpperCase()));
                }
            case GENRE:
                return criteriaBuilder.and(criteriaBuilder.and(root.join("genres")
                        .in(Genre.valueOf(criteria.getValue().toString().toUpperCase()))));
            case NAME:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey().getFilterName()), "%" + criteria.getValue().toString().toLowerCase() + "%");
                }
                break;
            case PUBLISHER:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            publisherJoin.get("searchName"), "%" + criteria.getValue().toString().toLowerCase() + "%");
                }
                break;
            case PUBLISHER_ID:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.equal(
                            publisherJoin.get("id"), criteria.getValue().toString());
                }
                break;
            case RELEASE:
                if (criteria.getOperation().equalsIgnoreCase("=")
                        && criteria.getValue().toString().equalsIgnoreCase("no")) {
                    return criteriaBuilder.greaterThan(
                            root.get(criteria.getKey().getFilterName()).as(LocalDate.class), LocalDate.now());
                }
                break;
            case DISCOUNT:
                if (criteria.getOperation().equalsIgnoreCase(">")) {
                    return criteriaBuilder.greaterThan(priceJoin.get(CURRENT_DISCOUNT), criteria.getValue().toString());
                }
                break;
            case PSPLUS:
                return criteriaBuilder.lessThan(
                        priceJoin.get("currentPsPlusPrice"), priceJoin.get(CURRENT_PRICE));
            case EAACCESS:
            case EXCLUSIVE:
                return criteriaBuilder.isTrue(root.get(criteria.getKey().getFilterName()));
            default:
                return null;
        }
        return null;
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }
}
