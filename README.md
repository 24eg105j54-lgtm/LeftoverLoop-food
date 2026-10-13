# LeftoverLoop — AI-Powered Food Donation & Redistribution Platform

LeftoverLoop is a complete, production-grade food redistribution system designed to connect food donors (restaurants, hotels, caterers, grocers), community receivers, charitable organizations/food banks, and administrators to eliminate hunger and reduce landfill waste.

---

## 🌟 Key Features

1. **Role-Based Architecture (4 Roles)**:
   - **DONOR**: Post surplus food batches, set expiry windows, manage incoming requests, approve/reject claims, and verify collection via secure collection code or QR code.
   - **RECEIVER**: Browse active donations, search/filter by category/location/urgency, request partial quantities, receive digital collection vouchers with QR codes, and track claims.
   - **ORGANIZATION**: Manage bulk food distribution batches, handle incoming receiver requests, track real-time stock levels, and monitor community impact metrics.
   - **ADMIN**: System-wide governance, user account toggling (activate/deactivate), role assignment, food listing audit, and safety monitoring.

2. **Smart Remaining Quantity Management**:
   - Concurrency-safe, transactional quantity calculations.
   - When a donation of 20 units has 10 units requested/reserved, other receivers immediately see the remaining 10 units as **AVAILABLE**.
   - Overbooking is strictly prevented at the database and service level.
   - Cancelled and rejected requests release their reserved quantities back to available stock.

3. **Genuine AI-Powered Matching & Recommendations**:
   - Evaluates Category affinity, Location proximity, Expiry urgency, and Quantity capacity.
   - Configurable for **Google Gemini 1.5 Flash API** or **OpenAI API** via cloud credentials.
   - Includes realistic multi-factor local AI matching model fallback when cloud API credentials are not configured, clearly distinguishing cloud vs. heuristic engine.

4. **Food Safety & Dynamic Color-Coded Indicators**:
   - **RED**: Expired or urgently approaching expiry (< 4 hours). Expired items automatically block new requests.
   - **ORANGE**: Approaching expiry (< 12 hours) or partially reserved listings requiring attention.
   - **GREEN**: Normal active listings with ample shelf life or successfully fulfilled distributions.

5. **Real Database-Backed Impact Analytics & CSV Export**:
   - Total food units donated and collected.
   - People served based on standardized food pantry metrics (`1 portion = ~2.5 meals`).
   - Landfill waste prevented (kg) and greenhouse gas CO₂e emissions averted.
   - One-click CSV export directly from database JPA aggregation.

---

## 🛠️ Technology Stack

- **Frontend**: HTML5, Vanilla CSS3 (modern glassmorphism design system), Vanilla JavaScript (ES6+), Fetch API.
- **Backend**: Java 17+, Spring Boot 3.2.5, Spring Security, JWT (jjwt 0.11.5), Spring Data JPA, Hibernate, Maven.
- **Database**: MySQL 8.0, Aiven Cloud MySQL (with TLS/SSL support), H2 (in-memory for automated tests).
- **AI Integration**: Google Gemini 1.5 Flash REST API / Spring WebClient.
- **Containerization**: Docker, Docker Compose, Multi-stage Dockerfile.

---

## 📁 Folder Structure

```
LeftoverLoop-food/
├── frontend/
│   ├── index.html              # Landing page with live statistics and feed
│   ├── login.html              # Login with demo shortcuts for all 4 roles
│   ├── register.html           # Registration for Donor, Receiver, Organization, Admin
│   ├── donor.html              # Donor dashboard and listing manager
│   ├── receiver.html           # Receiver portal with AI smart match
│   ├── organization.html       # Organization bulk distribution hub
│   ├── admin.html              # Admin governance & user audit panel
│   ├── requests.html           # Request tracking and code search
│   ├── food-details.html       # Item details with remaining quantity progress
│   ├── receipt.html            # Digital collection voucher & QR code
│   ├── impact.html             # Impact dashboard with real stats & CSV download
│   ├── css/
│   │   └── style.css           # Glassmorphism design system
│   ├── js/
│   │   ├── api.js              # Centralized REST API client
│   │   ├── auth.js             # Session & role state manager
│   │   ├── app.js              # Utilities, toast notifications, QR generator
│   │   ├── donor.js            # Donor logic
│   │   ├── receiver.js         # Receiver logic
│   │   ├── organization.js     # Organization logic
│   │   ├── admin.js            # Admin logic
│   │   └── login.js            # Login logic
│   └── package.json            # Starts frontend static dev server
├── backend/
│   ├── pom.xml                 # Maven build configuration
│   ├── package.json            # npm script to launch Maven
│   ├── Dockerfile              # Multi-stage container build
│   └── src/
│       ├── main/
│       │   ├── java/com/leftoverloop/
│       │   │   ├── LeftoverLoopApplication.java
│       │   │   ├── config/     # Security, JWT, CORS
│       │   │   ├── controller/ # MVC REST APIs
│       │   │   ├── dto/        # Data transfer objects
│       │   │   ├── entity/     # JPA entities & Enums
│       │   │   ├── exception/  # Global error handling
│       │   │   ├── repository/ # Spring Data repositories
│       │   │   └── service/    # Business logic & AI engine
│       │   └── resources/
│       │       └── application.properties
│       └── test/               # Automated unit & integration tests
├── database/
│   ├── schema.sql              # MySQL DDL table schema
│   └── sample-data.sql         # Seed data for demo testing
├── docker-compose.yml          # Container orchestration
├── .env.example                # Template environment variables
├── .gitignore
└── README.md
```

---

## 💻 Prerequisites & Software Installations

1. **Java Development Kit (JDK 17 or higher)**:
   - Check version: `java -version`
2. **Apache Maven (3.8+)**:
   - Check version: `mvn -version`
3. **Node.js (18+) & npm**:
   - Check version: `node -v` and `npm -v`
4. **MySQL (Optional if using Docker or Aiven Cloud MySQL)**:
   - MySQL 8.0 local server.

---

## 🚀 How to Run the Application

### 1. Launching the Backend

You can run the Spring Boot backend using any of the following methods:

#### Option A: Using npm (from `backend/` folder)
```bash
cd backend
npm run dev
```

#### Option B: Using Maven directly
```bash
cd backend
mvn spring-boot:run
```

#### Option C: Importing into Spring Tools for Eclipse (STS) or IntelliJ
1. Open STS or Eclipse.
2. Go to **File -> Import -> Existing Maven Projects**.
3. Browse to the `LeftoverLoop-food/backend` directory.
4. Select `pom.xml` and click **Finish**.
5. Right-click on `LeftoverLoopApplication.java` -> **Run As -> Spring Boot App**.

The backend will start at: `http://localhost:8080`

---

### 2. Launching the Frontend

In a separate terminal window:
```bash
cd frontend
npm run dev
```
The frontend static development server will start at: `http://localhost:3000`

---

### 3. Running with Docker Compose

To start the database, backend, and frontend simultaneously:
```bash
docker-compose up --build
```
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- MySQL: `localhost:3306`

---

## ☁️ Aiven Cloud MySQL Configuration

To connect LeftoverLoop to an **Aiven Cloud MySQL** managed database:

1. Create a MySQL service in the [Aiven Console](https://console.aiven.io/).
2. Obtain your Service URI, Host, Port, User, and Password.
3. Download or ensure SSL connection is required (`useSSL=true&requireSSL=true`).
4. Set the following environment variables (or create a `.env` file):

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://<aiven-host>:<aiven-port>/defaultdb?sslMode=REQUIRED&useSSL=true&requireSSL=true&verifyServerCertificate=false
SPRING_DATASOURCE_USERNAME=avnadmin
SPRING_DATASOURCE_PASSWORD=<your-aiven-password>
SPRING_JPA_DDL_AUTO=update
```

5. Run `database/schema.sql` and `database/sample-data.sql` in your Aiven MySQL console or workbench.

---

## 🤖 Configuring Real AI Credentials

LeftoverLoop includes genuine AI donation matching.

To connect **Google Gemini AI**:
1. Get a Gemini API Key from [Google AI Studio](https://aistudio.google.com/).
2. Set the environment variable:
   ```bash
   export AI_API_KEY="your-gemini-api-key"
   export AI_PROVIDER="gemini"
   ```
3. Restart the backend. When receivers request AI match recommendations, the application calls the Gemini 1.5 Flash REST API, provides real-time listings and receiver criteria, and returns genuine AI-ranked results.
4. If no key is set, the system gracefully operates on the built-in multi-factor heuristic model and explicitly states so in the UI.

---

## 🧪 Automated Testing

Automated test suites cover registration, auth, role authorization, donation creation, partial quantity requests, overbooking prevention, organization incoming requests, accept/reject flows, expiry restrictions, collection verification, and impact statistics.

Run the test suite from the `backend/` folder:
```bash
cd backend
mvn clean test
```

Build and compile without testing:
```bash
mvn clean compile
```

---

## 🔒 Default Demo Accounts

All sample users are pre-configured with password: `password123`

| Role | Email | Purpose |
|------|-------|---------|
| **DONOR** | `donor@grandplaza.com` | Hotel Caterer donating meals |
| **RECEIVER** | `receiver@shelter.org` | Community shelter claiming food |
| **ORGANIZATION** | `contact@foodrescuengo.org` | Food bank managing bulk distribution |
| **ADMIN** | `admin@leftoverloop.org` | System administrator & auditor |

---

## 🌐 API Endpoints Reference

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Register a new account
- `POST /api/auth/login` - Login and receive JWT
- `GET /api/auth/me` - Get authenticated profile

### Food Donations (`/api/donations`)
- `GET /api/donations/public/active` - List active unexpired donations
- `GET /api/donations/public/{id}` - Get donation details
- `POST /api/donations` - Create food donation
- `GET /api/donations/my-donations` - Current donor's listings
- `GET /api/donations/organization` - Organization bulk batches
- `PUT /api/donations/{id}` - Update food donation
- `DELETE /api/donations/{id}` - Cancel/delete food donation

### Requests & Distribution (`/api/requests`)
- `POST /api/requests` - Submit request for partial/full quantity
- `GET /api/requests/my-requests` - Receiver's claims and status
- `GET /api/requests/incoming` - Donor/Organization incoming requests
- `PATCH /api/requests/{id}/status` - Accept or reject request
- `POST /api/requests/verify` - Verify collection code and complete claim

### AI Recommendations (`/api/ai`)
- `POST /api/ai/recommendations` - Multi-factor AI donation matching

### Admin Governance (`/api/admin`)
- `GET /api/admin/users` - All registered accounts
- `PATCH /api/admin/users/{id}/toggle-status` - Activate/Deactivate user
- `PATCH /api/admin/users/{id}/role` - Update user role
- `GET /api/admin/donations` - Audit all listings

### Impact Statistics (`/api/impact`)
- `GET /api/impact/public/stats` - Live database impact metrics
- `GET /api/impact/export-csv` - Export CSV impact report
