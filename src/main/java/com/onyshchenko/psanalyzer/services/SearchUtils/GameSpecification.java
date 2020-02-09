package com.onyshchenko.psanalyzer.services.SearchUtils;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.Price;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class GameSpecification implements Specification<Game> {

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
                    return criteriaBuilder.equal(priceJoin.get("currentPrice"), criteria.getValue());
                } else if (criteria.getOperation().equalsIgnoreCase(">")) {
                    return criteriaBuilder.greaterThan(priceJoin.get("currentPrice"), criteria.getValue().toString());
                } else if (criteria.getOperation().equalsIgnoreCase("<")) {
                    return criteriaBuilder.lessThan(
                            priceJoin.get("currentPrice"), criteria.getValue().toString());
                }
                break;
            case NAME:
                if (criteria.getOperation().equalsIgnoreCase("=")) {
                    return criteriaBuilder.like(
                            root.get(criteria.getKey().getFilterName()), "%" + criteria.getValue() + "%");
                }
            default:
                return null;
        }
        return null;
    }
}
