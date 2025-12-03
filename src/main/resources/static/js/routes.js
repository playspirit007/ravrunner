/* Wird ausgeführt, sobald die Seite vollständig geladen ist */
document.addEventListener('DOMContentLoaded', () => {

    // Alle Lösch-Formulare finden
    const deleteForms = document.querySelectorAll('.delete-route-form');

    // Für jedes Formular einen Bestätigungsdialog einbauen
    deleteForms.forEach(form => {
        form.addEventListener('submit', (e) => {

            // Nutzer fragen, bevor die Route gelöscht wird
            const ok = confirm('Route wirklich löschen?');

            // Wenn "Abbrechen" gedrückt wurde → Submit verhindern
            if (!ok) {
                e.preventDefault();
            }
        });
    });
});
