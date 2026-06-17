(function () {
  const state = {
    profile: null
  };

  const selectors = {
    message: "[data-profile-message]",
    role: "[data-profile-role]",
    name: "[data-profile-name]",
    subtitle: "[data-profile-subtitle]",
    email: "[data-profile-email]",
    phone: "[data-profile-phone]",
    address: "[data-profile-address]",
    statPrimary: "[data-stat-primary]",
    statPrimaryLabel: "[data-stat-primary-label]",
    statSecondary: "[data-stat-secondary]",
    statSecondaryLabel: "[data-stat-secondary-label]",
    statMessages: "[data-stat-messages]",
    documentList: "[data-document-list]",
    documentForm: "[data-document-form]",
    documentType: "[data-document-type]",
    documentFile: "[data-document-file]",
    actionEdit: "[data-action-edit]",
    actionPrimary: "[data-action-primary]",
    actionSecondary: "[data-action-secondary]"
  };

  function element(selector) {
    return document.querySelector(selector);
  }

  function backendUrl(path) {
    if (window.StudentBridgePlatform && StudentBridgePlatform.canReachBackend()) {
      return StudentBridgePlatform.toBackendUrl(path);
    }

    return `../${path}`;
  }

  async function getSessionAuth() {
    if (!window.StudentBridgePlatform || !StudentBridgePlatform.canReachBackend()) {
      return { loggedIn: false };
    }

    const response = await fetch(backendUrl("AuthStatusServlet"), {
      credentials: "same-origin",
      cache: "no-store"
    });

    if (!response.ok) {
      return { loggedIn: false };
    }

    return response.json();
  }

  function showMessage(text, type) {
    const message = element(selectors.message);

    if (!message) {
      return;
    }

    message.textContent = text;
    message.className = `profile-message show ${type}`;
  }

  function setText(selector, value) {
    const target = element(selector);

    if (target) {
      target.textContent = value || "-";
    }
  }

  function formatBytes(bytes) {
    const size = Number(bytes || 0);

    if (size < 1024) {
      return `${size} B`;
    }

    if (size < 1024 * 1024) {
      return `${Math.round(size / 1024)} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  function documentLabel(type) {
    const labels = {
      resume: "Resume.pdf",
      arc: "ARC Card",
      certificates: "Certificates",
      "business-registration": "Business Registration",
      "company-certificate": "Company Certificate"
    };

    return labels[type] || "Document";
  }

  function configureDocumentTypes(accountType) {
    const select = element(selectors.documentType);

    if (!select) {
      return;
    }

    const types = accountType === "Employer"
      ? [
        ["business-registration", "Business Registration"],
        ["arc", "ARC / ID Card"],
        ["company-certificate", "Company Certificate"]
      ]
      : [
        ["resume", "Resume.pdf"],
        ["arc", "ARC Card"],
        ["certificates", "Certificates"]
      ];

    select.innerHTML = types
      .map(([value, label]) => `<option value="${value}">${label}</option>`)
      .join("");
  }

  function renderDocuments(documents) {
    const list = element(selectors.documentList);

    if (!list) {
      return;
    }

    if (!documents || documents.length === 0) {
      list.innerHTML = '<p class="document-empty">No documents uploaded yet.</p>';
      return;
    }

    list.innerHTML = documents.map((doc) => {
      const downloadHref = backendUrl(`ProfileDocumentServlet?id=${encodeURIComponent(doc.id)}`);
      return `
        <div class="document-item">
          <div>
            <strong>${documentLabel(doc.documentType)}</strong>
            <span>${escapeHtml(doc.fileName)} · ${formatBytes(doc.fileSize)}</span>
          </div>
          <a href="${downloadHref}">
            <i class="fa-solid fa-download" aria-hidden="true"></i>
            <span class="sr-only">Download ${escapeHtml(doc.fileName)}</span>
          </a>
        </div>
      `;
    }).join("");
  }

  function configureActions(profile) {
    const isEmployer = profile.accountType === "Employer";
    const edit = element(selectors.actionEdit);
    const primary = element(selectors.actionPrimary);
    const secondary = element(selectors.actionSecondary);

    setText(selectors.statPrimaryLabel, isEmployer ? "Posted Jobs" : "Applied Jobs");
    setText(selectors.statSecondaryLabel, isEmployer ? "Applicants" : "Saved Jobs");

    if (edit) {
      edit.href = isEmployer ? "./employer-dashboard.html" : "./student-profile.html";
      edit.querySelector("span").textContent = isEmployer ? "Employer Dashboard" : "Edit Profile";
    }

    if (primary) {
      primary.href = isEmployer ? "./employer-dashboard.html" : "./jobsearch.html";
      primary.querySelector("span").textContent = isEmployer ? "My Jobs & Applicants" : "My Applications";
    }

    if (secondary) {
      secondary.href = isEmployer ? "./post-job.html" : "./jobsearch.html";
      secondary.querySelector("span").textContent = isEmployer ? "Post Job" : "Saved Jobs";
    }
  }

  function renderProfile(profile) {
    state.profile = profile;
    const roleLabel = profile.accountType === "Employer" ? "Employer Dashboard" : "Student Dashboard";

    setText(selectors.role, roleLabel);
    setText(selectors.name, profile.name);
    setText(selectors.subtitle, profile.subtitle);
    setText(selectors.email, profile.email);
    setText(selectors.phone, profile.phone || "Not added yet");
    setText(selectors.address, profile.address || "Not added yet");
    setText(selectors.statPrimary, profile.stats ? profile.stats.primary : 0);
    setText(selectors.statSecondary, profile.stats ? profile.stats.secondary : 0);
    setText(selectors.statMessages, profile.stats ? profile.stats.messages : 0);

    configureActions(profile);
    configureDocumentTypes(profile.accountType);
    renderDocuments(profile.documents || []);
  }

  async function loadProfile() {
    try {
      const auth = await getSessionAuth();

      if (!auth.loggedIn) {
        window.location.href = "./login.html?error=loginRequired";
        return;
      }

      const response = await fetch(backendUrl("ProfileServlet"), {
        credentials: "same-origin",
        cache: "no-store"
      });

      if (response.status === 401) {
        window.location.href = "./login.html?error=loginRequired";
        return;
      }

      if (!response.ok) {
        throw new Error("Profile could not be loaded.");
      }

      renderProfile(await response.json());
    } catch (error) {
      console.error(error);
      showMessage("Profile could not be loaded. Please refresh and try again.", "error");
    }
  }

  async function uploadDocument(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const fileInput = element(selectors.documentFile);

    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
      showMessage("Please choose a document first.", "error");
      return;
    }

    if (fileInput.files[0].size > 5 * 1024 * 1024) {
      showMessage("Please choose a file smaller than 5 MB.", "error");
      return;
    }

    const button = form.querySelector("button");
    const previousText = button ? button.innerHTML : "";

    if (button) {
      button.disabled = true;
      button.innerHTML = '<i class="fa-solid fa-spinner fa-spin" aria-hidden="true"></i><span>Uploading...</span>';
    }

    try {
      const response = await fetch(backendUrl("ProfileDocumentServlet"), {
        method: "POST",
        credentials: "same-origin",
        body: new FormData(form)
      });

      if (!response.ok) {
        throw new Error("Upload failed.");
      }

      form.reset();
      showMessage("Document uploaded successfully.", "success");
      await loadProfile();
    } catch (error) {
      console.error(error);
      showMessage("Document upload failed. Please try a smaller file or different document.", "error");
    } finally {
      if (button) {
        button.disabled = false;
        button.innerHTML = previousText;
      }
    }
  }

  function escapeHtml(value) {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  document.addEventListener("DOMContentLoaded", () => {
    const form = element(selectors.documentForm);

    configureDocumentTypes("Student");

    if (form) {
      form.addEventListener("submit", uploadDocument);
    }

    loadProfile();
  });
})();
