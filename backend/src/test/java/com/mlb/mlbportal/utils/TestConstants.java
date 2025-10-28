package com.mlb.mlbportal.utils;

import java.time.Year;

public final class TestConstants {

    private TestConstants() {}

    public static final String USER1_EMAIL = "fonssi@gmail.com";
    public static final String USER1_USERNAME = "fonssi29";
    public static final String USER1_PASSWORD = "123";

    public static final String USER2_EMAIL = "armin@gmail.com";
    public static final String USER2_USERNAME = "armiin13";
    public static final String USER2_PASSWORD = "345";

    public static final String TEST_USER_EMAIL = "test@gmail.com";
    public static final String TEST_USER_USERNAME = "testUser";
    public static final String TEST_USER_PASSWORD = "test";

    public static final String BASE_URI = "http://localhost";
    public static final String ME_PATH = "/api/auth/me";
    public static final String LOGIN_PATH = "/api/auth/login";
    public static final String REGISTER_PATH = "/api/auth/register";
    public static final String LOGOUT_PATH = "/api/auth/logout";
    public static final String FORGOT_PASSWORD_PATH = "/api/auth/forgot-password";
    public static final String RESET_PASSWORD_PATH = "/api/auth/reset-password";

    public static final String ALL_TEAMS_PATH = "/api/teams";
    public static final String STANDINGS_PATH = "/api/teams/standings";

    public static final String MATCHES_OF_DAY_PATH = "/api/matches/today";

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";

    public static final String VALID_CODE = "1234";
    public static final String INVALID_CODE = "5678";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String NEW_PASSWORD_ENCODED = "newPasswordEncoded";
    public static final String UNKNOWN_EMAIL = "unknown@gmail.com";
    public static final String INVALID_EMAIL = "email";

    public static final String TEST_TEAM1_NAME = "Team1";
    public static final String TEST_TEAM1_ABBREVIATION = "T1";
    public static final int TEST_TEAM1_WINS = 70;
    public static final int TEST_TEAM1_LOSSES = 79;
    public static final String TEST_TEAM1_LOGO = "Team1.png";
    public static final String TEST_TEAM1_CITY = "City1";
    public static final String TEST_TEAM1_INFO = "Information1";

    public static final String TEST_TEAM2_NAME = "Team2";
    public static final String TEST_TEAM2_ABBREVIATION = "T2";
    public static final int TEST_TEAM2_WINS = 84;
    public static final int TEST_TEAM2_LOSSES = 65;
    public static final String TEST_TEAM2_LOGO = "Team2.png";
    public static final String TEST_TEAM2_CITY = "City2";
    public static final String TEST_TEAM2_INFO = "Information2";

    public static final String TEST_TEAM3_NAME = "Team3";
    public static final String TEST_TEAM3_ABBREVIATION = "T3";
    public static final int TEST_TEAM3_WINS = 10;
    public static final int TEST_TEAM3_LOSSES = 119;
    public static final String TEST_TEAM3_LOGO = "Team3.png";
    public static final String TEST_TEAM3_CITY = "City3";
    public static final String TEST_TEAM3_INFO = "Information3";
    
    public static final String UNKNOWN_TEAM = "AnyTeam";

    public static final String STADIUM1_NAME = "Stadium1";
    public static final Year STADIUM1_YEAR = Year.of(1985);

    public static final String STADIUM2_NAME = "Stadium2";
    public static final Year STADIUM2_YEAR = Year.of(2000);

    public static final String STADIUM3_NAME = "Stadium3";
    public static final Year STADIUM3_YEAR = Year.of(2001);
}