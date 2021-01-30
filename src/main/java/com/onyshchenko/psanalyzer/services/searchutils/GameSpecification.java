package com.onyshchenko.psanalyzer.services.searchutils;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Price;
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
            case NAME:
            case PUBLISHER:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey().getFilterName()), "%" + criteria.getValue().toString().toLowerCase() + "%");
                }
                break;
            case RELEASE:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    if (criteria.getValue().toString().equalsIgnoreCase("no")) {
                        return criteriaBuilder.greaterThan(
                                root.get(criteria.getKey().getFilterName()).as(LocalDate.class), LocalDate.now());
                    }
                }
                break;
            case DISCOUNT:
                if (criteria.getOperation().equalsIgnoreCase(">")) {
                    return criteriaBuilder.greaterThan(priceJoin.get(CURRENT_DISCOUNT), criteria.getValue().toString());
                }
                break;
            default:
                return null;
        }
        return null;
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }
}
