let map;
let waypointCounter = 1;
let markers = [];

function initMap() {
    map = L.map('map').setView([47.7810, 9.6100], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);
    map.on('click', function(e) {
        addWaypointFromMap(e.latlng.lat, e.latlng.lng);
    });
}

function addWaypointFromMap(lat, lng) {
    const waypointItem = document.getElementById('waypointTemplate').cloneNode(true);
    waypointItem.style.display = 'block';
    waypointItem.id = '';
    
    const nameInput = waypointItem.querySelector('.waypoint-name');
    const latInput = waypointItem.querySelector('.waypoint-lat');
    const lngInput = waypointItem.querySelector('.waypoint-lng');
    const dataInput = waypointItem.querySelector('.waypoint-data');
    
    nameInput.value = `Punkt ${waypointCounter}`;
    latInput.value = lat.toFixed(6);
    lngInput.value = lng.toFixed(6);
    
    const waypointData = { name: nameInput.value, latitude: lat, longitude: lng };
    dataInput.name = `waypoints[${waypointCounter - 1}]`;
    dataInput.value = JSON.stringify(waypointData);
    
    document.getElementById('waypointsContainer').appendChild(waypointItem);
    
    const marker = L.marker([lat, lng]).addTo(map).bindPopup(nameInput.value);
    markers.push(marker);
    
    waypointCounter++;
    updateWaypointNumbers();
}

function addWaypoint() {
    // manueller Wegpunkt (Standardposition)
    addWaypointFromMap(52.5200, 13.4050);
}

function removeWaypoint(button) {
    const waypointItem = button.closest('.waypoint-item');
    const index = Array.from(waypointItem.parentNode.children).indexOf(waypointItem);
    waypointItem.remove();
    if (markers[index]) {
        map.removeLayer(markers[index]);
        markers.splice(index, 1);
    }
    updateWaypointNumbers();
}

function updateWaypointNumbers() {
    const waypoints = document.querySelectorAll('.waypoint-item');
    waypoints.forEach((item, index) => {
        const nameInput = item.querySelector('.waypoint-name');
        const dataInput = item.querySelector('.waypoint-data');
        if (!nameInput.value.startsWith('Punkt')) return;
        nameInput.value = `Punkt ${index + 1}`;
        try {
            const waypointData = JSON.parse(dataInput.value);
            waypointData.name = nameInput.value;
            dataInput.value = JSON.stringify(waypointData);
        } catch (e) {
            console.error('Fehler beim Aktualisieren der Wegpunkt-Daten:', e);
        }
        dataInput.name = `waypoints[${index}]`;
    });
    waypointCounter = waypoints.length + 1;
}

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

        const waypoints = document.querySelectorAll('.waypoint-item');
        if (waypoints.length === 0) {
            alert('❌ Bitte füge mindestens einen Wegpunkt hinzu!');
            return;
        }

        const originalBtnText = saveBtn.innerText;
        saveBtn.disabled = true;
        saveBtn.innerText = '💾 Speichere...';

        try {
            const actionUrl = form.getAttribute('action');
            const formData = new FormData(form);
            const csrfToken = form.querySelector('input[name="_csrf"]')?.value;

            const response = await fetch(actionUrl, {
                method: 'POST',
                body: formData,
                headers: csrfToken ? { 'X-CSRF-TOKEN': csrfToken } : undefined
            });

            if (response.ok) {
                // Folge der tatsächlichen Ziel-URL nach Redirect (z.B. /route/{id})
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
