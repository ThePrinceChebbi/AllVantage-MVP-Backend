package com.MarketingMVP.AllVantage.Entities.Account.Snapchat;

public enum SnapchatPermissions {

    // Core Permissions (Generally available)
    PROFILE,            // Access to basic profile information (display name, username, bitmoji)
    SNAP_SEND,          // Permission to send Snaps to friends
    SNAP_RECEIVE,       // Permission to receive Snaps from friends
    FRIENDS,            // Access to the user's friend list
    STORY_VIEW,         // Permission to view friends' Stories
    STORY_POST,         // Permission to post Stories (requires specific review for some use cases)

    // Discover Permissions (Access to Discover content)
    DISCOVER_READ,      // Read access to Discover content (Publishers, Shows)

    // Lens Permissions (For Lens Studio developers)
    LENS_CREATION,      // Permission to create and publish Lenses (for developers)
    LENS_USE,          // Permission to use Lenses (maybe granted automatically with Lens creation)

    // Bitmoji Permissions
    BITMOJI_AVATAR,       // Access to the user's Bitmoji avatar

    // Location Permissions (Use with extreme caution, requires strong justification)
    LOCATION,            // Access to the user's location (very sensitive, requires explicit user consent and justification)

    // Marketing API Permissions (For advertisers)
    AD_ACCOUNT_MANAGEMENT, // Permission to manage ad accounts
    CAMPAIGN_MANAGEMENT,   // Permission to manage ad campaigns

    // Advanced Permissions (Require specific approval and justification)
    // These permissions are often more sensitive and require a strong justification.
    // Be very cautious when requesting them and check Snapchat's latest documentation.
    // Examples (Do your research before using!):

    // Note: Snapchat's API and permissions can change.
    // Always refer to the official Snapchat Developer Documentation
    // for the most up-to-date and accurate list of permissions.
}
