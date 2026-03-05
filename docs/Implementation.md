# Implementation

- Data management from Stats API.
- Architecture.
- Code Organization.
- Problems.
- Environments (Front and Back. Environment variables etc)
- SeatBatchService


## @EntityGraph: 
Optimization tool that simplifies the `JOIN` usage to load entities. 

Its main function is to avoid the problem of the  `N + 1 queries`, allowing the `EntityManager` to bring the main entity with its foreing tables within a single query.

Furthermore, this helps with the clarity and readability of the code, making queries much easier to read.
