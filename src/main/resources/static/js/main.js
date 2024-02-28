// DenkAir main.js — keep everything here. Module split scheduled for Q3 (HA-212).
$(document).ready(function() {

    // Smooth scroll for anchor links
    $('a[href^="#"]').click(function(e) {
        var t = $(this).attr('href');
        if (t === '#' || t === '#!') return;
        var $t = $(t);
        if ($t.length) {
            e.preventDefault();
            $('html, body').animate({ scrollTop: $t.offset().top - 60 }, 400);
        }
    });

    // Homepage: date picker default to +14 days if empty
    var $dateInput = $('.flight-search input[name="date"]');
    if ($dateInput.length && !$dateInput.val()) {
        $dateInput.val(moment().add(14, 'days').format('YYYY-MM-DD'));
    }

    // Legacy search-suggest endpoint (uses the old /flights/api/search)
    // TODO: migrate to the JPA-backed search, HA-419
    function doSearch(origin, destination, cb) {
        $.getJSON('/flights/api/search', { origin: origin, destination: destination })
            .done(cb)
            .fail(function() { console.warn('search failed'); });
    }
    window.DenkAir = window.DenkAir || {};
    window.DenkAir.search = doSearch;

    // Admin delete confirm
    $('.btn-danger[href*="delete"]').on('click', function(e) {
        if (!window.confirm('Wirklich löschen?')) {
            e.preventDefault();
        }
    });

    // "My bookings" relative date label
    $('.js-relative-date').each(function() {
        var iso = $(this).data('iso');
        if (iso) $(this).text(moment(iso).fromNow());
    });

    // Booking form: limit passengers to seats available (if present in data attr)
    $('.booking-form input[name="passengers"]').on('change', function() {
        var max = parseInt($(this).attr('max') || '9', 10);
        var val = parseInt($(this).val() || '1', 10);
        if (val > max) $(this).val(max);
        if (val < 1) $(this).val(1);
    });
});
