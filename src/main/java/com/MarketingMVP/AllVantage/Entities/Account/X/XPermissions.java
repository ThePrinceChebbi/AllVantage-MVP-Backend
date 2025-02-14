package com.MarketingMVP.AllVantage.Entities.Account.X;

public enum XPermissions {
    // Core Permissions (Most commonly used)
    READ,                       // Read Tweets, profiles, likes, followers, lists, etc.
    WRITE,                      // Post Tweets, Retweets, like Tweets, follow/unfollow accounts, manage lists, mute/block users.

    // Direct Message Permissions
    DIRECT_MESSAGES_READ,       // Read Direct Messages
    DIRECT_MESSAGES_WRITE,      // Send and delete Direct Messages

    // User Profile Permissions
    ACCOUNT_UPDATE,            // Update account profile information (name, bio, location, website, profile/banner image)
    EMAIL,                      // Access the user’s verified email address (Requires special permission and justification)

    // Offline Access (For long-lived operations)
    OFFLINE_ACCESS,           // Allows your app to access the API on behalf of the user even when they are offline (Use with caution)

    // Advanced Permissions (Require specific approval)
    // These permissions are often more sensitive and require a strong justification.
    // Be very cautious when requesting them and check Twitter's latest documentation.
    // Examples (Do your research before using!):

    // Muted and Blocked Accounts Permissions
    MUTE_READ,            // Allows to read muted accounts
    MUTE_WRITE,           // Allows to mute accounts
    BLOCK_READ,           // Allows to read blocked accounts
    BLOCK_WRITE,          // Allows to block accounts

    // Follower and Following Permissions
    FOLLOW, // Allows to follow and unfollow accounts

    // Tweet and Retweet Permissions
    TWEET_READ, // Allows to read tweets.
    TWEET_WRITE, // Allows to create, edit, and delete tweets.

    // List Permissions
    LIST_READ, // Allows to read lists.
    LIST_WRITE, // Allows to create, edit, and delete lists.

    // Space Permissions
    SPACE_READ, // Allows to read Spaces data

    // Note: Twitter's API and permissions can change.
    // Always refer to the official Twitter Developer Documentation
    // for the most up-to-date and accurate list of permissions.
}
