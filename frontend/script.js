const AUTH_STORAGE_KEY = "studentBridgeAuth";

const App = {
	  init() {
	    this.navbar = document.querySelector(".navbar");
    this.navToggle = document.querySelector("[data-nav-toggle]");
    this.primaryNav = document.querySelector("[data-primary-nav]");
    this.guestActions = document.querySelector("[data-guest-actions]");
    this.userMenu = document.querySelector("[data-user-menu]");
    this.userMenuTrigger = document.querySelector("[data-user-menu-trigger]");
    this.userDropdown = document.querySelector("[data-user-dropdown]");
    this.userLabel = document.querySelector("[data-user-label]");
    this.userNameElements = document.querySelectorAll("[data-user-name]");
    this.userMailLink = document.querySelector("[data-user-mail]");
    this.employerLinks = document.querySelectorAll("[data-employer-link]");
    this.studentLinks = document.querySelectorAll("[data-student-link]");

    this.logoutButton = document.querySelector("[data-logout-button]");
    this.languageButton = document.getElementById("languageButton");
    this.languageMenu = document.getElementById("languageMenu");
    this.languageWrapper = document.querySelector(".language-wrapper");

	    const redirectParams = new URLSearchParams(window.location.search);
	    this.hasRecentLoginRedirect = redirectParams.get("login") === "success";

	    this.setAuthState("checking");
	    this.cleanAuthRedirectParams();
	    this.loadSessionAuthState();
	    this.initAnimations();
	    this.bindEvents();
	  },

  bindEvents() {
    window.addEventListener("scroll", () => this.handleScroll());
    window.addEventListener("resize", () => this.closeMobileNav());

    if (this.navToggle) {
      this.navToggle.addEventListener("click", () => this.toggleMobileNav());
    }

    if (this.primaryNav) {
      this.primaryNav.addEventListener("click", (event) => {
        if (event.target.closest("a")) {
          this.closeMobileNav();
        }
      });
    }

    if (this.userMenuTrigger) {
      this.userMenuTrigger.addEventListener("click", () => this.toggleUserMenu());
    }

    if (this.logoutButton) {
      this.logoutButton.addEventListener("click", () => this.logout());
    }

    if (this.languageButton && this.languageMenu) {
      this.languageButton.addEventListener("click", () => {
        this.languageMenu.classList.toggle("show");
      });
    }

    document.querySelectorAll("[data-lang]").forEach((item) => {
      item.addEventListener("click", () => {
        const selected = item.dataset.lang;

        if (window.StudentBridgeI18n) {
          StudentBridgeI18n.setLanguage(selected);
        } else {
          localStorage.setItem("studentBridgeLanguage", selected);
        }

        if (this.languageMenu) {
          this.languageMenu.classList.remove("show");
        }
      });
    });

    document.addEventListener("click", (event) => {
      if (this.navbar
          && this.navToggle
          && !this.navbar.contains(event.target)) {
        this.closeMobileNav();
      }

      if (this.userMenu && !this.userMenu.contains(event.target)) {
        this.closeUserMenu();
      }

      if (this.languageMenu
          && this.languageWrapper
          && !this.languageWrapper.contains(event.target)) {
        this.languageMenu.classList.remove("show");
      }
    });

    document.addEventListener("keydown", (event) => {
      if (event.key !== "Escape") {
        return;
      }

      this.closeMobileNav();
      this.closeUserMenu();

      if (this.languageMenu) {
        this.languageMenu.classList.remove("show");
      }
    });
  },

  cleanAuthRedirectParams() {
    const params = new URLSearchParams(window.location.search);

    if (!params.has("login") && !params.has("logout") && !params.has("registered")) {
      return;
    }

    params.delete("login");
    params.delete("logout");
    params.delete("registered");
    params.delete("email");
    params.delete("name");
    params.delete("accountType");

    const cleanQuery = params.toString();
    const cleanUrl = `${window.location.pathname}${cleanQuery ? `?${cleanQuery}` : ""}${window.location.hash}`;
    window.history.replaceState({}, document.title, cleanUrl);
  },

	  async loadSessionAuthState() {
	    localStorage.removeItem(AUTH_STORAGE_KEY);

    if (!window.StudentBridgePlatform || !StudentBridgePlatform.canReachBackend()) {
      this.renderLoggedOutState();
      return;
    }

    try {
      const response = await fetch(StudentBridgePlatform.toBackendUrl("AuthStatusServlet"), {
        credentials: "same-origin",
        cache: "no-store"
      });

      if (!response.ok) {
        throw new Error("Unable to read authentication session.");
      }

	      let auth = await response.json();

	      if ((!auth || !auth.loggedIn) && this.hasRecentLoginRedirect) {
	        await new Promise((resolve) => setTimeout(resolve, 500));
	        const retryResponse = await fetch(StudentBridgePlatform.toBackendUrl("AuthStatusServlet"), {
	          credentials: "same-origin",
	          cache: "no-store"
	        });

	        if (retryResponse.ok) {
	          auth = await retryResponse.json();
	        }
	      }

	      this.renderAuthState(auth);
	    } catch (error) {
	      console.error(error);
      this.renderLoggedOutState();
    }
  },

	  renderLoggedOutState() {
	    this.setAuthState("guest");

	    if (!this.guestActions || !this.userMenu) {
	      return;
	    }

    this.guestActions.hidden = false;
    this.guestActions.style.display = "";
    this.userMenu.hidden = true;
    this.userMenu.style.display = "none";
    this.closeUserMenu();

    if (this.userMailLink) {
      this.userMailLink.href = "#";
      this.userMailLink.hidden = true;
    }
  },

	  renderAuthState(auth) {
	    if (!auth || !auth.loggedIn) {
	      this.renderLoggedOutState();
	      return;
	    }

	    this.setAuthState("user");

    const accountType = String(auth.accountType || "").toLowerCase();
    const displayName = auth.name || "Account";

    this.guestActions.hidden = true;
    this.guestActions.style.display = "none";
    this.userMenu.hidden = false;
    this.userMenu.style.display = "";

    this.userNameElements.forEach((element) => {
      element.textContent = displayName;
    });

    if (this.userMenuTrigger) {
      this.userMenuTrigger.setAttribute("aria-label", `Profile menu for ${displayName}`);
      this.userMenuTrigger.title = "Profile menu";
    }

    if (this.userLabel) {
      this.userLabel.textContent = `Profile menu for ${displayName}`;
    }

    if (this.userMailLink) {
      this.userMailLink.hidden = !auth.email;
      this.userMailLink.href = auth.email ? `mailto:${auth.email}` : "#";
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

	  setAuthState(state) {
	    if (document.body) {
	      document.body.dataset.authState = state;
	    }
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

  toggleMobileNav() {
    if (!this.navbar || !this.navToggle) {
      return;
    }

    const shouldOpen = !this.navbar.classList.contains("nav-open");
    this.navbar.classList.toggle("nav-open", shouldOpen);
    this.navToggle.setAttribute("aria-expanded", String(shouldOpen));
    this.navToggle.setAttribute("aria-label", shouldOpen ? "Close menu" : "Open menu");
  },

  closeMobileNav() {
    if (!this.navbar || !this.navToggle) {
      return;
    }

    this.navbar.classList.remove("nav-open");
    this.navToggle.setAttribute("aria-expanded", "false");
    this.navToggle.setAttribute("aria-label", "Open menu");
  },

  logout() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    this.closeUserMenu();

    if (!window.StudentBridgePlatform || !StudentBridgePlatform.canReachBackend()) {
      this.renderLoggedOutState();
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
