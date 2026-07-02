document.addEventListener("DOMContentLoaded", () => {
    const root = document.documentElement;
    const body = document.body;
    const themeToggle = document.getElementById("theme-toggle");

    const applyTheme = (theme) => {
        const nextTheme = theme === "dark" ? "dark" : "light";
        root.setAttribute("data-okerry-theme", nextTheme);

        if (body) {
            body.setAttribute("data-okerry-theme", nextTheme);
        }

        if (!themeToggle) {
            return;
        }

        const isDark = nextTheme === "dark";
        themeToggle.textContent = isDark ? "라이트 모드" : "다크 모드";
        themeToggle.setAttribute("aria-label", isDark ? "라이트 모드로 전환" : "다크 모드로 전환");
        themeToggle.setAttribute("aria-pressed", String(isDark));
    };

    applyTheme("light");

    if (themeToggle) {
        themeToggle.addEventListener("click", () => {
            const currentTheme = root.getAttribute("data-okerry-theme");
            applyTheme(currentTheme === "dark" ? "light" : "dark");
        });
    }

});
