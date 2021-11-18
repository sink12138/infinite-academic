package com.buaa.academic.search.service;

import java.io.IOException;
import java.util.List;

public interface SuggestService {

    <T> List<String> completionSuggest(Class<T> target, String text);

    <T> List<String> phraseSuggest(Class<T> target, String text, String field);

}
