(function () {
  const DEFAULT_API_BASE_URL = "http://localhost:8080/api";

  const fromWindow =
    (window.__GOVSHIELD_CONFIG__ &&
      window.__GOVSHIELD_CONFIG__.API_BASE_URL) ||
    window.GOVSHIELD_API_BASE_URL;

  const fromMeta = (function () {
    const meta = document.querySelector('meta[name="govshield-api-base-url"]');
    return meta && meta.content ? meta.content : "";
  })();

  const fromLocalStorage = (function () {
    try {
      return localStorage.getItem("GOVSHIELD_API_BASE_URL") || "";
    } catch (e) {
      return "";
    }
  })();

  const raw = fromWindow || fromMeta || fromLocalStorage || DEFAULT_API_BASE_URL;
  const normalized = String(raw).replace(/\/+$/, "");

  window.GOVSHIELD = window.GOVSHIELD || {};
  window.GOVSHIELD.API_BASE_URL = normalized;
})();

