package com.MarketingMVP.AllVantage.Security.Utility;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {

    public static  final long ACCESS_JWT_EXPIRATION = 60*60*24;
    public static  final long REFRESH_JWT_EXPIRATION = 60*60*24*7;

    @Value("${jwt.access.secret}")
    public static String JWT_ACCESS_SECRET;
    @Value("${jwt.refresh.secret}")
    public static String JWT_REFRESH_SECRET;
}
