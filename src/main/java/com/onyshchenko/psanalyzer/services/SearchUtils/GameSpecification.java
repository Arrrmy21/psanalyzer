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

    private String field;

    public GameSpecification(String field, SearchCriteria criteria) {
        this.field = field;
        this.criteria = criteria;
    }

    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

        if (field != null && field.equals("price") && criteria.getOperation().equalsIgnoreCase("=")){
            Join<Game, Price> priceJoin = root.join("price");
            return criteriaBuilder.equal(
                    priceJoin.get("currentPrice"), criteria.getValue() );
        }
        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get(criteria.getKey()), criteria.getValue().toString());
        } else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return criteriaBuilder.lessThanOrEqualTo(
                    root.get(criteria.getKey()), criteria.getValue().toString());
        } else if (criteria.getOperation().equalsIgnoreCase("=")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return criteriaBuilder.like(
                        root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
            } else {
                return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }
        return null;
    }
}
