const CACHE_NAME = "smart-student-v3";

const staticFiles = [
    "/manifest.json",
    "/icon-192.png",
    "/icon-512.png"
];

// Install the new service worker
self.addEventListener("install", event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(staticFiles))
            .then(() => self.skipWaiting())
    );
});

// Remove old caches
self.addEventListener("activate", event => {
    event.waitUntil(
        caches.keys()
            .then(keys =>
                Promise.all(
                    keys
                        .filter(key => key !== CACHE_NAME)
                        .map(key => caches.delete(key))
                )
            )
            .then(() => self.clients.claim())
    );
});

// Always fetch pages and login requests from the server.
// Use the network for other GET requests, too.
self.addEventListener("fetch", event => {
    const request = event.request;
    const url = new URL(request.url);

    if (request.method !== "GET") return;
    if (url.origin !== self.location.origin) return;

    // Never serve cached HTML pages for login or navigation.
    if (
        request.mode === "navigate" ||
        url.pathname === "/" ||
        url.pathname === "/login" ||
        url.pathname === "/index.html"
    ) {
        event.respondWith(fetch(request));
        return;
    }

    // Network first; use cache only when the network is unavailable.
    event.respondWith(
        fetch(request).catch(() => caches.match(request))
    );
});