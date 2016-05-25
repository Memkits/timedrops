
console.log('loaded sw.js');

var CACHE_NAME = 'v1';
var urlsToCache = [
  'index.html',
  'main.8802f905.js',
  'drop-192x192.png'
];

self.addEventListener('install', function(event) {
  // Perform install steps
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
});
