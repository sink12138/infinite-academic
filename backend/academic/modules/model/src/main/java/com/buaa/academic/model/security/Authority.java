package com.buaa.academic.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authority implements Serializable {

    private String userId;

    private String researcherId;

    private boolean isAdmin = false;

}
