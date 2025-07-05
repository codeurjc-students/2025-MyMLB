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

## Entities
- User
- Team
- Stadium
- Player
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
      <td>Stadium</td>
      <td>N..1</td>
    </tr>
  </tbody>
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

### Administrator (Admin)
- Edit team information.
- Edit player information.
- Edit stadium information.
- Add/Modify tickets.

## Entities with Images
- User
- Team
- Stadium
- Player

## Graphs
TBD

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

<!-- ------------------------------------------------ See Team Information ------------------------------- -->
### See Team Information
**As a:** Anonymous User.

**I want to:** See the information of every team.

**So that:** I can inform myself about every team in the league.

#### Acceptance Criteria
TBD.

#### Dependencies
TBD.

#### Tests
TBD.

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
### Remove a Favourite Team
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
- If any error occurr, an error message should inform the user of the issue.
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
