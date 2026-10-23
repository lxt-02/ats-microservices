/**
 * Technical building blocks shared by every module (exceptions, HTTP envelope, current user,
 * clock, MyBatis type handlers). Contains no business logic.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.ats.shared;
