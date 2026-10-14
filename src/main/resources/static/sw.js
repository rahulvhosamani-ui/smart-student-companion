const CACHE_NAME = "smart-student-v1";

const urlsToCache = [
    "/",
    "/index.html",
    "/dashboard.html",
    "/notes.html",
    "/assignments.html",
    "/attendance.html",
    "/gpa.html",
    "/reminders.html",
    "/flashcards.html",
    "/chatbot.html",
    "/ainotes.html"
];

self.addEventListener("install", event => {

    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
    );

});

self.addEventListener("fetch", event => {

    event.respondWith(
        caches.match(event.request)
            .then(response => {
                return response || fetch(event.request);
            })
    );

});