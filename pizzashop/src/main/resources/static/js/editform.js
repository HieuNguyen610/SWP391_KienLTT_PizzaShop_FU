document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".info_edit_form");

    form.addEventListener("submit", function (e) {
        let changed = false;

        form.querySelectorAll("input").forEach(input => {
            if (input.hasAttribute("readonly")) {
                return; // bỏ qua email
            }

            const original = (input.defaultValue || "").trim();
            const current  = (input.value || "").trim();

            if (original !== current) {
                changed = true;
            }
        });

        if (!changed) {
            e.preventDefault();
            alert("Have no changes to save.");
        }
    });
});