package com.buaa.academic.search.service;

public interface InfoService {

    <T> T findDocument(Class<T> target, String id);

    <T> boolean hasDocument(Class<T> target, String id);

}