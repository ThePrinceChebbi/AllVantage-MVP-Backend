package com.MarketingMVP.AllVantage.DTOs.Authentication.Client;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientRegisterDTO {

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String phoneNumber;

    private String address;

    private String state;

    private String country;

    private String postalCode;
}
