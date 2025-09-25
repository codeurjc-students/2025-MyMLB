# ⚡ Quick Start

## 🧾 Table of Contents
- [Repository Cloning](#-repository-cloning)
- [Requirements](#-requirements)
- [Database](#-database)
- [Backend](#-backend)
- [Frontend](#-frontend)
- [Executable Generation](#-executable-generation)
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

If everything goes as expected, you will be able to access it at: **http://localhost:8080**

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
## 🔧 Tools Usage

### Visual Studio Code
Is the main IDE used during the development of the application. Its usage its simple, the only requirements are having a `JDK` in your system (for this project is recommended JDK21) and install the `Java Extension Pack` and the `Spring Boot Extension Pack` extensions for Visual Studio Code.

### REST Client
REST Client is a Visual Studio Code extension that allows sending API requests to test it, within the IDE. This extension is used in this project, however, `Postman` is also a valid alternative. In order to use this extension, first you will need to install it in Visual Studio Code, and finally, send the API requests that are on every `.http` file. In this project, you can find these files by following this [link](./backend/src/main/java/com/mlb/mlbportal/requests).

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
You can create a release directly from GitHub, the only thing yo need to do is go to the repository´s releases section, create a new release and publish it.

---
[👈 Return to README](../README.md)
