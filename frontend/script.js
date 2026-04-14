// ===============================
// StudentBridge - Main JS (Pro)
// ===============================

// Global App Object
const App = {
  state: {
    user: null,
    jobs: [],
  },

  init() {
    this.cacheDOM();
    this.bindEvents();
    this.initUI();
    this.loadData();
  },

  // ===============================
  // CACHE DOM ELEMENTS
  // ===============================
 cacheDOM() {
   this.navLinks = document.querySelectorAll(".nav-center a");
   this.searchBtn = document.getElementById("searchBtn");
   this.getStartedBtn = document.getElementById("getStartedBtn");
   this.browseJobsBtn = document.getElementById("browseJobsBtn");
  },

  // ===============================
  // EVENT LISTENERS
  // ===============================
  bindEvents() {
    this.navLinks.forEach(link =>
      link.addEventListener("click", this.handleNavClick.bind(this))
    );

    if (this.searchBtn)
      this.searchBtn.addEventListener("click", this.handleSearch.bind(this));

    if (this.getStartedBtn)
      this.getStartedBtn.addEventListener("click", this.handleRegister.bind(this));

    if (this.browseJobsBtn)
      this.browseJobsBtn.addEventListener("click", this.handleBrowseJobs.bind(this));

    window.addEventListener("scroll", this.handleScroll.bind(this));
  },

  // ===============================
  // INITIAL UI SETUP
  // ===============================
  initUI() {
    this.enableSmoothScroll();
    this.initAnimations();
  },

  // ===============================
  // DATA LOADING (API READY)
  // ===============================
  async loadData() {
    try {
      console.log("Loading data...");

      // Future API call
      // const res = await fetch('/api/jobs');
      // this.state.jobs = await res.json();

      this.state.jobs = this.getMockJobs();

      console.log("Jobs loaded:", this.state.jobs);
    } catch (err) {
      console.error("Error loading data:", err);
    }
  },

  // ===============================
  // NAVIGATION HANDLER
  // ===============================
  handleNavClick(e) {
    this.navLinks.forEach(l => l.classList.remove("active"));
    e.target.classList.add("active");
  },

  // ===============================
  // SEARCH HANDLER
  // ===============================
  handleSearch() {
    this.showLoader();

    setTimeout(() => {
      window.location.href = "/jobs.html"; // clean routing
    }, 800);
  },

  // ===============================
  // REGISTER HANDLER
  // ===============================
  handleRegister() {
    window.location.href = "/frontend/register.html";
  },

  // ===============================
  // BROWSE JOBS HANDLER
  // ===============================
  handleBrowseJobs() {
    window.location.href = "/jobs.html";
  },

  // ===============================
  // SCROLL EFFECT (Navbar Shadow)
  // ===============================
  handleScroll() {
    const navbar = document.querySelector(".navbar");

    if (window.scrollY > 50) {
      navbar.classList.add("scrolled");
    } else {
      navbar.classList.remove("scrolled");
    }
  },

  // ===============================
  // SMOOTH SCROLL
  // ===============================
  enableSmoothScroll() {
    document.querySelectorAll("a[href^='#']").forEach(anchor => {
      anchor.addEventListener("click", function (e) {
        const target = document.querySelector(this.getAttribute("href"));
        if (target) {
          e.preventDefault();
          target.scrollIntoView({ behavior: "smooth" });
        }
      });
    });
  },

  // ===============================
  // ANIMATIONS (Intersection Observer)
  // ===============================
  initAnimations() {
    const elements = document.querySelectorAll(".card, .step");

    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("show");
        }
      });
    }, { threshold: 0.2 });

    elements.forEach(el => observer.observe(el));
  },

  // ===============================
  // UI LOADER (Professional UX)
  // ===============================
  showLoader() {
    const loader = document.createElement("div");
    loader.className = "page-loader";
    loader.innerHTML = `<div class="spinner"></div>`;
    document.body.appendChild(loader);
  },

  // ===============================
  // MOCK DATA (TEMP)
  // ===============================
  getMockJobs() {
    return [
      { id: 1, title: "Cafe Assistant", location: "Ulsan", pay: "₩10,000/hr" },
      { id: 2, title: "English Tutor", location: "Busan", pay: "₩25,000/hr" },
      { id: 3, title: "Convenience Store Staff", location: "Seoul", pay: "₩9,620/hr" }
    ];
  }
};

// ===============================
// INIT APP
// ===============================
document.addEventListener("DOMContentLoaded", () => {
  App.init();
});