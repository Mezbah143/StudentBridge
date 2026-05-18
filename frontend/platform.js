(function () {
  function trimTrailingSlash(value) {
    return String(value || "").replace(/\/+$/, "");
  }

  function isStaticPreview() {
    return window.location.protocol === "file:" || window.location.hostname.endsWith("github.io");
  }

  function getConfiguredBackendBase() {
    return trimTrailingSlash(window.STUDENTBRIDGE_BACKEND_BASE_URL || "");
  }

  function inferBackendBase() {
    const pathname = window.location.pathname || "";
    const frontendIndex = pathname.indexOf("/frontend/");

    if (frontendIndex >= 0) {
      return pathname.slice(0, frontendIndex);
    }

    const normalizedPath = pathname.endsWith("/") && pathname.length > 1
      ? pathname.slice(0, -1)
      : pathname;

    const lastSlash = normalizedPath.lastIndexOf("/");
    return lastSlash > 0 ? normalizedPath.slice(0, lastSlash) : "";
  }

  function getBackendBase() {
    const configuredBase = getConfiguredBackendBase();

    if (configuredBase) {
      return configuredBase;
    }

    if (isStaticPreview()) {
      return "";
    }

    return trimTrailingSlash(inferBackendBase());
  }

  function canReachBackend() {
    return !isStaticPreview() || getConfiguredBackendBase() !== "";
  }

  function toBackendUrl(path) {
    const cleanPath = String(path || "").replace(/^\/+/, "");
    const backendBase = getBackendBase();

    if (!backendBase) {
      return `/${cleanPath}`;
    }

    return `${backendBase}/${cleanPath}`;
  }

  window.StudentBridgePlatform = {
    isStaticPreview,
    canReachBackend,
    getBackendBase,
    toBackendUrl
  };
})();
