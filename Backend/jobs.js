const express = require("express");
const router = express.Router();
const db = require("../db");

// GET all jobs
router.get("/", (req, res) => {
  const sql = "SELECT * FROM jobs";

  db.query(sql, (err, result) => {
    if (err) {
      return res.status(500).json(err);
    }
    res.json(result);
  });
});

module.exports = router;