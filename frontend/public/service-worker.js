let user
let storage = {}

self.addEventListener('install', function(e) {
    e.waitUntil(
        caches.open('airhorner').then(function(cache) {
          return cache.addAll([
            '/',
            "https://indestructibletype.com/fonts/Jost.css"
          ]);
        })
      );
});

self.addEventListener('activate', function(event) {
    // Perform some task
});

self.addEventListener('fetch', function (event) {
  //  console.log(event.request.url);
  /*event.respondWith(
    caches.match(event.request).then(function(response) {
      return response || fetch(event.request);
    })
  );*/
});