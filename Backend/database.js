const mysql = require("mysql2");

const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "YOUR_PASSWORD",
  database: "studentbridge"
});

db.connect(err => {
  if (err) {
    console.error("DB connection failed:", err);
  } else {
    console.log("MySQL Connected ✅");
  }
});

module.exports = db;