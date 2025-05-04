package com.MarketingMVP.AllVantage.Entities.Platform_Specific.Instagram;

public enum InstagramPermissions {
    // Basic Permissions (Often granted by default)
    INSTAGRAM_BASIC,         // Access to basic user profile information (id, username, profile picture)

    // Public Content Permissions
    INSTAGRAM_CONTENT_READ,  // Read access to public content
    INSTAGRAM_CONTENT_PUBLISH, // Permission to publish content (photos, videos, stories) - Requires specific approval

    // Comment Permissions
    INSTAGRAM_MANAGE_COMMENTS, // Permission to manage comments on the user's media

    // Message Permissions (for business accounts)
    INSTAGRAM_MANAGE_MESSAGES, // Permission to manage messages on the user's business account (for specific use cases)

    // Insights Permissions (for business accounts)
    INSTAGRAM_INSIGHTS,       // Access to business insights data (reach, impressions, etc.) - Requires business account

    // Shop Permissions (for business accounts)
    INSTAGRAM_SHOP_READ, // Permission to read shop data.
    INSTAGRAM_SHOP_WRITE, // Permission to manage shop data.

    // Reels Permissions (for business accounts)
    INSTAGRAM_REELS_PUBLISH, // Permission to publish Reels content.

    // Story Permissions (for business accounts)
    INSTAGRAM_STORY_READ, // Permission to read stories.
    INSTAGRAM_STORY_WRITE, // Permission to create and manage stories.

    // User Data Permissions (Read-only)
    INSTAGRAM_USER_PROFILE,   // Access to detailed user profile information (bio, website, counts)
    INSTAGRAM_USER_MEDIA,      // Access to the user's media (posts, photos, videos)

    // Advanced API Permissions (Require review and approval)
    // These permissions are often more sensitive and require a strong justification.
    // Be very cautious when requesting them and check Instagram's latest documentation.
    // Examples (Do your research before using!):

    // Business Discovery Permissions (for partners)
    INSTAGRAM_BUSINESS_DISCOVERY, // Access to business discovery capabilities

    // Note: Instagram's API and permissions can change.
    // Always refer to the official Instagram Platform Documentation
    // for the most up-to-date and accurate list of permissions.
}
