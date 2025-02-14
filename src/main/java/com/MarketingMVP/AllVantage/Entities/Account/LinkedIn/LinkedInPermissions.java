package com.MarketingMVP.AllVantage.Entities.Account.LinkedIn;

public enum LinkedInPermissions {
    // Member Profile API Permissions
    R_BASICPROFILE,     // Access to basic profile information (name, headline, photo, etc.)
    R_EMAILADDRESS,     // Access to the member's primary email address
    R_PROFILE,          // Access to the full member profile (more detailed than basic profile)
    R_CONTACTINFO,      // Access to the member's contact information (phone number, address) - *Requires special permission and justification*
    R_ADVERTISING,      // Access to member's engagement data with ads.

    // Share API Permissions (for posting updates, articles, etc.)
    W_MEMBER_SOCIAL,    // Permission to share content on the member's behalf
    RW_COMPANY_ADMIN, // Allows to post on company pages the member is admin of.

    // Groups API Permissions
    RW_GROUPS,          // Permission to manage and post to groups the member belongs to.
    R_GROUPS,           // Permission to read data from groups the member belongs to.

    // Jobs API Permissions
    R_JOB_POSTINGS,  //Permission to access job postings
    W_JOB_POSTINGS,  //Permission to create and manage job postings

    // Messaging API Permissions
    W_MESSAGES,         // Permission to send messages to other members (use with caution, rate limits apply)
    R_MESSAGES,         // Permission to read messages.

    // Other Permissions (less common or require special access)
    // These may require special approval from LinkedIn and have specific use cases.
    // Be very cautious and consult LinkedIn's documentation.

    // V2 Permissions (may be deprecated or limited)
    // These are older permissions and might not be relevant for most modern integrations.
    // Check LinkedIn's API documentation for the latest information.
    // Examples (Check the docs!):
    // R_FULLPROFILE, (Deprecated)
    // ... other v2 permissions

    // Note:  LinkedIn's API and permissions can change.
    // Always refer to the official LinkedIn Developer Documentation
    // for the most up-to-date and accurate list of permissions.
}
