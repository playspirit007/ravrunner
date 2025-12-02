let map;
let routeLayer;

function getWaypointsFromPage() {
    const waypointElements = document.querySelectorAll('.waypoint-card');
    const waypoints = [];

    waypointElements.forEach((element, index) => {
        const titleElement = element.querySelector('h6');
        const latElement = element.querySelector('span:nth-child(3)');
        const lngElement = element.querySelector('span:nth-child(5)');

        if (titleElement && latElement && lngElement) {
            const lat = parseFloat(latElement.textContent.replace(',', '.'));
            const lng = parseFloat(lngElement.textContent.replace(',', '.'));
            const name = titleElement.textContent;

            if (!isNaN(lat) && !isNaN(lng)) {
                waypoints.push({
                    lat: lat,
                    lng: lng,
                    name: name
                });
            }
        }
    });

    return waypoints;
}

function initMap() {
    const waypoints = getWaypointsFromPage();
    console.log("Gefundene Wegpunkte:", waypoints);

    if (waypoints.length === 0) {
        const loading = document.getElementById('mapLoading');
        if (loading) {
            loading.innerHTML = '<p>❌ Keine gültigen Wegpunkte gefunden</p>';
        }
        return;
    }

    map = L.map('map').setView([waypoints[0].lat, waypoints[0].lng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 18
    }).addTo(map);

    waypoints.forEach((waypoint, index) => {
        L.marker([waypoint.lat, waypoint.lng])
            .addTo(map)
            .bindPopup(
                `<div style="text-align: center;">
                    <strong>${waypoint.name}</strong><br>
                    <small>Punkt ${index + 1}</small><br>
                    <small>${waypoint.lat.toFixed(6)}, ${waypoint.lng.toFixed(6)}</small>
                </div>`
            );
    });

    if (waypoints.length >= 2) {
        calculateBikeRoute(waypoints);
    } else {
        const loading = document.getElementById('mapLoading');
        if (loading) {
            loading.style.display = 'none';
        }
        if (waypoints.length === 1) {
            map.setView([waypoints[0].lat, waypoints[0].lng], 15);
            document.getElementById('totalDistance').textContent = '0 km';
            document.getElementById('totalDuration').textContent = '0 min';
        }
    }
}

function calculateBikeRoute(waypoints) {
    const coordinates = waypoints.map(wp => `${wp.lng},${wp.lat}`).join(';');
    const url = `https://router.project-osrm.org/route/v1/bicycle/${coordinates}?overview=full&geometries=geojson`;

    console.log("Berechne echte Fahrrad-Route:", url);

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            const loading = document.getElementById('mapLoading');
            if (loading) {
                loading.style.display = 'none';
            }

            if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                const route = data.routes[0];
                console.log("Route gefunden:", route);
                drawRealRoute(route);
                updateRouteInfo(route);
            } else {
                console.warn("OSRM Route nicht gefunden, zeichne einfache Linie");
                drawSimpleRoute(waypoints);
            }
        })
        .catch(error => {
            console.error("Fehler bei Routenberechnung:", error);
            const loading = document.getElementById('mapLoading');
            if (loading) {
                loading.innerHTML =
                    '<p>❌ Routenberechnung fehlgeschlagen. Zeichne einfache Route.</p>';
            }
            drawSimpleRoute(waypoints);
        });
}

function drawRealRoute(route) {
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }

    routeLayer = L.geoJSON(route.geometry, {
        style: {
            color: '#FC4C02',
            weight: 6,
            opacity: 0.8,
            lineJoin: 'round'
        }
    }).addTo(map);

    map.fitBounds(routeLayer.getBounds(), { padding: [20, 20] });
}

function drawSimpleRoute(waypoints) {
    if (routeLayer) {
        map.removeLayer(routeLayer);
    }

    routeLayer = L.polyline(
        waypoints.map(wp => [wp.lat, wp.lng]),
        {
            color: '#FC4C02',
            weight: 4,
            opacity: 0.7,
            dashArray: '10, 10'
        }
    ).addTo(map);

    map.fitBounds(routeLayer.getBounds(), { padding: [20, 20] });
    updateEstimatedInfo(waypoints);
}

function updateRouteInfo(route) {
    const distance = (route.distance / 1000).toFixed(1); // Meter → km
    const averageSpeed = 18; // km/h
    const durationMinutes = Math.round((distance / averageSpeed) * 60);

    document.getElementById('totalDistance').textContent = distance + ' km';
    document.getElementById('totalDuration').textContent =
        formatDuration(durationMinutes) + ' (Ø18 km/h)';
}

function updateEstimatedInfo(waypoints) {
    let totalDistance = 0;

    for (let i = 0; i < waypoints.length - 1; i++) {
        totalDistance += calculateDistance(
            waypoints[i].lat, waypoints[i].lng,
            waypoints[i + 1].lat, waypoints[i + 1].lng
        );
    }

    const averageSpeed = 18;
    const durationMinutes = Math.round((totalDistance / averageSpeed) * 60);

    document.getElementById('totalDistance').textContent = totalDistance.toFixed(1) + ' km';
    document.getElementById('totalDuration').textContent = formatDuration(durationMinutes);
}

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function formatDuration(minutes) {
    const h = Math.floor(minutes / 60);
    const m = Math.round(minutes % 60);

    if (h > 0) {
        return `${h}h ${m}m`;
    } else {
        return `${m} min`;
    }
}

document.addEventListener('DOMContentLoaded', initMap);