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
- [Types of Users and Browsing Permissions](#type-of-users-and-browsing-permissions)
- [Entities with Images](#entities-with-images)
- [Relations Between Entities](#relations-between-entities)
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
TBD

### Registered User
TBD

### Administrator (Admin)
TBD

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

### Acceptance Criteria
TBD.

### Dependencies
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

### Acceptance Criteria
- The current ammount of favourite teams that exists on the list, must be updated.
- After removal, the user can add a new favourite team.
- A confirmation message may be shwown before the removal (TBD).

### Dependencies
- The team must be currently present in the user´s list.

#### Tests
- Verify that a team can be removed from the list of favourites.
- Verify if the list is correctly updated after the removal.
- Verify that removing a favourite allows a new one to be added within the limit.

TO BE CONTINUED...
