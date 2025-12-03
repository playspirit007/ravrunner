let map;
let waypointCounter = 1;  // laufende Nummer für Wegpunkte
let markers = [];         // gespeicherte Leaflet-Marker

/* Karte initialisieren */
function initMap() {
    map = L.map('map').setView([47.7810, 9.6100], 14);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // Wegpunkt durch Klick auf die Karte hinzufügen
    map.on('click', function(e) {
        addWaypointFromMap(e.latlng.lat, e.latlng.lng);
    });
}

/* Wegpunkt ins Formular + Karte einfügen */
function addWaypointFromMap(lat, lng) {
    const waypointItem = document.getElementById('waypointTemplate').cloneNode(true);
    waypointItem.style.display = 'block';
    waypointItem.id = '';

    const nameInput = waypointItem.querySelector('.waypoint-name');
    const latInput  = waypointItem.querySelector('.waypoint-lat');
    const lngInput  = waypointItem.querySelector('.waypoint-lng');
    const dataInput = waypointItem.querySelector('.waypoint-data');

    // Formularwerte setzen
    nameInput.value = `Punkt ${waypointCounter}`;
    latInput.value = lat.toFixed(6);
    lngInput.value = lng.toFixed(6);

    // Daten für Backend (als JSON im versteckten Input)
    const waypointData = { name: nameInput.value, latitude: lat, longitude: lng };
    dataInput.name = `waypoints[${waypointCounter - 1}]`;
    dataInput.value = JSON.stringify(waypointData);

    document.getElementById('waypointsContainer').appendChild(waypointItem);

    // Marker auf der Karte anzeigen
    const marker = L.marker([lat, lng]).addTo(map).bindPopup(nameInput.value);
    markers.push(marker);

    waypointCounter++;
    updateWaypointNumbers();
}

/* Wegpunkt entfernen */
function removeWaypoint(button) {
    const waypointItem = button.closest('.waypoint-item');
    const index = Array.from(waypointItem.parentNode.children).indexOf(waypointItem);

    waypointItem.remove();

    // zugehörigen Marker löschen
    if (markers[index]) {
        map.removeLayer(markers[index]);
        markers.splice(index, 1);
    }

    updateWaypointNumbers();
}

/* Wegpunkte neu durchnummerieren (Namen + Input-Felder) */
function updateWaypointNumbers() {
    const waypoints = document.querySelectorAll('.waypoint-item');

    waypoints.forEach((item, index) => {
        const nameInput = item.querySelector('.waypoint-name');
        const dataInput = item.querySelector('.waypoint-data');

        // nur Auto-Namen überschreiben
        if (!nameInput.value.startsWith('Punkt')) return;

        nameInput.value = `Punkt ${index + 1}`;

        try {
            const wp = JSON.parse(dataInput.value);
            wp.name = nameInput.value;
            dataInput.value = JSON.stringify(wp);
        } catch (e) {
            console.error('Fehler beim Aktualisieren der Wegpunkt-Daten:', e);
        }

        // hidden Input-Name anpassen (waypoints[0], waypoints[1]...)
        dataInput.name = `waypoints[${index}]`;
    });

    waypointCounter = waypoints.length + 1;
}

/* Formular-Submit abfangen und per Fetch POST senden */
document.addEventListener('DOMContentLoaded', () => {
    initMap();

    const form = document.getElementById('routeForm');
    if (!form) {
        console.error('Formular #routeForm nicht gefunden.');
        return;
    }

    const saveBtn = document.getElementById('saveBtn');

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        // mindestens 1 Wegpunkt?
        if (document.querySelectorAll('.waypoint-item').length === 0) {
            alert('❌ Bitte füge mindestens einen Wegpunkt hinzu!');
            return;
        }

        // Button sperren
        const originalBtnText = saveBtn.innerText;
        saveBtn.disabled = true;
        saveBtn.innerText = '💾 Speichere...';

        try {
            const actionUrl = form.getAttribute('action');
            const formData = new FormData(form);
            const csrfToken = form.querySelector('input[name="_csrf"]')?.value;

            // Absenden an /route/save
            const response = await fetch(actionUrl, {
                method: 'POST',
                body: formData,
                headers: csrfToken ? { 'X-CSRF-TOKEN': csrfToken } : undefined
            });

            if (response.ok) {
                // Redirect-Ziel folgen (z.B. /route/123)
                window.location.href = response.url;
            } else {
                const text = await response.text().catch(() => '');
                alert(
                    '❌ Speichern fehlgeschlagen.\n\nStatus: ' +
                    response.status + ' ' + response.statusText +
                    (text ? '\n\n' + text : '')
                );
                saveBtn.disabled = false;
                saveBtn.innerText = originalBtnText;
            }

        } catch (err) {
            console.error(err);
            alert('❌ Unerwarteter Fehler beim Speichern.');
            saveBtn.disabled = false;
            saveBtn.innerText = originalBtnText;
        }
    });
});