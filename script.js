const themeBtn = document.getElementById("themeBtn");
const yearEl = document.getElementById("year");

// footer year
yearEl.textContent = new Date().getFullYear();

// theme toggle (saved)
const key = "qb-theme";
const saved = localStorage.getItem(key);

if (saved === "dark") {
  document.documentElement.setAttribute("data-theme", "dark");
  themeBtn.textContent = "☀️";
} else {
  themeBtn.textContent = "🌙";
}

themeBtn.addEventListener("click", () => {
  const isDark = document.documentElement.getAttribute("data-theme") === "dark";
  if (isDark) {
    document.documentElement.removeAttribute("data-theme");
    localStorage.setItem(key, "light");
    themeBtn.textContent = "🌙";
  } else {
    document.documentElement.setAttribute("data-theme", "dark");
    localStorage.setItem(key, "dark");
    themeBtn.textContent = "☀️";
  }
});
