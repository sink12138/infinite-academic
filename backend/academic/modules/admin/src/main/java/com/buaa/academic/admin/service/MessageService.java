package com.buaa.academic.admin.service;

import com.buaa.academic.document.system.Application;

public interface MessageService {

    void sendRejectMessage(Application application, String reason);

    void sendAcceptMessage(Application application);

}
