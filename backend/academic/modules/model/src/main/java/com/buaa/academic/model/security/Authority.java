package com.buaa.academic.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authority {

    private String userId;

    private String researcherId;

    private boolean isAdmin = false;

}
