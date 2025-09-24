# ⚡ Quick Start

## 🧾 Table of Contents
- [Repository Cloning](#-repository-cloning)
- [Requirements](#-requirements)
- [Database](#-database)
- [Backend](#-backend)
- [Frontend](#-frontend)
- [Binary Generation](#-binary-generation)

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
## 🗄️ Database
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
## 🛠️ Binary Generation
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
[👈 Return to README](../README.md)
