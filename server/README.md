# MineStats API Server

This is an Express.js server that provides a RESTful API for the MineStats Minecraft plugin. It interacts with a MongoDB database to retrieve and manage player statistics, medals, and Discord account linking.

## Setup

1.  **Prerequisites:**
    *   Node.js (LTS version recommended)
    *   MongoDB instance running (default connection string assumes `mongodb://localhost:27017/MineStats`)

2.  **Installation:**

    Navigate to the `server` directory:
    ```bash
    cd server
    ```

    Install the dependencies:
    ```bash
    npm install
    ```

3.  **Running the Server:**

    To start the server, run:
    ```bash
    node index.js
    ```

    The server will run on port `3000` by default (or the port specified in the `PORT` environment variable).

## API Endpoints

### Player Statistics

*   **GET /api/stats**
    *   Returns all player statistics.

*   **GET /api/stats/:playerName**
    *   Returns statistics for a specific player.

### Medals

*   **GET /api/medals**
    *   Returns all available medals.

*   **GET /api/medals/:playerName**
    *   Returns all medals for a specific player.

### Discord Linking

*   **POST /api/linkDiscord**
    *   Links a Discord account to a Minecraft player.
    *   **Request Body:**
        ```json
        {
            "playerName": "<minecraft_player_name>",
            "discordId": "<discord_user_id>",
            "discordUsername": "<discord_username>"
        }
        ```

*   **POST /api/unlinkDiscord**
    *   Unlinks a Discord account from a Minecraft player.
    *   **Request Body:**
        ```json
        {
            "playerName": "<minecraft_player_name>"
        }
        ```

## MongoDB Schema

### PlayerStat Schema

```javascript
const playerStatSchema = new mongoose.Schema({
  playerId: String,
  name: String,
  blcksDestroyed: Number,
  blcksPlaced: Number,
  blockMined: Number,
  kills: Number,
  mobKills: Number,
  mTravelled: Number,
  deaths: Number,
  medals: Array,
  redstoneUsed: Number,
  fishCaught: Number,
  enderdragonKills: Number,
  witherKills: Number,
  versionPlayed: Array,
  timeslogin: Number,
  lastLogin: String,
  playerSince: String,
  timePlayed: String,
  online: Boolean,
  mobsKilled: Array,
  blocks: Array,
  link: String,
});
```

### Medal Schema

```javascript
const medalSchema = new mongoose.Schema({
  name: String,
  description: String,
  level: String,
});
```

### Player Statistics Endpoints

- `GET /api/stats` - Get all player statistics.
- `GET /api/stats/:playerName` - Get statistics for a specific player.
- `POST /api/upload` - Upload all player stats to the database (simulated).
- `POST /api/uploadall` - Upload all stats from all players to the database (simulated).
- `GET /api/download/:playerName` - Download all stats from a player in the cloud.
- `POST /api/merge` - Merge two player documents from the database (simulated).
- `POST /api/reset/:playerName` - Reset a player's stats to default values.