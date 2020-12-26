package com.onyshchenko.psanalyzer.services.searchutils;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Price;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class GameSpecification implements Specification<Game> {

    public static final String CURRENT_PRICE = "currentPrice";

    private SearchCriteria criteria;

    public GameSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        Join<Game, Price> priceJoin = root.join("price");

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
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey().getFilterName()), "%" + criteria.getValue().toString().toLowerCase() + "%");
                }
                break;
            case PUBLISHER:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey().getFilterName()), "%" + criteria.getValue().toString().toLowerCase() + "%");
                }
            default:
                return null;
        }
        return null;
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }
}
