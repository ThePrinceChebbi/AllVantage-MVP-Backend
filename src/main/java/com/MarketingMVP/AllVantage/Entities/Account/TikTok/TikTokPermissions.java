package com.MarketingMVP.AllVantage.Entities.Account.TikTok;

public enum TikTokPermissions {

    // Basic User Information
    USER_INFO_BASIC,      // Access to basic user profile information (username, display name, profile picture)

    // Video Permissions
    VIDEO_LIST,           // Access to the user's public video list
    VIDEO_VIEW,           // Permission to view public videos
    VIDEO_UPLOAD,         // Permission to upload videos (requires review and approval)

    // Comment Permissions
    COMMENT_READ,         // Permission to read comments on videos
    COMMENT_WRITE,        // Permission to post comments on videos (requires review and approval)

    // Follow/Following Permissions
    FOLLOW_READ,          // Permission to read the user's followers and following lists
    FOLLOW_WRITE,         // Permission to follow and unfollow users (requires review and approval)

    // Like Permissions
    LIKE_READ,            // Permission to read liked videos
    LIKE_WRITE,           // Permission to like and unlike videos

    // Share Permissions
    SHARE,                // Permission to share videos

    // Profile Permissions (More detailed user info)
    USER_INFO_PROFILE,      // Access to more detailed user profile information (bio, website, etc.)

    // Message Permissions (Limited and restricted)
    MESSAGE_SEND,         // Permission to send messages (highly restricted, requires specific approval and justification)
    MESSAGE_READ,         // Permission to read messages (highly restricted, requires specific approval and justification)

    // Analytics Permissions (For business accounts)
    ANALYTICS_READ,       // Access to analytics data (views, likes, shares, etc.) - Requires business account

    // Live Streaming Permissions (For creators)
    LIVE_STREAM_PUSH,     // Permission to push live streams (requires specific approval)
    LIVE_STREAM_PULL,     // Permission to pull live streams

    // Advanced Permissions (Require specific approval and justification)
    // These permissions are often more sensitive and require a strong justification.
    // Be very cautious when requesting them and check TikTok's latest documentation.
    // Examples (Do your research before using!):

    // Note: TikTok's API and permissions can change.
    // Always refer to the official TikTok Developer Documentation
    // for the most up-to-date and accurate list of permissions.
}
