package com.buaa.academic.search.service;

public interface InfoService {

    <T> T findDocument(Class<T> target, String id);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    <T> boolean existsDocument(Class<T> target, String id);

}
