package com.onyshchenko.psanalyzer.services;

import com.onyshchenko.psanalyzer.model.Game;
import com.onyshchenko.psanalyzer.model.RequestFilters;
import com.onyshchenko.psanalyzer.services.SearchUtils.GameSpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.xml.bind.ValidationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FilteringUtils {

    public static Specification<Game> getSpecificationFromFilter(String filter) throws ValidationException {

        GameSpecificationBuilder builder = new GameSpecificationBuilder();

        Pattern pattern = Pattern.compile("(\\w+)([=<>])(\\w+(-| |)\\w+)",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(filter + ",");
        while (matcher.find()) {
            builder.with(validateKeyForSearch(matcher.group(1)), matcher.group(2), matcher.group(3));
        }
        return builder.build();
    }

    private static RequestFilters validateKeyForSearch(String key) throws ValidationException {
        switch (key) {
            case "name":
                return RequestFilters.NAME;
            case "price":
                return RequestFilters.PRICE;
            case "userId":
                return RequestFilters.USERID;
            default:
                throw new ValidationException("Filter parameter {" + key + "} is not valid.");
        }
    }
}
