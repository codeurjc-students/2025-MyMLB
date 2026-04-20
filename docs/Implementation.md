# Implementation

> [!NOTE]
> This section contains only the key points and a brief explanation of some of the topics that will be covered here; the full description of all of them can be found in the official document for this project.

- Data management from Stats API.
- Architecture.
- Code Organization.
- Problems.
- Environments (Front and Back. Environment variables etc)
- SeatBatchService
- Resilience.
- EntityManager.

---

## Design Patterns
### Backend
The backend follows the `Hexagonal Architecture` architectural pattern.

<table>
  <thead>
    <th>Element</th>
    <th>Pattern</th>
    <th>Category</th>
  </thead>
  <tbody>
    <tr>
      <td>@Transactional and @Cacheable</td>
      <td>Proxy (AOP)</td>
      <td>Structural</td>
    </tr>
    <tr>
      <td>Dependency Injection</td>
      <td>Inversion of Control (IoC)</td>
      <td>Creational</td>
    </tr>
    <tr>
      <td>@Service, @Component, etc</td>
      <td>Singleton</td>
      <td>Creational</td>
    </tr>
    <tr>
      <td>Circuit Breaker (Resilience4J)</td>
      <td>Circuit Breaker</td>
      <td>Resilience or Behavior</td>
    </tr>
    <tr>
      <td>@Reposritory and @RestController</td>
      <td>Adapter</td>
      <td>Structural</td>
    </tr>
    <tr>
      <td>@Scheduled</td>
      <td>Observer</td>
      <td>Behavior</td>
    </tr>
    <tr>
      <td>Mappers (MapStruct)</td>
      <td>Mapper or Assembler</td>
      <td>Structural</td>
    </tr>
  </tbody>
</table>

> [!TIP]
> When referring to `AOP` as a pattern design in the table, it is referring to `Aspect-oriented programming`. AOP is a paradigm that serves to separate `cross-cutting concerns` from the main logic of your application. In other words, it allows you to extract repetitive code that is not part of the application's business logic and automatically execute it at any point in the application where it is needed. This is something that can be seen in the methods of the `Service Layer` under the `@Transactional` annotation and do the following things:
> 1) **Before:** The `Transactional Aspect` opens the connection.
> 2) **Target:** Run the method.
> 3) **After:** The `Aspect` performs the transaction `commit` automatically.


### Frontend
The frontend follows a `Component-Service Pattern`, which is an evolution of the `Model View Controller (MVC)` pattern. To be specific, both are `Software Architecture Patterns`

<table>
  <thead>
    <th>Element</th>
    <th>Pattern</th>
    <th>Category</th>
  </thead>
  <tbody>
    <tr>
      <td>Interceptors</td>
      <td>Chain of Responsability</td>
      <td>Behavior</td>
    </tr>
    <tr>
      <td>Observables (Reactivity with RxJS)</td>
      <td>Observer</td>
      <td>Behavior</td>
    </tr>
    <tr>
      <td>Guards</td>
      <td>Proxy/Strategy</td>
      <td>Structural</td>
    </tr>
    <tr>
      <td>Services</td>
      <td>Singleton</td>
      <td>Creational</td>
    </tr>
    <tr>
      <td>Models (Types)</td>
      <td>Data Transfer Object (DTO)</td>
      <td>Structural</td>
    </tr>
    <tr>
      <td>Dependendy Injection</td>
      <td>Inversion of Control (IoC)</td>
      <td>Creational</td>
    </tr>
  </tbody>
</table>

---

## Optimizations
- Cache system in the backend (Caffeine).
- Indexes in some entities, on those attributes on which recurring searches are performed
- @EntityGraph


### @EntityGraph: 
Optimization tool that simplifies the `JOIN` usage to load entities. 

Its main function is to avoid the problem of the  `N + 1 queries`, allowing the `EntityManager` to bring the main entity with its foreing tables within a single query.

Furthermore, this helps with the clarity and readability of the code, making queries much easier to read.
