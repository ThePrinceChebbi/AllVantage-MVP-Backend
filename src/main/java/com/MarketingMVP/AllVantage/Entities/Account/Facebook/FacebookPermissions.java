package com.MarketingMVP.AllVantage.Entities.Account.Facebook;

public enum FacebookPermissions {
    PAGES_MANAGE_ADS,
    PAGES_MESSAGING,

    // Basic Permissions (Often granted by default)
    PUBLIC_PROFILE, // Access to basic profile information
    EMAIL,         // Access to the user's email address
    USER_FRIENDS,   // Access to the user's friends list (use with caution, deprecated)

    // Extended Permissions (Require explicit user consent)
    USER_ABOUT_ME,      // Access to the "About Me" section of the profile
    USER_ACTIONS_BOOKS, // Access to the user's book actions
    USER_ACTIONS_FITNESS,// Access to the user's fitness actions
    USER_ACTIONS_MUSIC, // Access to the user's music actions
    USER_ACTIONS_NEWS,  // Access to the user's news actions
    USER_ACTIONS_VIDEO, // Access to the user's video actions
    USER_BIRTHDAY,     // Access to the user's birthday
    USER_EDUCATION,    // Access to the user's education information
    USER_EVENTS,       // Access to the user's events
    USER_GAMES_ACTIVITY,// Access to the user's games activity
    USER_HOMETOWN,     // Access to the user's hometown
    USER_LIKES,        // Access to the user's likes
    USER_LOCATION,     // Access to the user's location
    USER_PHOTOS,       // Access to the user's photos
    USER_POSTS,        // Access to the user's posts
    USER_RELATIONSHIP,  // Access to the user's relationship status
    USER_RELIGION_POLITICS, // Access to the user's religious and political views
    USER_TAGGED_PLACES, // Access to the places the user has been tagged in
    USER_VIDEOS,       // Access to the user's videos
    USER_WEBSITE,       // Access to the user's website

    // Page-Specific Permissions (Already included some above)
    PAGES_CREATE_ADS,       // Create ads on behalf of a Page
    PAGES_MANAGE_CTA,        // Manage call to action button on a Page
    PAGES_MANAGE_INSTANT_ARTICLES, // Manage Instant Articles for a Page
    PAGES_MANAGE_LIFECYCLE_APPS, // Manage lifecycle apps for a Page
    PAGES_MANAGE_MESSAGES,   // Manage messages for a Page (similar to PAGES_MESSAGING, may be redundant)
    PAGES_MANAGE_OFFERS,     // Manage offers for a Page
    PAGES_MANAGE_PARTNERSHIPS, // Manage partnerships for a Page
    PAGES_MANAGE_POSTS,      // Already included
    PAGES_MANAGE_ROLES,     // Manage roles for a Page
    PAGES_READ_ENGAGEMENT,   // Already included
    PAGES_READ_USER_CONTENT, // Read user-generated content on a Page
    PAGES_SHOW_LIST,         // Already included

    // Business-Specific Permissions (For managing Facebook Business accounts)
    BUSINESS_MANAGEMENT, // Access to business management features

    // These permissions are often deprecated or require special review by Facebook
    // Be very cautious when requesting them and check Facebook's latest documentation.
    // Examples (Do your research before using!):
    // USER_STATUS, // Access to the user's status updates (often restricted)
    // USER_CHECKINS // Access to the user's check-ins (often restricted)
    // ... other potentially sensitive permissions

    // Add any other permissions as needed based on Facebook's API documentation.
    // It's crucial to consult the official documentation for the most up-to-date list.
}
