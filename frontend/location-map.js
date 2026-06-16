(function () {
  const DEFAULT_CENTER = {
    lat: 35.5384,
    lng: 129.3114
  };

  let kakaoLoaderPromise = null;

  const fallbackMessages = {
    "map.keyMissing": "Add your Kakao JavaScript key in frontend/map-config.js to enable the map.",
    "map.viewerKeyMissing": "Add your Kakao JavaScript key in frontend/map-config.js to enable job map previews.",
    "map.loadFailed": "Kakao map could not load. Check that this exact domain is registered in Kakao Developers.",
    "map.enterAddress": "Please enter a full address first.",
    "map.addressNotFound": "Address not found. Try a more specific Korean road address.",
    "map.locationSelected": "Location found. The address will be saved with this form.",
    "map.searching": "Searching for this address...",
    "map.openInKakao": "Open address in Kakao Map",
    "map.searchOrClick": "Enter an address, then use Search Location to show it on the map.",
    "map.selectJob": "Select a job with saved coordinates to preview it on the map.",
    "map.noCoordinates": "This job does not have saved map coordinates yet."
  };

  function t(key) {
    if (window.StudentBridgeI18n) {
      return StudentBridgeI18n.t(key);
    }

    return fallbackMessages[key] || key;
  }

  function setStatus(element, message, type = "info") {
    if (!element) {
      return;
    }

    delete element.dataset.mapStatusKey;
    element.textContent = message;
    element.className = `map-status ${type}`;
  }

  function setStatusKey(element, key, type = "info") {
    if (!element) {
      return;
    }

    element.dataset.mapStatusKey = key;
    element.textContent = t(key);
    element.className = `map-status ${type}`;
  }

  function buildKakaoSearchUrl(address) {
    const cleanAddress = String(address || "").trim();

    if (!cleanAddress) {
      return "";
    }

    return `https://map.kakao.com/link/search/${encodeURIComponent(cleanAddress)}`;
  }

  function ensureFallbackLink(statusElement, addressInput) {
    if (!statusElement || !addressInput) {
      return null;
    }

    const link = document.createElement("a");
    link.className = "map-fallback-link";
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.hidden = true;
    link.textContent = t("map.openInKakao");
    statusElement.insertAdjacentElement("afterend", link);

    function refresh() {
      const href = buildKakaoSearchUrl(addressInput.value);
      link.hidden = !href;
      link.href = href || "#";
    }

    refresh();

    return {
      element: link,
      refresh
    };
  }

  function getKakaoKey() {
    return typeof window.STUDENTBRIDGE_KAKAO_MAP_KEY === "string"
      ? window.STUDENTBRIDGE_KAKAO_MAP_KEY.trim()
      : "";
  }

  function getLoadFailureMessage(kind) {
    if (!getKakaoKey()) {
      return kind === "viewer"
        ? "map.viewerKeyMissing"
        : "map.keyMissing";
    }

    return "map.loadFailed";
  }

  function getLoadFailureText(kind) {
    const key = getLoadFailureMessage(kind);
    const baseMessage = t(key);

    if (key !== "map.loadFailed") {
      return baseMessage;
    }

    return `${baseMessage} Current domain: ${window.location.origin}`;
  }

  function loadKakaoMaps() {
    if (window.kakao && window.kakao.maps && window.kakao.maps.Map) {
      return Promise.resolve(window.kakao);
    }

    if (kakaoLoaderPromise) {
      return kakaoLoaderPromise;
    }

    const appKey = getKakaoKey();

    if (!appKey) {
      return Promise.reject(new Error("Kakao Maps JavaScript key is missing."));
    }

    kakaoLoaderPromise = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      const timeoutId = window.setTimeout(() => {
        kakaoLoaderPromise = null;
        reject(new Error("Kakao Maps request timed out."));
      }, 12000);

      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(appKey)}&libraries=services&autoload=false`;
      script.async = true;
      script.dataset.studentbridgeKakaoMap = "true";

      script.onload = () => {
        if (!window.kakao || !window.kakao.maps || typeof window.kakao.maps.load !== "function") {
          window.clearTimeout(timeoutId);
          kakaoLoaderPromise = null;
          reject(new Error("Kakao Maps SDK loaded but did not initialize."));
          return;
        }

        window.kakao.maps.load(() => {
          window.clearTimeout(timeoutId);
          resolve(window.kakao);
        });
      };

      script.onerror = () => {
        window.clearTimeout(timeoutId);
        kakaoLoaderPromise = null;
        reject(new Error("Kakao Maps SDK could not be loaded."));
      };

      document.head.appendChild(script);
    });

    return kakaoLoaderPromise;
  }

  function createLatLng(kakao, latitude, longitude) {
    return new kakao.maps.LatLng(Number(latitude), Number(longitude));
  }

  function hasCoordinates(latitude, longitude) {
    return latitude !== null
      && longitude !== null
      && latitude !== undefined
      && longitude !== undefined
      && String(latitude).trim() !== ""
      && String(longitude).trim() !== ""
      && !Number.isNaN(Number(latitude))
      && !Number.isNaN(Number(longitude));
  }

  async function initPicker(options) {
    const mapElement = document.getElementById(options.mapId);
    const addressInput = document.getElementById(options.addressInputId);
    const latitudeInput = document.getElementById(options.latitudeInputId);
    const longitudeInput = document.getElementById(options.longitudeInputId);
    const searchButton = document.getElementById(options.searchButtonId);
    const statusElement = document.getElementById(options.statusId);

    if (!mapElement || !addressInput || !latitudeInput || !longitudeInput || !searchButton) {
      return;
    }

    const fallbackLink = ensureFallbackLink(statusElement, addressInput);

    function refreshFallbackLink() {
      if (fallbackLink) {
        fallbackLink.refresh();
      }
    }

    function clearCoordinatesAfterAddressEdit() {
      latitudeInput.value = "";
      longitudeInput.value = "";
      refreshFallbackLink();
      setStatusKey(statusElement, "map.searchOrClick", "info");
    }

    let runAddressSearch = null;

    searchButton.addEventListener("click", () => {
      const address = addressInput.value.trim();
      refreshFallbackLink();

      if (!address) {
        setStatusKey(statusElement, "map.enterAddress", "error");
        addressInput.focus();
        return;
      }

      if (!runAddressSearch) {
        setStatus(statusElement, getLoadFailureText("picker"), "error");
        return;
      }

      runAddressSearch(address);
    });

    addressInput.addEventListener("input", clearCoordinatesAfterAddressEdit);
    addressInput.addEventListener("keydown", (event) => {
      if (event.key !== "Enter") {
        return;
      }

      event.preventDefault();
      searchButton.click();
    });

    try {
      const kakao = await loadKakaoMaps();
      const geocoder = new kakao.maps.services.Geocoder();
      const center = hasCoordinates(latitudeInput.value, longitudeInput.value)
        ? createLatLng(kakao, latitudeInput.value, longitudeInput.value)
        : createLatLng(kakao, options.defaultLat || DEFAULT_CENTER.lat, options.defaultLng || DEFAULT_CENTER.lng);

      const map = new kakao.maps.Map(mapElement, {
        center,
        level: options.level || 4
      });

      const marker = new kakao.maps.Marker({
        map,
        position: center
      });

      function updateCoordinates(latlng, message) {
        marker.setPosition(latlng);
        map.setCenter(latlng);
        latitudeInput.value = latlng.getLat().toFixed(7);
        longitudeInput.value = latlng.getLng().toFixed(7);
        refreshFallbackLink();
        setStatus(statusElement, message, "success");
      }

      runAddressSearch = (address) => {
        setStatusKey(statusElement, "map.searching", "info");

        geocoder.addressSearch(address, (result, status) => {
          if (status !== kakao.maps.services.Status.OK || result.length === 0) {
            latitudeInput.value = "";
            longitudeInput.value = "";
            refreshFallbackLink();
            setStatusKey(statusElement, "map.addressNotFound", "error");
            return;
          }

          const coordinates = new kakao.maps.LatLng(result[0].y, result[0].x);
          updateCoordinates(coordinates, t("map.locationSelected"));
          if (statusElement) {
            statusElement.dataset.mapStatusKey = "map.locationSelected";
          }
        });
      };

      setStatusKey(statusElement, "map.searchOrClick", "info");
    } catch (error) {
      console.warn("StudentBridge map setup failed:", error);
      refreshFallbackLink();
      setStatus(statusElement, getLoadFailureText("picker"), "error");
    }
  }

  async function initViewer(options) {
    const mapElement = document.getElementById(options.mapId);
    const statusElement = document.getElementById(options.statusId);

    if (!mapElement) {
      return null;
    }

    try {
      const kakao = await loadKakaoMaps();
      const center = createLatLng(kakao, options.defaultLat || DEFAULT_CENTER.lat, options.defaultLng || DEFAULT_CENTER.lng);
      const map = new kakao.maps.Map(mapElement, {
        center,
        level: options.level || 5
      });
      const marker = new kakao.maps.Marker({
        map,
        position: center
      });
      const infoWindow = new kakao.maps.InfoWindow({
        zIndex: 1
      });

      setStatusKey(statusElement, "map.selectJob", "info");

      return {
        showLocation(job) {
          if (!hasCoordinates(job.latitude, job.longitude)) {
            setStatusKey(statusElement, "map.noCoordinates", "error");
            return;
          }

          const position = createLatLng(kakao, job.latitude, job.longitude);
          marker.setPosition(position);
          map.setCenter(position);
          infoWindow.setContent(`<div style="padding:8px 10px;font-weight:700;">${escapeHtml(job.title)}</div>`);
          infoWindow.open(map, marker);
          const address = job.address || `${job.location} · ${job.company}`;
          setStatus(statusElement, address, "success");

          if (statusElement && job.address) {
            const link = document.createElement("a");
            link.className = "map-fallback-link";
            link.href = buildKakaoSearchUrl(job.address);
            link.target = "_blank";
            link.rel = "noopener noreferrer";
            link.textContent = t("map.openInKakao");
            statusElement.append(" ");
            statusElement.appendChild(link);
          }
        }
      };
    } catch (error) {
      console.warn("StudentBridge job map setup failed:", error);
      setStatus(statusElement, getLoadFailureText("viewer"), "error");
      return null;
    }
  }

  function escapeHtml(value) {
    return String(value || "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  window.StudentBridgeLocationMap = {
    initPicker,
    initViewer,
    hasCoordinates
  };

  window.addEventListener("studentbridge:languagechange", () => {
    document.querySelectorAll(".map-status[data-map-status-key]").forEach((element) => {
      element.textContent = t(element.dataset.mapStatusKey);
    });
  });
})();
