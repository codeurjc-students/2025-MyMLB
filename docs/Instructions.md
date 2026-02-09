# ⚡ Quick Start

## 🧾 Table of Contents
- [Repository Cloning](#-repository-cloning)
- [Requirements](#-requirements)
- [Database](#-database)
- [Backend](#-backend)
- [Frontend](#-frontend)
- [Executable Generation](#-executable-generation)
- [Docker Deployment](#-docker-deployment)
- [Tools Usage](#-tools-usage)
- [Tests Execution](#-tests-execution)
- [Release Creation](#-release-creation)

## 📂 Repository Cloning
In order to clone this repository, you should use the following command:
```bash
git clone https://github.com/codeurjc-students/2025-MyMLB.git
````
And finally, locate yourself into the correct folder:
````bash
cd 2025-MLB
````
---
## 📋 Requirements
<table>
  <thead>
    <th>Software/Tool</th>
    <th>Version</th>
  </thead>
  <tbody>
    <tr>
      <td>Java</td>
      <td>21</td>
    </tr>
    <tr>
      <td>Maven</td>
      <td>3.9</td>
    </tr>
     <tr>
      <td>Spring Boot</td>
      <td>3.5</td>
    </tr>
    <tr>
      <td>GraalVM</td>
      <td>21</td>
    </tr>
    <tr>
      <td>Microsoft C/C++ Compiler</td>
      <td>19.44</td>
    </tr>
    <tr>
      <td>Node</td>
      <td>22</td>
    </tr>
    <tr>
      <td>npm</td>
      <td>10.9</td>
    </tr>
    <tr>
      <td>MySQL</td>
      <td>8.0</td>
    </tr>
    <tr>
      <td>Docker</td>
      <td>28</td>
    </tr>
    <tr>
      <td>Docker Conpose</td>
      <td>2.34-desktop.1</td>
    </tr>
  </tbody>
</table>

---
## 💾 Database
TBD

---
## 💻 Backend
First, navigate to the folder that contains the backend:
```bash
cd backend
````

Once there, you can run the backend of the application by using the following command:
```bash
mvn spring-boot:run
```

If everything goes as expected, you will be able to access it at: **https://localhost:8443**

---
## 🌐 Frontend
First, navigate to the folder that contains the frontend:
```bash
cd frontend
````

Install the dependencies:
```bash
npm install
````

Once the dependencies are installed, you can run the frontend with:
```bash
npm start
# or
ng serve
````

If everything goes as expected, you will be able to access it at: **http://localhost:4200**

---
## 📦 Executable Generation
To generate the application executable, run the following command in the `backend` folder:
```bash
mvn -Pnative native:compile
```

If you are using `Visual Studio Code`, you first need to fit the following requirements:
- Visual Studio Community 2022 (17.14).
- Microsoft C/C++ Compiler (19.44).

The compiler comes with the installation of Visual Studio Community 2022, which you can download [here](https://visualstudio.microsoft.com/es/vs/community/).

In case you already have it, you can check the version of the compiler with the following command:
```bash
cl
````

> [!IMPORTANT]
> To run the previous command, you will have to do it in the `x64 Native Tools Command Prompt for VS 2022` terminal.
> In case you do not have it, follow these steps:
> 1) Open the `Visual Studio Installer`.
> 2) Click `Modify` on the Visual Studio Community 2022 section.
> 3) Click on the `Individual Components` tab.
> 4) Search for `MSVC v143-VS 2022 C++ Build Tools for x64/x86 (latest)`.
> 5) Select it and click `Install`.

---

## 🐳 Docker Deployment
Docker was used to containerize the application by generating it's images to an easily deployment. These are the instructions you need to follow in order to run this image, aswell the requirements for it.

### Requirements for Windows and Mac OS
If you are a Windows or Mac user, you will need to have `Docker Desktop` installed in your system, in case you don't have it, you can consult the [official website](https://www.docker.com/products/docker-desktop/) for the installation process.

### Requirements for Linux OS
If you are a Linux user, you will need to have `Docker` and `Docker Compose` installed in your system, in case you don't have it, you can consult these official websites for the installation process:

- [Docker Installation](https://docs.docker.com/engine/install/ubuntu/)
- [Docker Compose Installation](https://docs.docker.com/compose/install/linux/)

### Running Instructions

> [!IMPORTANT]
> If you are a Windows or Mac user, to run any `docker` command you will need to have `Docker Desktop` open, as this allows the `Docker Daemon` to be operational.

To run and deploy the application, you will need to pull the `compose` published in `Docker Hub`. Additionally, you must have a`.env` file located in the [docker folder](https://github.com/codeurjc-students/2025-MyMLB/tree/main/docker). This file should follow the template below:

```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-container:3306/mlb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
MYSQL_ROOT_PASSWORD=root

CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

MAIL_USERNAME=mailusername@gmail.com
MAIL_PASSWORD=application_password
```

> [!NOTE]
> To obtain your Cloudinary credentials, you need to create an account in [Cloudinary](https://cloudinary.com/). Once your account is created, you cand find this information in your dashboard.

> [!IMPORTANT]
> For the email account, it is not an ordinary email account, it has to be an `application account`. The `MAIL_USERNAME` can be an ordinary one, but the password is the `application password` google generates when creating the account. To do this follow these steps:
> <br>
> 1. Go to [https://myaccount.google.com](https://myaccount.google.com)
> 2. Activate two-step verification if it is not already enabled.
> 3. Search in the search bar for `Application Password`.
> 4. Once there, fill in the requested information and if everything goes correctly the password will have been created successfully, copy it and paste it into your `.env` file.

Once you have created this file, follow these steps to launch the application container:

1) Pull the compose from the DockerHub repository:

```bash
docker compose pull
```

2) Launch the container using the compose:

```bash
docker compose up
```

---

## 🔧 Tools Usage

### Visual Studio Code
Is the main IDE used during the development of the application. Its usage its simple, the only requirements are having a `JDK` in your system (for this project is recommended JDK21) and install the `Java Extension Pack` and the `Spring Boot Extension Pack` extensions for Visual Studio Code.

### IntelliJ
This IDE was used as a secondary tool during the development process. As the backend of the project became more complex, Visual Studio Code started to present issues. To mitigate these problems and improve development efficiency, the backend of the application was developed in IntelliJ.

### Cloudinary
Is a cloud-based service for managing, storing, and delivering media assets. In this application, Cloudinary was used to host and manage the non-static images, ensuring reliable storage and optimized delivery.

### REST Client
REST Client is a Visual Studio Code extension that allows sending API requests to test it, within the IDE. This extension is used in this project, however, `Postman` is also a valid alternative. In order to use this extension, first you will need to install it in Visual Studio Code, and finally, send the API requests that are on every `.http` file. In this project, you can find these files by following this [link](../backend/src/main/java/com/mlb/mlbportal/requests).

### DBeaver
Is a universal SQL client and management tool. It allows you to manage, query, and visualize multiple relational databases. This tool was used to test the PostgreSQL database, performing queries directly on the database to test the persistence of entities and the state of various data.

---
## 🧪 Tests Execution

### Backend Tests

> [!IMPORTANT]
> Navigate to the `backend` folder.

```bash
mvn test
````

### Frontend Unit and Integration Tests

> [!IMPORTANT]
> Navigate to the `frontend` folder.

To run both unit and integration tests:

```bash
ng test
# or
npm run test
````

To only run the unit tests:

```bash
npm run test:unit
```

To only run the integration tests:

```bash
npm run test:integration
```

### Frontend System Tests with Cypress

> [!IMPORTANT]
> Navigate to the `frontend` folder.

To run the tests without UI feedback:

```bash
npx cypress run
```

To run the tests with UI feedback:

```bash
npx cypress open
```

---

## 🚀 Release Creation
You can create a release directly from GitHub, the only thing yo need to do is go to the repository's releases section, create a new release and publish it.

### 0.1 Version Release
**Date of Release:** December 19 of 2025

**Features Developed:** The features developed on this version were the basic ones, which you can find in the [User Stories Section](https://github.com/codeurjc-students/2025-MyMLB/blob/main/docs/UserStories.md).

### 0.2 Version Release
**Date of Release:** TBD

**Features Developed:** The features developed on this version were the intermediate ones, which you can find in the [User Stories Section](https://github.com/codeurjc-students/2025-MyMLB/blob/main/docs/UserStories.md).


### 1.0 Version Release
**Date of Release:** TBD

**Features Developed:** The features developed on this version were the advanced ones, which you can find in the [User Stories Section](https://github.com/codeurjc-students/2025-MyMLB/blob/main/docs/UserStories.md).

---
[👈 Return to README](../README.md)
