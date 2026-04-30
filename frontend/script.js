const App = {
  init() {
    this.navbar = document.querySelector(".navbar");
    this.initAnimations();
    this.bindEvents();
  },

  bindEvents() {
    window.addEventListener("scroll", () => this.handleScroll());
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
