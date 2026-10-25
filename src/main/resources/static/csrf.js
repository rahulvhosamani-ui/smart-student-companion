(function () {
    "use strict";

    const nativeFetch = window.fetch.bind(window);
    const safeMethods = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);
    let tokenPromise;

    async function getCsrfToken() {
        if (!tokenPromise) {
            tokenPromise = nativeFetch("/csrf", {
                method: "GET",
                credentials: "same-origin",
                headers: { "Accept": "application/json" }
            }).then(async response => {
                if (!response.ok) {
                    throw new Error(
                        "Could not get a security token (HTTP " +
                        response.status + "). Please reload and sign in again."
                    );
                }
                return response.json();
            }).catch(error => {
                tokenPromise = null;
                throw error;
            });
        }
        return tokenPromise;
    }

    window.fetch = async function (input, init = {}) {
        const request = input instanceof Request ? input : null;
        const method = String(
            init.method || (request && request.method) || "GET"
        ).toUpperCase();
        const url = new URL(
            typeof input === "string" ? input : input.url,
            window.location.href
        );

        if (!safeMethods.has(method) && url.origin === window.location.origin) {
            const csrf = await getCsrfToken();
            const headers = new Headers(
                init.headers || (request && request.headers) || {}
            );
            headers.set(csrf.headerName || "X-XSRF-TOKEN", csrf.token);
            init = {
                ...init,
                credentials: init.credentials || "same-origin",
                headers
            };
        }

        return nativeFetch(input, init);
    };

    document.addEventListener("click", async function (event) {
        const target = event.target instanceof Element
            ? event.target
            : event.target.parentElement;
        const link = target && target.closest("a[data-logout]");
        if (!link) return;

        event.preventDefault();
        link.setAttribute("aria-disabled", "true");
        try {
            const response = await window.fetch(link.href, {
                method: "POST",
                credentials: "same-origin"
            });
            if (!response.ok) {
                throw new Error("Logout failed. Please try again.");
            }
            window.location.assign("/login?logout=true");
        } catch (error) {
            link.removeAttribute("aria-disabled");
            window.alert(error.message || "Logout failed. Please try again.");
        }
    });
})();
