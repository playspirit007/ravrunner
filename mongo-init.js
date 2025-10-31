// Zuerst zur admin Datenbank wechseln und User erstellen
db = db.getSiblingDB('admin');

// User für ravrunner Datenbank erstellen
db.createUser({
    user: 'ravuser',
    pwd: 'ravpass',
    roles: [
        {
            role: 'readWrite',
            db: 'ravrunner'
        }
    ]
});

// Zur ravrunner Datenbank wechseln
db = db.getSiblingDB('ravrunner');

// Testdaten einfügen
db.routes.insertMany([
    {
        name: "Stadtpark Runde",
        description: "Eine schöne Runde durch den Stadtpark",
        waypoints: [
            { name: "Start", latitude: 52.5200, longitude: 13.4050 },
            { name: "Park Mitte", latitude: 52.5210, longitude: 13.4100 },
            { name: "See", latitude: 52.5190, longitude: 13.4150 }
        ]
    },
    {
        name: "Fluss Tour",
        description: "Entlang des Flusses",
        waypoints: [
            { name: "Start Fluss", latitude: 52.5180, longitude: 13.4000 },
            { name: "Brücke", latitude: 52.5170, longitude: 13.4050 }
        ]
    }
]);

print("MongoDB Initialisierung abgeschlossen!");