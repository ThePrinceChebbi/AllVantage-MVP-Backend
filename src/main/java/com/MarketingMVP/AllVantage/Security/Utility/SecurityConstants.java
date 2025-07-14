package com.MarketingMVP.AllVantage.Security.Utility;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {

    @Value("${jwt.access.secret}")
    public static String JWT_ACCESS_SECRET;
    @Value("${jwt.refresh.secret}")
    public static String JWT_REFRESH_SECRET;
    public static final long ACCESS_JWT_EXPIRATION = 1000L * 60 * 60 * 24; // 1 day in ms
    public static  final long REFRESH_JWT_EXPIRATION = 1000L * 60 * 60 * 24 * 7;
}
