(function () {
  const LANGUAGE_KEY = "studentBridgeLanguage";

  const dictionaries = {
    ko: {
      "Home": "홈",
      "Jobs": "채용",
      "Login": "로그인",
      "Register": "회원가입",
      "Dashboard": "대시보드",
      "Employer Dashboard": "고용주 대시보드",
      "Post Job": "채용 공고 등록",
      "Student Dashboard": "학생 대시보드",
      "Email": "이메일",
      "Logout": "로그아웃",
      "Built for students in Korea": "한국의 학생들을 위해 만든 서비스",
      "Build your career, one": "하나의 기회로 커리어를 시작하세요",
      "opportunity": "기회",
      "at a time": "",
      "Discover student-friendly jobs, filter by city and schedule, and connect with employers who understand international students.": "도시와 일정으로 학생 친화적인 일자리를 찾고, 유학생을 이해하는 고용주와 연결하세요.",
      "Explore Jobs": "채용 둘러보기",
      "Create Account": "계정 만들기",
      "A smarter way to find student work": "학생 일을 찾는 더 똑똑한 방법",
      "StudentBridge keeps the job search simple, clear, and focused on opportunities students can actually use.": "StudentBridge는 실제로 활용할 수 있는 기회에 집중해 구직 과정을 단순하고 명확하게 만듭니다.",
      "Easy Job Search": "쉬운 채용 검색",
      "International Student Friendly": "유학생 친화적",
      "Employer Connections": "고용주 연결",
      "How It Works": "이용 방법",
      "Create Profile": "프로필 만들기",
      "Search Jobs": "채용 검색",
      "Apply Online": "온라인 지원",
      "Get Hired": "채용되기",
      "Ready to find your next opportunity?": "다음 기회를 찾을 준비가 되었나요?",
      "Start with the job search, or create your account to prepare for applications.": "채용 검색부터 시작하거나 계정을 만들어 지원을 준비하세요.",
      "Get Started": "시작하기",
      "Browse Jobs": "채용 보기",
      "For Students": "학생",
      "Find Jobs": "일자리 찾기",
      "Save Address": "주소 저장",
      "For Employers": "고용주",
      "Register Employer Account": "고용주 계정 등록",
      "View Listings": "공고 보기",
      "Support": "지원",
      "Login Help": "로그인 도움말",
      "Registration Help": "회원가입 도움말",
      "Student-friendly job board": "학생 친화 채용 게시판",
      "Explore jobs that fit your student life": "학생 생활에 맞는 일자리 찾기",
      "Search by role, location, category, or schedule to find opportunities across Korea.": "역할, 위치, 카테고리, 일정으로 한국 전역의 기회를 찾아보세요.",
      "Job title": "직무명",
      "Location": "지역",
      "Category": "카테고리",
      "Job type": "근무 형태",
      "Search": "검색",
      "Reset": "초기화",
      "Only with map": "지도 있는 공고만",
      "Salary sort": "급여 정렬",
      "All Locations": "전체 지역",
      "All Categories": "전체 카테고리",
      "All Types": "전체 형태",
      "Job location map": "채용 위치 지도",
      "Welcome back": "다시 오신 것을 환영합니다",
      "Login to continue exploring student-friendly jobs.": "학생 친화적인 일자리를 계속 보려면 로그인하세요.",
      "Email address": "이메일 주소",
      "Password": "비밀번호",
      "Enter your email": "이메일을 입력하세요",
      "Enter your password": "비밀번호를 입력하세요",
      "Do not have an account?": "계정이 없나요?",
      "Join as a student or employer.": "학생 또는 고용주로 가입하세요.",
      "Student": "학생",
      "Employer": "고용주",
      "Full name": "이름",
      "Phone number": "전화번호",
      "University name": "대학교 이름",
      "Major": "전공",
      "Student ID": "학번",
      "Preferred job category": "선호 직무 카테고리",
      "Available working time": "근무 가능 시간",
      "Korean language level": "한국어 수준",
      "Confirm password": "비밀번호 확인",
      "Already have an account?": "이미 계정이 있나요?"
    },
    bn: {
      "Home": "হোম",
      "Jobs": "চাকরি",
      "Login": "লগইন",
      "Register": "রেজিস্টার",
      "Dashboard": "ড্যাশবোর্ড",
      "Employer Dashboard": "নিয়োগকর্তা ড্যাশবোর্ড",
      "Post Job": "চাকরি পোস্ট",
      "Student Dashboard": "স্টুডেন্ট ড্যাশবোর্ড",
      "Email": "ইমেইল",
      "Logout": "লগআউট",
      "Built for students in Korea": "কোরিয়ার শিক্ষার্থীদের জন্য তৈরি",
      "Build your career, one": "একটি সুযোগ দিয়ে ক্যারিয়ার শুরু করুন",
      "opportunity": "সুযোগ",
      "at a time": "",
      "Discover student-friendly jobs, filter by city and schedule, and connect with employers who understand international students.": "শহর ও সময়সূচি দিয়ে শিক্ষার্থী-বান্ধব চাকরি খুঁজুন এবং আন্তর্জাতিক শিক্ষার্থীদের বোঝেন এমন নিয়োগকর্তার সাথে যুক্ত হন।",
      "Explore Jobs": "চাকরি দেখুন",
      "Create Account": "অ্যাকাউন্ট তৈরি",
      "A smarter way to find student work": "শিক্ষার্থীদের কাজ খোঁজার সহজ উপায়",
      "StudentBridge keeps the job search simple, clear, and focused on opportunities students can actually use.": "StudentBridge চাকরি খোঁজাকে সহজ, পরিষ্কার এবং ব্যবহারযোগ্য সুযোগে কেন্দ্রীভূত রাখে।",
      "Easy Job Search": "সহজ চাকরি খোঁজা",
      "International Student Friendly": "আন্তর্জাতিক শিক্ষার্থী-বান্ধব",
      "Employer Connections": "নিয়োগকর্তার সংযোগ",
      "How It Works": "কীভাবে কাজ করে",
      "Create Profile": "প্রোফাইল তৈরি",
      "Search Jobs": "চাকরি খুঁজুন",
      "Apply Online": "অনলাইনে আবেদন",
      "Get Hired": "চাকরি পান",
      "Ready to find your next opportunity?": "আপনার পরের সুযোগ খুঁজতে প্রস্তুত?",
      "Start with the job search, or create your account to prepare for applications.": "চাকরি খোঁজা শুরু করুন বা আবেদন করার জন্য অ্যাকাউন্ট তৈরি করুন।",
      "Get Started": "শুরু করুন",
      "Browse Jobs": "চাকরি ব্রাউজ করুন",
      "For Students": "শিক্ষার্থীদের জন্য",
      "Find Jobs": "চাকরি খুঁজুন",
      "Save Address": "ঠিকানা সংরক্ষণ",
      "For Employers": "নিয়োগকর্তাদের জন্য",
      "Register Employer Account": "নিয়োগকর্তা অ্যাকাউন্ট",
      "View Listings": "লিস্টিং দেখুন",
      "Support": "সহায়তা",
      "Login Help": "লগইন সহায়তা",
      "Registration Help": "রেজিস্ট্রেশন সহায়তা",
      "Student-friendly job board": "শিক্ষার্থী-বান্ধব জব বোর্ড",
      "Explore jobs that fit your student life": "আপনার শিক্ষার্থী জীবনের সাথে মানানসই চাকরি খুঁজুন",
      "Search by role, location, category, or schedule to find opportunities across Korea.": "ভূমিকা, স্থান, ক্যাটাগরি বা সময়সূচি দিয়ে কোরিয়াজুড়ে সুযোগ খুঁজুন।",
      "Job title": "চাকরির নাম",
      "Location": "লোকেশন",
      "Category": "ক্যাটাগরি",
      "Job type": "চাকরির ধরন",
      "Search": "সার্চ",
      "Reset": "রিসেট",
      "Only with map": "শুধু ম্যাপসহ",
      "Salary sort": "বেতন সাজান",
      "All Locations": "সব লোকেশন",
      "All Categories": "সব ক্যাটাগরি",
      "All Types": "সব ধরন",
      "Job location map": "চাকরির লোকেশন ম্যাপ",
      "Welcome back": "স্বাগতম",
      "Login to continue exploring student-friendly jobs.": "শিক্ষার্থী-বান্ধব চাকরি দেখতে লগইন করুন।",
      "Email address": "ইমেইল ঠিকানা",
      "Password": "পাসওয়ার্ড",
      "Enter your email": "ইমেইল লিখুন",
      "Enter your password": "পাসওয়ার্ড লিখুন",
      "Do not have an account?": "অ্যাকাউন্ট নেই?",
      "Join as a student or employer.": "শিক্ষার্থী বা নিয়োগকর্তা হিসেবে যোগ দিন।",
      "Student": "শিক্ষার্থী",
      "Employer": "নিয়োগকর্তা",
      "Full name": "পূর্ণ নাম",
      "Phone number": "ফোন নম্বর",
      "University name": "বিশ্ববিদ্যালয়ের নাম",
      "Major": "বিষয়",
      "Student ID": "স্টুডেন্ট আইডি",
      "Preferred job category": "পছন্দের চাকরির ক্যাটাগরি",
      "Available working time": "কাজের সময়",
      "Korean language level": "কোরিয়ান ভাষার স্তর",
      "Confirm password": "পাসওয়ার্ড নিশ্চিত করুন",
      "Already have an account?": "ইতিমধ্যে অ্যাকাউন্ট আছে?"
    }
  };

  function getLanguage() {
    return localStorage.getItem(LANGUAGE_KEY) || "en";
  }

  function translateValue(value, dictionary) {
    const normalized = String(value || "").replace(/\s+/g, " ").trim();
    return dictionary[normalized] || null;
  }

  function applyLanguage(language) {
    const dictionary = dictionaries[language] || {};
    document.documentElement.lang = language;

    if (language === "en") {
      return;
    }

    document.querySelectorAll("input[placeholder], textarea[placeholder]").forEach((element) => {
      const translated = translateValue(element.getAttribute("placeholder"), dictionary);
      if (translated) {
        element.setAttribute("placeholder", translated);
      }
    });

    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    const nodes = [];

    while (walker.nextNode()) {
      nodes.push(walker.currentNode);
    }

    nodes.forEach((node) => {
      const parent = node.parentElement;

      if (!parent || ["SCRIPT", "STYLE", "TEXTAREA"].includes(parent.tagName)) {
        return;
      }

      const translated = translateValue(node.textContent, dictionary);

      if (translated) {
        node.textContent = node.textContent.replace(node.textContent.trim(), translated);
      }
    });
  }

  function setLanguage(language) {
    localStorage.setItem(LANGUAGE_KEY, language);
    window.location.reload();
  }

  function mountSwitcher() {
    if (document.getElementById("languageButton") || document.querySelector("[data-sb-language-switcher]")) {
      return;
    }

    const wrapper = document.createElement("label");
    wrapper.className = "sb-language-switcher";
    wrapper.dataset.sbLanguageSwitcher = "true";
    wrapper.innerHTML = `
      <span class="sr-only">Language</span>
      <select aria-label="Language">
        <option value="en">EN</option>
        <option value="ko">KO</option>
        <option value="bn">BN</option>
      </select>
    `;

    const style = document.createElement("style");
    style.textContent = `
      .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border: 0;
      }
      .sb-language-switcher select {
        min-height: 34px;
        border: 1px solid rgba(255,255,255,0.2);
        border-radius: 999px;
        color: inherit;
        background: rgba(255,255,255,0.08);
        padding: 0 10px;
        font-weight: 800;
      }
      .login-box .sb-language-switcher,
      .auth-card .sb-language-switcher {
        display: inline-flex;
        margin-bottom: 14px;
        color: #0f172a;
      }
    `;
    document.head.appendChild(style);

    const target = document.querySelector(".nav-links")
      || document.querySelector(".login-box")
      || document.querySelector(".auth-card")
      || document.body;

    target.appendChild(wrapper);

    const select = wrapper.querySelector("select");
    select.value = getLanguage();
    select.addEventListener("change", () => setLanguage(select.value));
  }

  document.addEventListener("DOMContentLoaded", () => {
    mountSwitcher();
    applyLanguage(getLanguage());
  });

  window.StudentBridgeI18n = {
    setLanguage,
    applyLanguage,
    getLanguage
  };
})();
