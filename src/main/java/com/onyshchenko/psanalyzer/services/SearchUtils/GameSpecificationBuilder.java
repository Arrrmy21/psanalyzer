package com.onyshchenko.psanalyzer.services.SearchUtils;

import com.onyshchenko.psanalyzer.model.Game;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

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

        Specification result = new GameSpecification(null, params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(getSpecification(params.get(i)));
        }

        return result;
    }
    private Specification getSpecification(SearchCriteria key){
        if (key.getKey().equals("currentPrice")){
            return new GameSpecification("price", key);
        }
        else return new GameSpecification(null, key);
    }
}
