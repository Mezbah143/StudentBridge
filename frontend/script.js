const AUTH_STORAGE_KEY = "studentBridgeAuth";

const App = {
  init() {
    this.navbar = document.querySelector(".navbar");
    this.guestActions = document.querySelector("[data-guest-actions]");
    this.userMenu = document.querySelector("[data-user-menu]");
    this.userMenuTrigger = document.querySelector("[data-user-menu-trigger]");
    this.userDropdown = document.querySelector("[data-user-dropdown]");
    this.userEmailElements = document.querySelectorAll("[data-user-email]");
    this.userMailLink = document.querySelector("[data-user-mail]");
    this.dropdownEmail = document.querySelector("[data-dropdown-email]");
    this.employerLinks = document.querySelectorAll("[data-employer-link]");
    this.studentLinks = document.querySelectorAll("[data-student-link]");

    this.logoutButton = document.querySelector("[data-logout-button]");
    this.languageButton = document.getElementById("languageButton");
    this.languageMenu = document.getElementById("languageMenu");
    this.languageWrapper = document.querySelector(".language-wrapper");

    this.syncLoginFromRedirect();
    this.renderAuthState();
    this.initAnimations();
    this.bindEvents();
  },

  bindEvents() {
    window.addEventListener("scroll", () => this.handleScroll());

    if (this.userMenuTrigger) {
      this.userMenuTrigger.addEventListener("click", () => this.toggleUserMenu());
    }

    if (this.logoutButton) {
      this.logoutButton.addEventListener("click", () => this.logout());
    }
    if (this.languageButton) {

  this.languageButton
    .addEventListener("click", () => {

      this.languageMenu
        .classList.toggle("show");

    });
}

document.querySelectorAll("[data-lang]")
  .forEach((item) => {

    item.addEventListener("click", () => {

      const selected = item.dataset.lang;

      if (window.StudentBridgeI18n) {
        StudentBridgeI18n.setLanguage(selected);
      } else {
        localStorage.setItem("studentBridgeLanguage", selected);
      }

      this.languageMenu
        .classList.remove("show");

    });

});

    document.addEventListener("click", (event) => {
      if (!this.userMenu || this.userMenu.contains(event.target)) {
        return;
      }

      this.closeUserMenu();
      if (
  this.languageWrapper &&
  !this.languageWrapper.contains(event.target)
) {

  this.languageMenu
    .classList.remove("show");

}
    });
  },

  syncLoginFromRedirect() {
    const params = new URLSearchParams(window.location.search);

    if (params.get("login") !== "success") {
      return;
    }

    const email = params.get("email");
    const name = params.get("name") || "";
    const accountType = params.get("accountType") || "";

    if (email) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({
        email,
        name,
        accountType,
        loggedIn: true
      }));
    }

    params.delete("login");
    params.delete("registered");
    params.delete("email");
    params.delete("name");
    params.delete("accountType");

    const cleanQuery = params.toString();
    const cleanUrl = `${window.location.pathname}${cleanQuery ? `?${cleanQuery}` : ""}${window.location.hash}`;
    window.history.replaceState({}, document.title, cleanUrl);
  },

  getAuthState() {
    try {
      return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY)) || {};
    } catch (error) {
      return {};
    }
  },

  renderAuthState() {
    if (!this.guestActions || !this.userMenu) {
      return;
    }

    const auth = this.getAuthState();
    const isLoggedIn = auth.loggedIn && auth.email;
    const accountType = String(auth.accountType || "").toLowerCase();

    this.guestActions.hidden = Boolean(isLoggedIn);
    this.guestActions.style.display = isLoggedIn ? "none" : "";
    this.userMenu.hidden = !isLoggedIn;
    this.userMenu.style.display = isLoggedIn ? "" : "none";

    if (!isLoggedIn) {
      return;
    }

    this.userEmailElements.forEach((element) => {
      element.textContent = "";
    });

    if (this.userMenuTrigger) {
      this.userMenuTrigger.setAttribute("aria-label", `Profile menu for ${auth.email}`);
      this.userMenuTrigger.title = auth.email;
    }

    if (this.dropdownEmail) {
      this.dropdownEmail.textContent = auth.email;
    }

    if (this.userMailLink) {
      this.userMailLink.href = `mailto:${auth.email}`;
    }

    if (this.userDropdown) {
      this.userDropdown.hidden = true;
      this.userDropdown.style.display = "none";
    }

    this.employerLinks.forEach((link) => {
      link.hidden = accountType !== "employer";
    });

    this.studentLinks.forEach((link) => {
      link.hidden = accountType === "employer";
    });
  },

  toggleUserMenu() {
    if (!this.userDropdown || !this.userMenuTrigger) {
      return;
    }

    const shouldOpen = this.userDropdown.hidden;
    this.userDropdown.hidden = !shouldOpen;
    this.userDropdown.style.display = shouldOpen ? "block" : "none";
    this.userMenuTrigger.setAttribute("aria-expanded", String(shouldOpen));
  },

  closeUserMenu() {
    if (!this.userDropdown || !this.userMenuTrigger) {
      return;
    }

    this.userDropdown.hidden = true;
    this.userDropdown.style.display = "none";
    this.userMenuTrigger.setAttribute("aria-expanded", "false");
  },

  logout() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    this.closeUserMenu();

    if (!window.StudentBridgePlatform || !StudentBridgePlatform.canReachBackend()) {
      this.renderAuthState();
      return;
    }

    window.location.href = StudentBridgePlatform.toBackendUrl("LogoutServlet");
  },

  handleScroll() {
    if (!this.navbar) {
      return;
    }

    this.navbar.classList.toggle("scrolled", window.scrollY > 50);
  },

  initAnimations() {
    const elements = document.querySelectorAll(".card, .step");

    if (!("IntersectionObserver" in window)) {
      elements.forEach((element) => element.classList.add("show"));
      return;
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("show");
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.2 });

    elements.forEach((element) => observer.observe(element));
  }
};

document.addEventListener("DOMContentLoaded", () => {
  App.init();
});
