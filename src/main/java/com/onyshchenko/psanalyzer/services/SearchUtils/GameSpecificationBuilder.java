package com.onyshchenko.psanalyzer.services.SearchUtils;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameSpecificationBuilder {

    private final List<SearchCriteria> params;

    public GameSpecificationBuilder() {
        params = new ArrayList<>();
    }

    public GameSpecificationBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<Game> build() {
        if (params.size() == 0) {
            return null;
        }
        List<Specification> specs = params.stream()
                .map(GameSpecification::new)
                .collect(Collectors.toList());

        Specification result = specs.get(0);

        return result;
    }
}
