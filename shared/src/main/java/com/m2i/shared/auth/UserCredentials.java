package com.m2i.shared.auth;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserCredentials implements Serializable {
    private String username;
    private String password;
}