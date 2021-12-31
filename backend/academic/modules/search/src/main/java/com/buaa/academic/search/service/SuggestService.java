package com.buaa.academic.search.service;

import java.util.List;

public interface SuggestService {

    <T> List<String> completionSuggest(Class<T> target, String text, String field, int size);

    <T> List<String> correctionSuggest(Class<T> target, String text, String[] phrases, int size);

}
