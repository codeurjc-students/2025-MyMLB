# 2025-MyMLB

## Author and Tutor Information
<table>
  <thead>
    <th>Name</th>
    <th>Role</th>
    <th>University Account</th>
    <th>GitHub Account</th>
  </thead>
  <tbody>
    <tr>
      <td>Alfonso Rodríguez Gutt</td>
      <td>Student (Author)</td>
      <td>a.rodriguezgu.2022@alumnos.urjc.es</td>
      <td>AlfonsoRodr</td>
    </tr>
    <tr>
      <td>Iván Chicano Capelo</td>
      <td>Tutor</td>
      <td>ivan.chicano@urjc.es</td>
      <td>ivchicano</td>
    </tr>
  </tbody>
</table>

## Table of Contents
- [Entities](#entities)
- [Relations Between Entities](#relations-between-entities)
- [Types of Users and Browsing Permissions](#type-of-users-and-browsing-permissions)
- [Entities with Images](#entities-with-images)
- [Graphs](#graphs)
- [User Stories](#user-stories)
  - [User Register](#user-register)
  - [See Application General Information](#see-application-general-information)
  - [User Login](#user-login)
  - [User Logout](#user-logout)
  - [Delete Account](#delete-account)
  - [Add Favourite Team](#add-favourite-team)
  - [Remove Favourite Team](#remove-favourite-team)
  - [Edit Profile Settings](#edit-profile-settings)
  - [Ticket Purchase](#ticket-purchase)
  - [Cancel Ticket Purchase](#cancel-ticket-purchase)
  - [Notifications via Email](#notifications-via-email)
  - [Contact Support via Email](#contact-support-via-email)
  - [Update Team Information](#update-team-information)
  - [Edit Player Information](#edit-player-information)
  - [Edit Stadium Information](#edit-stadium-information)
  - [Edit Ticket](#edit-ticket)
  - [Create and Edit a Game](#create-and-edit-a-game)
  - [Update Game Score](#update-game-score)

## Entities
- User
- Team
- Stadium
- Player
- Game
- Ticket
- PasswordResetToken

## Relations between Entities
### User
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Team</td>
      <td>1..N</td>
    </tr>
    <tr>
      <td>Ticket</td>
      <td>1..N</td>
    </tr>
    <tr>
      <td>PasswordResetToken</td>
      <td>1..1</td>
    </tr>
  </tbody>
</table>

### Team
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>User</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Player</td>
      <td>1..N</td>
    </tr>
    <tr>
      <td>Stadium</td>
      <td>1..1</td>
    </tr>
    <tr>
      <td>Game</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### Player
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Team</td>
      <td>1..1</td>
    </tr>
  </tbody>
</table>

### Stadium
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Team</td>
      <td>1..1</td>
    </tr>
  </tbody>
</table>

### Game
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Team</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Ticket</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### Ticket
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>User</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Game</td>
      <td>N..1</td>
    </tr>
  </tbody>
</table>

### PasswordResetToken
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>User</td>
      <td>1..1</td>
    </tr>
</table>

## Type of Users and Browsing Permissions
### Anonymous User
- See the general information provided by the application.

### Registered User
- See both general and personalized information provided by the application.
- Access its profile settings.
- Delete Account.
- Add/Remove a team from the favourite list.
- Buy tickets for a game.
- Cancel ticket purchase.
- Receive notifications via email.
- Contact support via email.

### Administrator (Admin)
- Update team information.
- Edit player information.
- Edit stadium information.
- Add/Modify tickets.
- Create and Edit a Game.
- Update Game score.

## Entities with Images
- User
- Team
- Stadium
- Player

## Graphs
A graph that reflects the last 10 games of the team (TBD).

## User Stories

### User Register
**As a:** Anonymous User.

**I want to:** Create an account in the web application.

**So that:** I can access exclusive features, such as buying tickets for a game.

#### Acceptance Criteria
- The registration form must ask for:
  - Full name (first and last name).
  - Password.
  - Email.
- The email must be unique.

#### Dependencies
- The user must not already exists in the application`s database.

#### Tests
- Verify that the entered email by the user does not exist before allowing the registration.
- Attempting to register an user that already exist, should show an error message.

<!-- ------------------------------------------------ See Application General Information ------------------------------- -->
### See Application General Information
**As a:** Anonymous User.

**I want to:** See the general information of the application.

**So that:** I can inform myself about every team in the league.

#### Acceptance Criteria
- This information must be accesible without login.
- It must be displayed on the main page (Home).
- The home page must include an option to sign in/sign up.
- The standings for each league and division must be correctly displayed.
- The news around the league must be correctly displayed (TBD).
- A dropdown menu listing every team in the league must be available for the user.
- Selecting a team must redirect to that team´s detailed view.

#### Tests
- Verify that the home page is accessible without authentication.
- Verify that the standings are correctly displayed.
- Verify that selecting a team displays correctly its detailed information.
- Verify that the news around the league are correctly displayed (TBD).
- Verify that selecting the sign in/sign up option redirects to the login/register page.
- Attempting to purchase a ticket for any game should redirect to the login/register page.

<!-- ------------------------------------------------ Login ------------------------------- -->
### User Login
**As a:** Registered User.

**I want to:** Log into my account.

**So that:** I can access my personal dashboard and features.

#### Acceptance Criteria
- The login form must ask for:
  - Email.
  - Password.
- If the credentials are valid, the user is redirected to the main page.
- If the credentials are invalid, an error message is displayed.
- A "Recover my Password" option is available via email.

#### Dependencies
- The user must be register in the application´s database.

#### Tests
- Verify that the entered email and password are both correct.
- Attempting to log in with an user that does not exists, should show an error message.
- If the user selects the "Recover my Password" option; verify that the recovery email is sent successfully.

<!-- ------------------------------------------------ Log out ------------------------------- -->
### User Logout
**As a:** Registered User.

**I want to:** Log out from my account.

**So that:** I can close my session and switch to another account.

#### Acceptance Criteria
- The user´s data must not be deleted during the log out process.
- The current session must be properly terminated.
- The user must be redirected to the login/register page.

#### Dependencies
- The system must have session/token management implemented.

#### Tests
- Verify that the user´s account still exists in the database after logging out.
- Verify that the user is redirected to the login/register page once the session is closed.
- Verify that accessing a restricted page once the session is closed, instantly redirects to the login/register page.

<!-- ------------------------------------------------ Delete Account ------------------------------- -->
### Delete Account
**As a:** Registered User.

**I want to:** Delete my account from the application.

**So that:** I can remove all my personal data and stop using the application services.

#### Acceptance Criteria
- The "delete account" option is available from the user account settings.
- The user must confirm the action.
- Once confirmed, all the account´s data will be permanently remove from the application.

#### Dependencies
- The user must be logged in to access its account settings.
- All associated data (e.g., tickest) must be dettach from that account.

#### Tests
- Verify that the delete option is only visible to registered user.
- Verify that the deletion process removes all of the user related data from the database.
- Verify that attempting to log in with deleted credentials returns an error message.

<!-- ------------------------------------------------ Add favourite team ------------------------------- -->
### Add Favourite Team
**As a:** Registered User.

**I want to:** Select a team as my "favourite".

**So that:** I can personalized the information that the application provides me.

#### Acceptance Criteria
- The selected team must not already be in the list of favourites.
- The number of favourites team must not exceed the maximun allowed (3 (can change)).

#### Tests
- Verify that a user cannot add the same team more than once to their favourites.
- Verify that adding a team beyond the allowed limit is prevented and shows a proper message.
- Verify that favourite teams are stored correctly and can be retrieved/displayed.

<!-- ------------------------------------------------ Remove Fav Team ------------------------------- -->
### Remove Favourite Team
**As a:** Registered User.

**I want to:** Remove a team as my "favourite".

**So that:** I can udpate my preferences.

#### Acceptance Criteria
- The current ammount of favourite teams that exists on the list, must be updated.
- After removal, the user can add a new favourite team.
- A confirmation message may be shwown before the removal (TBD).

#### Dependencies
- The team must be currently present in the user´s list.

#### Tests
- Verify that a team can be removed from the list of favourites.
- Verify if the list is correctly updated after the removal.
- Verify that removing a favourite allows a new one to be added within the limit.

<!-- ------------------------------------------------ Edit Profile Settings ------------------------------- -->
### Edit Profile Settings
**As a:** Registered User.

**I want to:** Edit my account settings.

**So that:** I personalize my personal settings.

#### Acceptance Criteria
- The user must be able to edit the following fields:
  - Username.
  - Password.
  - Profile Picture.
  - Favourite Team(s).
- A confirmation message should appear before completing the operation.
- A success message should be displayed after the operation completes successfully.

#### Dependencies
- The new password must be different from the current one.
- The list of favourite teams must comply the defined requirements.

#### Tests
- Verify that profile changes are saved correctly in the database.
- Verify that any invalid input should thrown an error message.
- Verify that after saving, the new settings is correctly shown to the user.

<!-- ------------------------------------------------ Ticket Purchase ------------------------------- -->
### Ticket Purchase
**As a:** Registered User.

**I want to:** Purchase ticket(s).

**So that:** I can attend matches played by my favourite teams.

#### Acceptance Criteria
- The user must complete the purchase form with valid inputs.
- A success message should be displayed once the operation is successfully completed.
- If any error occur, an error message should inform the user of the issue.
- After the purchase, the ticket(s) must be registered under its respective user (ticket list).
- After the purchase, the tickets should appear in "My Tickets", within the user´s account settings.
- The system must handle any oncurrency problems that might occur during the purchase process.

#### Dependencies
- The number of tickets requested must be less or equal to the number of tickets available.

#### Tests
- Verify that the user can purchase a ticket with a valid input.
- Verify that attempting to purchase more tickets than are available results in an error.
- Verify that a success message appear afte the purchase.
- Verify that the ticket(s) are saved in the user`s ticket list after the purchase.
- Verify that the ticket(s) purchased appear in the "My Tickets" section after the purchase.
- Verify that the total number of available tickets for the respective type is updated correctly in the database after the purchase.

<!-- ------------------------------------------------ Cancel Ticket Purchase ------------------------------- -->
### Cancel Ticket Purchase
**As a:** Registered User.

**I want to:** Cancel my ticket purchase.

**So that:** I can undo the operation if I change my mind.

#### Acceptance Criteria
- The cancellation option must be available to the user after selecting the ticket quantity.
- Undoing the purchase of any ticket must not register any ticket under the respective user.
- After the operation, a success message must be displayed.
- The ammount of ticket of the selected type must restore accordingly.
- The system must handle any oncurrency problems that might occur during the cancellation.

#### Dependencies
- The cancellation must happen before the purchase is confirmed.

#### Tests
- Verify that the ammount of tickets previously selected is never registered under the respective user.
- Verify that the ammount of available tickets of the selected type is correctly restored.
- Verify that after the operation is successfully completed, a success message is displayed.

<!-- ------------------------------------------------ Notifications ------------------------------- -->
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
- Verify that the changes in the notifications preferences are correctly updated and stores.

<!-- ------------------------------------------------ Contact Support ------------------------------- -->
### Contact Support via Email
**As a:** Registered User.

**I want to:** Send a message to report a problem or asking for support.

**So that:** I can get help regarding the issue from the application´s administrators.

#### Acceptance Criteria
- The user can fill out a contact form with the following fields:
  - User´s email.
  - Subject.
  - Body.
- The message must be sent to the application´s administrators via email.
- A confirmation message must be displayed to the user after a successfull submission.  

#### Tests
- Verify that the contact form send the message correctly.
- Verify that the confirmation message is correctly displays.
- Verify that error messages appears when the required fields are empty or invalid.

<!-- ------------------------------------------------ Update Team Information ------------------------------- -->
### Update Team Information
**As a:** Admin.

**I want to:** Update the editable team information.

**So that:** The team´s data remains up to date.

#### Acceptance Criteria
- The admin must be able to edit the following fields:
  - Number of Wins.
  - Number of Losses.
  - Last 10 Games.
- All other team stats must be automatically calculated based on the previously mentioned stats.
- Once the information has been updated, a success message must be displayed.
- Every quantity (except the run differential) must be a positive number.
- The team`s position in the standings must be updated automatically based on the new data.

#### Dependencies
- The team must already exists in the database.

#### Tests
- Verify that the updated number of wins/losses of a team is correctly saved in the database.
- Verify that the total number of games played by a team is correctly calculated.
- Verify that the win percentage of a team is correctly calculated.
- Verify that the games behind (if its not in the first position) of a team is correctly calculated.
- Verify that the run differential is correctly calculated.
- Verify that the team´s position within its division is correctly updated.
- Verify that the success message is correctly displayed.
- Verify that entering invalid values triggers an error message.

<!-- ------------------------------------------------ Edit Player Information ------------------------------- -->
### Edit Player Information
**As a:** Admin.

**I want to:** Edit the player information.

**So that:** The player`s data can be updated whenever is needed.

#### Acceptance Criteria
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
- In case the player change teams, the new team must be a valid one (exists in the database).
- In case the player change position, the new position must be a valid one (exists in the database).

#### Dependencies
- The player must already exists in the database.
- The new team must exist in the database.
- The new position must be a valid one.

#### Tests
- Verify that the new player´s picture is correctly saved in the database.
- Verify that the new player´s team is correctly reflected in the player´s profile.
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

<!-- ------------------------------------------------ Edit Stadium Information ------------------------------- -->
### Edit Stadium Information
**As a:** Admin.

**I want to:** Edit the stadium information.

**So that:** The stadium`s data can be updated whenever is needed.

#### Acceptance Criteria
- The admin must be able to edit the following fields:
  - Stadium Picture.
- Once is finished, a success message must be displayed.

#### Dependencies
- The stadium must already exists in the database.

#### Tests
- Verify that the new stadium´s picture is correctly saved in the database.
- Verify that the success message is correctly displayed once the operation is completed.

<!-- ------------------------------------------------ Edit Ticket ------------------------------- -->
### Edit Ticket
**As a:** Admin.

**I want to:** Edit the tickets information.

**So that:** The ticket`s data can be modified whenever is needed.

#### Acceptance Criteria
- The admin must be able to edit the following fields:
  - Type.
  - Price.
  - Date or time.
  - Status (Active or Inactive).
- A sucess message must be displayed after a sucessfull update.
- The price must be a positive number.

#### Dependencies
- The ticket must already exists in the system.
- The associated user or stadium must remain untouchable.

#### Tests
- Verify that invalid values displays an error message.
- Verify that the updated information is correctly reflected.
- Verify that a sucess message appears after a sucessfull operation.
- Verify that the updated information persist in the database.
- Verify that the price is a valid value.
- Verify that if any field have an invalid value, an error message is displayed.
- Verify that inactive tickets no longer appears on the user´s ticket list or in the public purchase view.

<!-- ------------------------------------------------ Create and Edit a Game ------------------------------- -->
### Create and Edit a Game
**As a:** Admin.

**I want to:** Create and Edit a match.

**So that:** A match is establish between two teams, and modify any information if needed.

#### Acceptance Criteria
- The administrator must be able to create a game by selecting:
  - Home Team.
  - Away Team.
  - Date and Time.
  - Stadium.
- A team cannot play again itself.
- The stadium must be the one of the Home Team.
- A success message must appear after the operation.

#### Dependencies
- A game must not overlap in schedule with another game at the same stadium.

#### Tests
- Verify that a game can be created successfully.
- Verify that a success message appears after a successfull operation.
- Verify that an error message appears when attempting to create a game with invalid data.
- Verify that a game cannot be created in a stadium that is already booked at the same time.
- Verify that the stadium the game is being held, is the Home Team stadium.
- Verify that the updated data is correctly stored.

<!-- ------------------------------------------------ Update Game Score ------------------------------- -->
### Update Game Score
**As a:** Admin.

**I want to:** Update the score and inning of the game.

**So that:** I can keep the game information up to date.

#### Acceptance Criteria
- The administrator must be able to update the following fields:
  - Runs scored by home and away teams.
  - Current inning.
  - Game status (Scheduled, In Progress, Finished).
- Scores and innings must be positive integers.
- The game status can only progress forwards (ej: Not going back from "In Progress" to "Scheduled").

#### Dependencies
- The game must exists.
- The game must already started (the current date is the same or later than the scheduled date).  

#### Tests
- Verify that the score and inning are correctly updated.
- Verify that the score and inning are valid inputs.
- Verify that an invalid input triggers an error message.
- Verify that the transition of the status are valid (forwards).
