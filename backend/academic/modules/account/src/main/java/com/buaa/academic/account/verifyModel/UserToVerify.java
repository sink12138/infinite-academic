package com.buaa.academic.account.verifyModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToVerify implements Serializable {

    private String userId;

    private String email;
}
