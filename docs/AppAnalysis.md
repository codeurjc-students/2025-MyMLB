# 📊 Application Analysis: Entities, Relations, and User Permissions

## 🧾 Table of Contents
- [Entities](#-entities)
- [Relations Between Entities](#-relations-between-entities)
- [Types of Users and Browsing Permissions](#-type-of-users-and-browsing-permissions)
- [Entities with Images](#-entities-with-images)
- [Charts](#-charts)
- [Algorithm or Advanced Query](#-algorithm-or-advanced-query)

## 🧠 Entities
- UserEntity
- Team
- Stadium
- Player
- PositionPlayer
- Pitcher
- Match
- Event
- EventManager
- Sector
- Seat
- Ticket
- PasswordResetToken
- SupportMessage
- SupportTicket

> [!IMPORTANT]
> `Player` apart of being a current entity is also an `abstrac class`, from which the entities `PositionPlayer` and `Pitcher` inherit.

> [!IMPORTANT]
> The `PaswordResetToken` entity will be used to give the user an opportunity to create a new password (in case the user have forgotten the previous one, and clicks the "Forgot My Password" option).

> [!IMPORTANT]
> The entities `Ticket` and `SupportTicket` arec completely different. `Ticket` refers to the ticket a user uses to attend a match, while `SupportTicket` refers to an issue a user opens to contact the admins of the application.

---

## 🪢 Relations between Entities
### 🙍 UserEntity
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Team</td>
      <td>N..N</td>
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

### ⚾ Team
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>User</td>
      <td>N..N</td>
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
      <td>Match</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### 🏃 Player
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
  </tbody>
</table>

### 🏟️ Stadium
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
      <td>Match</td>
      <td>1..N</td>
    </tr>
    <tr>
      <td>Sector</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### 🆚 Match
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
      <td>Stadium</td>
      <td>N..1</td>
    </tr>
  </tbody>
</table>

### Event
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Match</td>
      <td>1..1</td>
    </tr>
    <tr>
      <td>EventManager</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### EventManager
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  <tbody>
    <tr>
      <td>Event</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Sector</td>
      <td>N..1</td>
    </tr>
  </tbody>
  </thead>
</table>

### Sector
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Stadium</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Seat</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

### Seat
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>Sector</td>
      <td>N..1</td>
    </tr>
  </tbody>
</table>

### 🎟️ Ticket
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>EventManager</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>User</td>
      <td>N..1</td>
    </tr>
    <tr>
      <td>Seat</td>
      <td>1..1</td>
    </tr>
  </tbody>
</table>

### 🔐 PasswordResetToken
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

### SupportMessage
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>SupportTicket</td>
      <td>N..1</td>
    </tr>
  </tbody>
</table>

### SupportTicket
<table>
  <thead>
    <th>Related with...</th>
    <th>Cardinality</th>
  </thead>
  <tbody>
    <tr>
      <td>SupportMessage</td>
      <td>1..N</td>
    </tr>
  </tbody>
</table>

Below is the relational diagram illustrating all the entities and their relationships within the application.


```mermaid
erDiagram
  MATCH {
    long id
    Team homeTeam
    Team awayTeam
    int homeScore
    int awayScore
    LocalDateTime date
    Stadium stadium
    MatchStatus status
  }

  PASSWORDRESETTOKEN {
    long id
    String code
    UserEntity user
    LocalDateTime expirationDate
  }

  PITCHER {
    PitcherPositions position
    int games
    double era
    int wins
    int losses
    double inningsPitched
    int totalStrikeouts
    int walks
    int hitsAllowed
    int runsAllowed
    int saves
    int saveOpportunities
    double whip
  }

  PLAYER {
    long id
    String name
    int playerNumber
    Team team
    PictureInfo picture
  }

  POSITIONPLAYER {
    PlayerPositions position
    int atBats
    int hits
    int walks
    int homeRuns
    int rbis
    double average
    double obp
    double ops
    int doubles
    int triples
    double slugging
  }

  STADIUM {
    long id
    String name
    int openingDate
    List_PictureInfo pictures
    PictureInfo pictureMap
    Team team
    List_Match matches
    List_Sector sectors
  }

  SUPPORTMESSAGE {
    UUID id
    SupportTicket supportTicket
    String senderEmail
    String body
    boolean isFromUser
    LocalDateTime creationDate
  }

  SUPPORTTICKET {
    UUID id
    String subject
    String userEmail
    SupportTicketStatus status
    LocalDateTime creationDate
    List_SupportMessage messages
    Long version
  }

  TEAM {
    long id
    String name
    String abbreviation
    int totalGames
    int wins
    int losses
    double pct
    double gamesBehind
    String lastTen
    String teamLogo
    String city
    String generalInfo
    List_Integer championships
    League league
    Division division
    List_Match homeMatches
    List_Match awayMatches
    Stadium stadium
    List_PositionPlayer positionPlayers
    List_Pitcher pitchers
    Set_UserEntity favoritedByUsers
  }

  EVENT {
    long id
    Match match
    List_EventManager eventManagers
  }

  EVENTMANAGER {
    long id
    Event event
    Sector sector
    double price
    int availability
  }

  SEAT {
    long id
    String name
    Sector sector
    boolean isOccupied
  }

  SECTOR {
    long id
    String name
    Stadium stadium
    List_Seat seats
    int totalCapacity
  }

  TICKET {
    long id
    EventManager eventManager
    UserEntity owner
    String ownerName
    Seat seat
    LocalDateTime purchaseDate
  }

  USERENTITY {
    long id
    String email
    String username
    String name
    String password
    PictureInfo picture
    boolean enableNotifications
    PasswordResetToken resetToken
    Set_Team favTeams
    List_String roles
    List_Ticket tickets
  }

  MATCH }o--|| TEAM : ""
  MATCH }o--|| STADIUM : ""
  PASSWORDRESETTOKEN ||--|| USERENTITY : ""
  PLAYER }o--|| TEAM : ""
  STADIUM ||--|| TEAM : ""
  STADIUM ||--o{ SECTOR : ""
  SUPPORTMESSAGE }o--|| SUPPORTTICKET : ""
  TEAM ||--o{ POSITIONPLAYER : ""
  TEAM ||--o{ PITCHER : ""
  TEAM }|--|{ USERENTITY : ""
  EVENT ||--|| MATCH : ""
  EVENT ||--o{ EVENTMANAGER : ""
  EVENTMANAGER }o--|| SECTOR : ""
  SEAT }o--|| SECTOR : ""
  TICKET }o--|| EVENTMANAGER : ""
  TICKET }o--|| USERENTITY : ""
  TICKET ||--|| SEAT : ""
```

---

## 🔒 Type of Users and Browsing Permissions
### 🕵️‍♂️ Anonymous User
- See the general information provided by the application.

### 🧑‍💻 Registered User
- See both general and personalized information provided by the application.
- Access its profile settings.
- Delete Account.
- Add/Remove a team from the favourite list.
- Buy tickets for a game.
- Cancel ticket purchase.
- Receive notifications via email.
- Contact support via email.

### 🔑 Administrator (Admin)
- Update team information.
- Edit player information.
- Edit stadium information.
- Add/Modify tickets.
- Create and Edit a Game.
- Update Game score.
- User´s favourite teams statistics.
- Ticket selling per team statistics.

---

## 📷 Entities with Images
- User
- Team
- Stadium
- Player

---

## 📊 Charts
<table>
  <thead>
    <th>Chart Topic</th>
    <th>Type of Chart</th>
  </thead>
  <tbody>
    <tr>
      <td>Standings (Dynamic)</td>
      <td>Table</td>
    </tr>
    <tr>
      <td>Last 10 Games of a Team</td>
      <td>Line</td>
    </tr>
    <tr>
      <td>Ticket Selling for Each Team</td>
      <td>Bar</td>
    </tr>
    <tr>
      <td>User's Favorite Teams</td>
      <td>Horizontal Bar</td>
    </tr>
  </tbody>
</table>

> [!NOTE]
> The information described above is subject to change. Both the chart types and topics may be updated as needed.

---

### 📚 Library To Use
Since the front-end will be develop using `Angular`, the library chosen for creating all the charts will be `ng2-charts`, which is a wrapper for the popular chart library `Chart.js`.

---

## 🔍 Algorithm or Advanced Query

### Internal Messaging System
The application features an internal messaging system between users and admins to report any issues encountered. The workflow is straightforward: a user creates a support ticket which is then routed to the admins. Admins access these tickets through a personal inbox, where they can reply to or close them.

To ensure seamless operation, the system integrates `Java Mail Sender` for notifications, a `polling mechanism` to provide admins with real-time updates on the amount of open tickets, and a `concurrency control system`. This last component prevents multiple admins from simultaneously editing or closing the same ticket. This safety measure is implemented via `Optimistic Locking`, which utilizes database versioning to automatically manage concurrent access and lock tickets currently under review.

### Event Management

---
[👈 Return to README](../README.md)
