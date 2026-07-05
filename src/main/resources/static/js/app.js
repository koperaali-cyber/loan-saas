/* LoanSaaS — UI interactions */
(function () {
    "use strict";

    // ---- Sidebar toggle (mobile) ----
    function initSidebar() {
        var sidebar = document.getElementById("sidebar");
        var overlay = document.getElementById("overlay");
        var burger = document.getElementById("hamburger");
        if (!sidebar || !burger) return;

        function open() { sidebar.classList.add("open"); if (overlay) overlay.classList.add("show"); }
        function close() { sidebar.classList.remove("open"); if (overlay) overlay.classList.remove("show"); }

        burger.addEventListener("click", function () {
            sidebar.classList.contains("open") ? close() : open();
        });
        if (overlay) overlay.addEventListener("click", close);
        document.querySelectorAll(".nav-item").forEach(function (item) {
            item.addEventListener("click", function () {
                if (window.innerWidth <= 900) close();
            });
        });
    }

    // ---- Delete confirmations ----
    function initConfirm() {
        document.querySelectorAll("[data-confirm]").forEach(function (el) {
            el.addEventListener("submit", function (e) {
                if (!window.confirm(el.getAttribute("data-confirm"))) {
                    e.preventDefault();
                }
            });
        });
    }

    // ---- Photo preview ----
    function initPhoto() {
        document.querySelectorAll("[data-photo-input]").forEach(function (input) {
            input.addEventListener("change", function () {
                var targetId = input.getAttribute("data-photo-target");
                var target = document.getElementById(targetId);
                if (target && input.files && input.files[0]) {
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        target.innerHTML = '<img src="' + e.target.result +
                            '" style="width:100%;height:100%;object-fit:cover;border-radius:inherit">';
                    };
                    reader.readAsDataURL(input.files[0]);
                }
            });
        });
    }

    // ---- Live loan repayment calculator ----
    function initLoanCalc() {
        var amount = document.getElementById("loanAmount");
        var rate = document.getElementById("loanRate");
        var out = document.getElementById("calcTotal");
        var outInterest = document.getElementById("calcInterest");
        if (!amount || !rate || !out) return;

        function recalc() {
            var a = parseFloat(amount.value) || 0;
            var r = parseFloat(rate.value) || 0;
            var interest = a * r / 100;
            var total = a + interest;
            if (outInterest) outInterest.textContent = interest.toLocaleString(undefined, { maximumFractionDigits: 0 }) + " TZS";
            out.textContent = total.toLocaleString(undefined, { maximumFractionDigits: 0 }) + " TZS";
        }
        amount.addEventListener("input", recalc);
        rate.addEventListener("input", recalc);
        recalc();
    }

    // ---- Auto due-date from start + duration ----
    function initDueDate() {
        var start = document.getElementById("loanStart");
        var duration = document.getElementById("loanDuration");
        var due = document.getElementById("loanDue");
        if (!start || !duration || !due) return;
        function recompute() {
            if (!start.value || !duration.value) return;
            var d = new Date(start.value);
            d.setMonth(d.getMonth() + parseInt(duration.value, 10));
            due.value = d.toISOString().slice(0, 10);
        }
        start.addEventListener("change", recompute);
        duration.addEventListener("input", recompute);
    }

    // ---- Simple client table search ----
    function initTableSearch() {
        document.querySelectorAll("[data-table-filter]").forEach(function (input) {
            var targetId = input.getAttribute("data-table-filter");
            var table = document.getElementById(targetId);
            if (!table) return;
            input.addEventListener("input", function () {
                var q = input.value.toLowerCase();
                table.querySelectorAll("tbody tr").forEach(function (row) {
                    row.style.display = row.textContent.toLowerCase().indexOf(q) > -1 ? "" : "none";
                });
            });
        });
    }

    // ---- Sortable tables ----
    function initSort() {
        document.querySelectorAll("table.data th[data-sort]").forEach(function (th) {
            th.style.cursor = "pointer";
            th.addEventListener("click", function () {
                var table = th.closest("table");
                var idx = Array.prototype.indexOf.call(th.parentNode.children, th);
                var tbody = table.querySelector("tbody");
                var rows = Array.prototype.slice.call(tbody.querySelectorAll("tr"));
                var asc = th.getAttribute("data-asc") !== "true";
                th.setAttribute("data-asc", asc);
                rows.sort(function (a, b) {
                    var x = a.children[idx].getAttribute("data-value") || a.children[idx].textContent.trim();
                    var y = b.children[idx].getAttribute("data-value") || b.children[idx].textContent.trim();
                    var nx = parseFloat(x.replace(/[^0-9.-]/g, ""));
                    var ny = parseFloat(y.replace(/[^0-9.-]/g, ""));
                    if (!isNaN(nx) && !isNaN(ny)) return asc ? nx - ny : ny - nx;
                    return asc ? x.localeCompare(y) : y.localeCompare(x);
                });
                rows.forEach(function (r) { tbody.appendChild(r); });
            });
        });
    }

    // ---- Clickable rows (data-href) ----
    function initRowLinks() {
        document.querySelectorAll("[data-href]").forEach(function (row) {
            row.style.cursor = "pointer";
            row.addEventListener("click", function (e) {
                if (e.target.closest("a,button,form,input")) return;
                window.location = row.getAttribute("data-href");
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initSidebar();
        initConfirm();
        initPhoto();
        initLoanCalc();
        initDueDate();
        initTableSearch();
        initSort();
        initRowLinks();
    });
})();
