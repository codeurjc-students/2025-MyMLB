# 👤 User Stories

## 🧾 Table of Contents
- [Basic Features](#-basic-features)
  - [User Registration](#user-registration)
  - [See Application General Information](#see-application-general-information)
  - [User Login](#user-login)
  - [User Logout](#user-logout)
  - [Add Favorite Team](#add-favorite-team)
  - [Remove Favorite Team](#remove-favorite-team)
  - [Edit Profile Settings](#edit-profile-settings)
  - [Update Team Information](#update-team-information)
  - [Edit Player Information](#edit-player-information)
  - [Edit Stadium Information](#edit-stadium-information)
  - [Create Stadium](#create-stadium)
  - [Create Player](#create-player)
  - [Delete Player](#delete-player)
- [Intermediate Features](#-intermediate-features)
  - [Delete Account](#delete-account)
  - [Ticket Purchase](#ticket-purchase)
  - [Notifications via Email](#notifications-via-email)
  - [Contact Support via Email](#contact-support-via-email)
- [Advanced Features](#-advanced-features)
  - [User's Favourite Teams Statistics](#users-favourite-teams-statistics)
  - [Ticket Selling per Team Statistics](#ticket-selling-per-team-statistics)

## 🟢 Basic Features
**These features were included in version 0.1.**

### User Registration
**As a:** Anonymous User.

**I want to:** Create an account in the web application.

**So that:** I can access exclusive features, such as buying tickets for a game.

#### Acceptance Criteria
- The registration form must ask for:
  - Username.
  - Email.
  - Password.
- The username must be unique.

#### Dependencies
- The user must not already exists in the application's database.

#### Tests
- Verify that the entered username by the user, does not exist before allowing the registration.
- Attempting to register an user that already exist, should show an error message.

---
### See Application General Information
**As a:** Anonymous User.

**I want to:** See the general information of the application.

**So that:** I can inform myself about every team in the league.

#### Acceptance Criteria
- This information must be accesible without login.
- It must be displayed on the main page (Home).
- The home page must include an option to sign in/sign up.
- The standings for each league and division must be correctly displayed.
- A dropdown menu listing every team in the league must be available for the user.
- Selecting a team must redirect to that team's detailed view.

#### Tests
- Verify that the home page is accessible without authentication.
- Verify that the standings are correctly displayed.
- Verify that selecting a team displays correctly its detailed information.
- Verify that selecting the sign in/sign up option redirects to the login/register page.
- Attempting to purchase a ticket for any game should redirect to the login/register page.

---
### User Login
**As a:** Registered User.

**I want to:** Log into my account.

**So that:** I can access my personal dashboard and features.

#### Acceptance Criteria
- The login form must ask for:
  - Username.
  - Password.
- If the credentials are valid, the user is redirected to the main page.
- If the credentials are invalid, an error message is displayed.
- A "Recover my Password" option is available via email.

#### Dependencies
- The user must be register in the application's database.

#### Tests
- Verify that the entered username and password are both correct.
- Attempting to log in with an user that does not exists, should show an error message.
- If the user selects the "Recover my Password" option; verify that the recovery email is sent successfully.

---
### User Logout
**As a:** Registered User.

**I want to:** Log out from my account.

**So that:** I can close my session and switch to another account.

#### Acceptance Criteria
- The user's data must not be deleted during the log out process.
- The current session must be properly terminated.
- The user must be redirected to the login/register page.

#### Dependencies
- The system must have session/token management implemented.

#### Tests
- Verify that the user's account still exists in the database after logging out.
- Verify that the user is redirected to the login/register page once the session is closed.
- Verify that accessing a restricted page once the session is closed, instantly redirects to the login/register page.

---
### Add Favorite Team
**As a:** Registered User.

**I want to:** Select a team as my "favorite".

**So that:** I can personalized the information that the application provides me.

#### Acceptance Criteria
- The selected team must not already be in the list of favorites.

#### Tests
- Verify that a user cannot add the same team more than once to their favorites.
- Verify that favorite teams are stored correctly and can be retrieved/displayed.
- Verify that the matches of the day and standings are displayed, prioritizing the teams marked as favorites by the user.

---
### Remove Favorite Team
**As a:** Registered User.

**I want to:** Remove a team as my "favorite".

**So that:** I can udpate my preferences.

#### Acceptance Criteria
- The current ammount of favorite teams that exists on the list, must be updated.
- After removal, the user can add a new favorite team.
- A confirmation message may be shwown before the removal.

#### Dependencies
- The team must be currently present in the user's list.

#### Tests
- Verify that a team can be removed from the list of favorites.
- Verify if the list is correctly updated after the removal.

---

### Edit Profile Settings
**As a:** Registered User.

**I want to:** Edit my account settings.

**So that:** I personalize my personal settings.

#### Acceptance Criteria
- The user must be able to edit the following fields:
  - Email.
  - Password.
  - Profile Picture.
  - Enable/Dissable Notifications.
- A confirmation message should appear before completing the operation.
- A success message should be displayed after the operation completes successfully.

#### Dependencies
- The new password must be different from the current one.

#### Tests
- Verify that profile changes are saved correctly in the database.
- Verify that any invalid input should thrown an error message.
- Verify that after saving, the new settings is correctly shown to the user.

---

### Update Team Information
**As a:** Admin.

**I want to:** Update the editable team information.

**So that:** The team's data remains up to date.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- The admin must be able to edit the following fields:
  - City.
  - General information of the team.
  - The championships.
  - The stadium.
- Once the information has been updated, a success message must be displayed.

#### Dependencies
- The team must already exists in the database.
- If the stadium will be modify, the new stadium must not be associated with any team.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the editable fields of a team are correctly saved in the database.
- Verify that the success message is correctly displayed.
- Verify that entering invalid values triggers an error message.

---
### Edit Player Information
**As a:** Admin.

**I want to:** Edit the player information.

**So that:** The player's data can be updated whenever is needed.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- The admin must be able to edit the following fields:
  - Player Picture.
  - Team he plays.
  - Position he plays.
  - Total At Bats (AB).
  - Number of Hits (H).
  - Number of Run Batted In (RBI).
  - Number of Home Runs (HR).
- The Batting Average (Avg) must be automatically calculated from AB and H.
- Once is finished, a success message must be displayed.
- Every numeric value must be positive.
- In case the player change teams, the new team must be a valid one (the team's roster should be less than 24 players).
- In case the player change position, the new position must be a valid one.

#### Dependencies
- The player must already exists in the database.
- The new team must exist in the database and not have a full roster.
- The new position must be a valid one.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the new player's picture is correctly saved in the database.
- Verify that the new player's team is correctly reflected in the player's profile.
- Verify that the new team correctly registers the new player in its roster.
- Verify that the total ammount of ABs a player have are correctly updated.
- Verify that the ammount of Hits of a player are correctly updated.
- Verify that the ammount of RBIs are correctly updated.
- Verify that the ammount of HR are correctly updated.
- Verify that the Batting Avg is correctly calculated.
- Verify that entering invalid values triggers an error message.
- Verify that entering a non valid team triggers an error message.
- Verify thah entering a non valid position triggers an error message.
- Verify that a success message is displayed after a successfull operation.

---
### Edit Stadium Information
**As a:** Admin.

**I want to:** Edit the stadium information.

**So that:** The stadium's data can be updated whenever is needed.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- The admin must be able to edit the following fields:
  - Stadium Picture.
- Once is finished, a success message must be displayed.

#### Dependencies
- The stadium must already exists in the database.
- The number of pictures must not exceed 5.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the new stadium's picture is correctly saved in the database.
- Verify that the success message is correctly displayed once the operation is completed.

### Create Stadium
**As a:** Admin.

**I want to:** Create a new stadium.

**So that:** new stadiums can be added into the system.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- The admin must fill the following fields in order to create the stadium:
  - Name of the stadium.
  - Opening date.
- Once is finished, a success message must be displayed.

#### Dependencies
- The stadium must not exists in the database.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the inputs are valid.
- Verify that the success message is correctly displayed once the operation is completed.

### Create Player
**As a:** Admin.

**I want to:** Add a new player to the MLB roster.

**So that:** New players making their league debut will be eligible to be included in the app.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- The admin must fill the following fields in order to create the player:
  - Name of the player.
  - Position of the player.
  - Team he will play.
  - Number of the player.
- Once is finished, a success message must be displayed.

#### Dependencies
- The player must not exists in the database.
- The team must not have a full roster.
- The position must be a valid one.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the inputs are valid.
- Verify that the entered team does not have a full roster.
- Verify that the entered position is a valid one.
- Verify that the success message is correctly displayed once the operation is completed.

### Delete Player
**As a:** Admin.

**I want to:** Remove a player from the MLB roster.

**So that:** Players who retire or leave MLB may be removed from the application.

#### Acceptance Criteria
- An admin-only section must be available for this operation.
- A confirmation modal should appear before deleting the player.
- Once the operation is confirmed, a success message should be displayed.

#### Dependencies
- The player must exists in the database.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the player does not exists in the database.

---
## 🟡 Intermediate Features

### Delete Account
**As a:** Registered User.

**I want to:** Delete my account from the application.

**So that:** I can remove all my personal data and stop using the application services.

#### Acceptance Criteria
- The "delete account" option is available from the user account settings.
- The user must confirm the action.
- Once confirmed, all the account's data will be permanently remove from the application.

#### Dependencies
- The user must be logged in to access its account settings.
- All associated data (e.g., tickets) must be dettach from that account.

#### Tests
- Verify that the delete option is only visible to registered user.
- Verify that the deletion process removes all of the user related data from the database.
- Verify that attempting to log in with deleted credentials returns an error message.

---

### Ticket Purchase
**As a:** Registered User.

**I want to:** Purchase ticket(s).

**So that:** I can attend matches played by my favorite teams.

#### Acceptance Criteria
- The user must complete the purchase form with valid inputs.
- A success message should be displayed once the operation is successfully completed.
- If any error occur, an error message should inform the user of the issue.
- After the purchase, the ticket(s) must be registered under its respective user (ticket list).
- After the purchase, the tickets should appear in "My Tickets".
- The system must handle any concurrency problems that might occur during the purchase process.

#### Dependencies
- The number of tickets requested must be less or equal to the number of tickets available.
- The number of selected seats must be equal to the number of selected tickets.

#### Tests
- Verify that the user can purchase a ticket with a valid input.
- Verify that attempting to purchase more tickets than are available results in an error.
- Verify that the number of selected seats are equal to the number of selected tickets.
- Verify that a success message appear after the purchase.
- Verify that the ticket(s) are saved in the user's ticket list after the purchase.
- Verify that the ticket(s) purchased appear in the "My Tickets" section after the purchase.
- Verify that the total number of available tickets for the respective type is updated correctly in the database after the purchase.

---

### Notifications via Email
**As a:** Registered User.

**I want to:** Receive notifications from the apllication via email.

**So that:** I can be notified about importan events like tickets on sale, upcoming match dates, profile changes, etc.

#### Acceptance Criteria
- The user must be able to enable or disable email notifications from their settings.
- The notifications are sent only if the user enabled them.
- The notifications must cover important events/actions such as: purchase confirmation, change in any of the personal settings, etc.

#### Dependencies
- User preferences for notifications must be stored and accessible.

#### Tests
- Verify that emails are sent correctly when the notifications are enabled.
- Verify that no emails are sent when the notifications are disabled.
- Verify that the changes in the notifications preferences are correctly updated and stored.

---
### Contact Support via Email
**As a:** Guest.

**I want to:** Send a message to report a problem or asking for support.

**So that:** I can get help regarding the issue from the application's administrators.

#### Acceptance Criteria
- The user can fill out a contact form with the following fields:
  - Email (if it is a guest user).
  - Subject.
  - Body.
- The message must be sent to the admins via email.
- A confirmation message must be displayed to the user after a successfull submission.
- The messages must be displayed at the admin's inbox in the application.

#### Tests
- Verify that the contact form send the message correctly.
- Verify that the confirmation message is correctly displays.
- Verify that error messages appears when the required fields are empty or invalid.
- Verify that the admin can reply to the user successfully.
- Verify that the admin can close any support ticket successfully.

---
## 🔴 Advanced Features

### User's Favourite Teams Statistics
**As a:** Admin.

**I want to:** See the statistics about which teams are most frequently selected as "favourite" for the users.

**So that:** I can identify which teams are the most beloved according to the users.

#### Acceptance Criteria
- An admin-only section must be available to display this data.
- The data will be display through a horizontal bar chart.
- The chart must update dynamically based on the user´s preferences.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the data updates when the users modify their favoruite teams.

---
### Ticket Selling per Team Statistics
**As a:** Admin.

**I want to:** See the statistics about which teams have sold the most tickets.

**So that:** I can identify which teams are the most entertaining and interesting to watch according to the users.

#### Acceptance Criteria
- An admin-only section must be available to display this data.
- The data will be display through a bar chart.
- The chart must update dynamically as ticket sales occur.

#### Tests
- Verify that this section is only visible to admins.
- Verify that the data updates when the users purchases tickets.

---
[👈 Return to README](../README.md)
