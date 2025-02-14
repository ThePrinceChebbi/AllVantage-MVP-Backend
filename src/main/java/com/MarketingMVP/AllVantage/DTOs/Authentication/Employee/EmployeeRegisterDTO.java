package com.MarketingMVP.AllVantage.DTOs.Authentication.Employee;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class EmployeeRegisterDTO {

    //TODO: Fix all the fields to match the UserEntity fields


    private String firstName;

    private String lastName;

    private String username;

    private Date creationDate;

    private String email;

    private String password;

    private String phoneNumber;
}
