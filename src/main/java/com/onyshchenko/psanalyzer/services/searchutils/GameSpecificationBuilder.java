package com.onyshchenko.psanalyzer.services.searchutils;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.RequestFilters;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameSpecificationBuilder {

    private final List<SearchCriteria> params;

    public GameSpecificationBuilder() {
        params = new ArrayList<>();
    }

    public void with(RequestFilters key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
    }

    public Specification<Game> build() {
        if (params.isEmpty()) {
            return null;
        }

        Specification<Game> result = new GameSpecification(params.get(0));
        for (int i = 1; i < params.size(); i++) {
            result = Objects.requireNonNull(Specification.where(result)).and(new GameSpecification(params.get(i)));
        }
        return result;
    }
}
