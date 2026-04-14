const express = require("express");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(express.json());

// Routes
const jobRoutes = require("./routes/jobs");
const userRoutes = require("./routes/users");

app.use("/api/jobs", jobRoutes);
app.use("/api/users", userRoutes);

const PORT = 5000;

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});