const express = require("express");
const router = express.Router();
const db = require("../db");

// Register user
router.post("/register", (req, res) => {
  const { name, email, password } = req.body;

  const sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

  db.query(sql, [name, email, password], (err, result) => {
    if (err) {
      return res.status(500).json(err);
    }

    res.json({ message: "User registered successfully" });
  });
});

module.exports = router;