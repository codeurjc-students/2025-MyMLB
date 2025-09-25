# 🎯 Objectives

## 🧾 Table of Contents
- [Functionals](#-functionals)
- [Technicals](#-technicals)

## 📌 Functionals
The main functional objective of this application is to offer baseball fans a complete and personalized portal for Major League Baseball (MLB), allowing them to have access to general league information, team and player data, standings and match results, as well as access interactive services such as purchasing tickets, personalizing content based on favorite teams, and allow the user to stay up to date on the latest that is happening around league by enabling email notifications.

### Main Features
1. Display general MLB information, including standings and match results.
2. Display teams and players information.
3. Allow the users to sign-up and authenticate themselves to have access to exclusive services.
4. Allow the registered users to mark a team as a favourite.

> [!NOTE]
> In the [User Stories](#-user-stories) section you can find all of the features the application will provide.

## 💻 Technicals
The application will be developed as a web platform with a client-server architecture using the **HTTPS** protocol to ensure application security, data protection, and secure communication. 

The **backend** will be implemented with **Spring Boot** to manage the business logic, communication with the **MySQL database**, and the exposure of services through a REST API. 

The **frontend** will be implemented with **HTML, CSS (it`s being considered to use TailwindCSS), JavaScript, and Angular**, offering a responsive interface. The **ng2-charts** library will be integrated to display statistical data (charts).

**Postman** will be used during the development and testing of REST services, and the workflow will include version control with **Git**. Additionally, **Docker** will be used as part of the Continuous Integration (CI), Continuous Delivery (CD), and Continuous Deployment processes, ensuring that the application can be built, packaged, tested, and deployed consistently across different environments. 

To ensure the software quality, unit and integration testing will be performed using **Mockito** and **AssertJ**, as well as system/end-to-end (e2e) testing using **Selenium**.

### Main Technical Features
1. Spring Boot as the backend framework to implement the business logic and expose the REST API.
2. HTML, CSS (it`s being considered to use TailwindCSS), JavaScript and Angular to implement the frontend, ensuring a responsive interface.
3. ng2-charts library to display the statistical data (charts).
4. Data storage and management using a MySQL relational database.
5. Postman for designing and validating REST API endpoints.
6. Responsive design to ensure compatibility with mobile devices, tablets and computers.
7. Version control with Git.
8. Docker for application containerization as part of the Continuous Integration (CI), Continuous Delivery (CD), and Continuous Deployment process.
9. Mockito and AssertJ for backend unit and integration testing.
10. Karma and Jasmine for frontend unit and integration testing.
11. REST Assured for backend system/end-to-end (e2e) testing.
12. Cypress for frontend system/end-to-end (e2e) testing.

---
[👈 Return to README](../README.md)
