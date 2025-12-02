document.addEventListener('DOMContentLoaded', () => {
    const deleteForms = document.querySelectorAll('.delete-route-form');

    deleteForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const ok = confirm('Route wirklich löschen?');
            if (!ok) {
                e.preventDefault();
            }
        });
    });
});
