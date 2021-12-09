package com.buaa.academic.scholar.service;

public interface ApplicationService {
    String saveAppWithFile(String userId, String email, String fileToken, String type);
    String saveAppWithLink(String userId, String email, String websiteLink, String type);
}
