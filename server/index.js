require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');

const app = express();
const PORT = process.env.PORT || 3000;

mongoose.connect(process.env.MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
})
.then(() => console.log('MongoDB connected'))
.catch(err => console.error('MongoDB connection error:', err));

// Debugging for Mongoose
mongoose.set('debug', true);

// Middleware
app.use(express.json());

// Define a simple schema and model for player stats (adjust as per your actual data structure)
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

const PlayerStat = mongoose.model('PlayerStat', playerStatSchema, 'players');

// Define a simple schema and model for medals (adjust as per your actual data structure)
const medalSchema = new mongoose.Schema({
  name: String,
  description: String,
  level: String,
  // Add other medal properties as needed
});

const Medal = mongoose.model('Medal', medalSchema, 'medals');

// API Endpoints

// Get all player stats
app.get('/api/stats', async (req, res) => {
  try {
    const stats = await PlayerStat.find();
    res.json(stats);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Get stats for a specific player
app.get('/api/stats/:playerName', async (req, res) => {
  try {
    const stat = await PlayerStat.findOne({ name: req.params.playerName });
    if (stat == null) {
      return res.status(404).json({ message: 'Cannot find player stat' });
    }
    res.json(stat);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
});

// Get all medals
app.get('/api/medals', async (req, res) => {
  try {
    const medals = await Medal.find();
    res.json(medals);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Get medals for a specific player (assuming medals are embedded in player stats or linked by player ID)
app.get('/api/medals/:playerName', async (req, res) => {
  try {
    const player = await PlayerStat.findOne({ name: req.params.playerName });
    if (player == null) {
      return res.status(404).json({ message: 'Cannot find player' });
    }
    res.json(player.medals);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
});

// Link Discord account to Minecraft player
app.post('/api/linkDiscord', async (req, res) => {
  try {
    const { playerName, discordId, discordUsername } = req.body;
    if (!playerName || !discordId || !discordUsername) {
      return res.status(400).json({ message: 'Missing required fields: playerName, discordId, discordUsername' });
    }
    const player = await PlayerStat.findOne({ name: playerName });

    if (!player) {
      return res.status(404).json({ message: 'Player not found' });
    }

    player.link = `${discordUsername} (${discordId})`; // Assuming 'link' field stores Discord info
    await player.save();

    res.status(200).json({ message: 'Discord account linked successfully', player });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Unlink Discord account from Minecraft player
app.post('/api/unlinkDiscord', async (req, res) => {
  try {
    const { playerName } = req.body;
    if (!playerName) {
      return res.status(400).json({ message: 'Missing required field: playerName' });
    }
    const player = await PlayerStat.findOne({ name: playerName });

    if (!player) {
      return res.status(404).json({ message: 'Player not found' });
    }

    player.link = ''; // Clear the link field
    await player.save();

    res.status(200).json({ message: 'Discord account unlinked successfully', player });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Start the server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});

// Upload all player stats to database
app.post('/api/upload', async (req, res) => {
  try {
    // This endpoint would typically be called by the Minecraft plugin itself
    // For now, we'll just return a success message.
    res.status(200).json({ message: 'Player stats uploaded successfully (simulated)' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Upload all stats from all players
app.post('/api/uploadall', async (req, res) => {
  try {
    // This endpoint would typically be called by the Minecraft plugin itself
    // For now, we'll just return a success message.
    res.status(200).json({ message: 'All player stats uploaded successfully (simulated)' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Download all stats from a player in the cloud
app.get('/api/download/:playerName', async (req, res) => {
  try {
    const player = await PlayerStat.findOne({ name: req.params.playerName });
    if (player == null) {
      return res.status(404).json({ message: 'Cannot find player' });
    }
    res.json(player);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
});

// Merge two player documents from the database
app.post('/api/merge', async (req, res) => {
  try {
    const { player1Name, player2Name } = req.body;
    if (!player1Name || !player2Name) {
      return res.status(400).json({ message: 'Missing required fields: player1Name, player2Name' });
    }

    const player1 = await PlayerStat.findOne({ name: player1Name });
    const player2 = await PlayerStat.findOne({ name: player2Name });

    if (!player1) {
      return res.status(404).json({ message: `Player ${player1Name} not found` });
    }
    if (!player2) {
      return res.status(404).json({ message: `Player ${player2Name} not found` });
    }

    // In a real scenario, you would implement the merging logic here.
    // For now, we'll just return a success message.
    res.status(200).json({ message: `Players ${player1Name} and ${player2Name} merged successfully (simulated)` });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Give a Medal to a player
app.post('/api/givemedal', async (req, res) => {
  try {
    const { playerName, medalName } = req.body;
    if (!playerName || !medalName) {
      return res.status(400).json({ message: 'Missing required fields: playerName, medalName' });
    }

    const player = await PlayerStat.findOne({ name: playerName });
    if (!player) {
      return res.status(404).json({ message: 'Player not found' });
    }

    const medal = await Medal.findOne({ name: medalName });
    if (!medal) {
      return res.status(404).json({ message: 'Medal not found' });
    }

    // Add medal to player's medals array (if not already present)
    if (!player.medals.some(m => m.name === medalName)) {
      player.medals.push(medal);
      await player.save();
    }

    res.status(200).json({ message: `Medal ${medalName} given to ${playerName} successfully` });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Updates all documents from database
app.post('/api/updateall', async (req, res) => {
  try {
    // This endpoint would typically trigger a full database update process.
    // For now, we'll just return a success message.
    res.status(200).json({ message: 'All documents updated successfully (simulated)' });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Gives you some information about a medal
app.get('/api/medal/:medalName', async (req, res) => {
  try {
    const medal = await Medal.findOne({ name: req.params.medalName });
    if (medal == null) {
      return res.status(404).json({ message: 'Cannot find medal' });
    }
    res.json(medal);
  } catch (err) {
    return res.status(500).json({ message: err.message });
  }
});

// Reset a player's stats
app.post('/api/reset/:playerName', async (req, res) => {
  try {
    const player = await PlayerStat.findOne({ name: req.params.playerName });
    if (player == null) {
      return res.status(404).json({ message: 'Cannot find player' });
    }

    // Resetting specific stats to 0 or default values
    player.blcksDestroyed = 0;
    player.blcksPlaced = 0;
    player.blockMined = 0;
    player.kills = 0;
    player.mobKills = 0;
    player.mTravelled = 0;
    player.deaths = 0;
    player.redstoneUsed = 0;
    player.fishCaught = 0;
    player.enderdragonKills = 0;
    player.witherKills = 0;
    player.medals = []; // Clear medals
    player.mobsKilled = []; // Clear mob kills
    player.blocks = []; // Clear blocks

    await player.save();
    res.status(200).json({ message: `Player ${req.params.playerName} stats reset successfully` });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});