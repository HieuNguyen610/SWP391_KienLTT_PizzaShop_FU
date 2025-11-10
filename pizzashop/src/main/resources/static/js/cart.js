document.addEventListener('DOMContentLoaded', function() {
  try {
    var addBtn = document.querySelector('.fd-addtocart');
    var qtyInput = document.getElementById('qty');
    var priceLabel = document.querySelector('.fd-price'); // top price display
    var btnPriceSpan = addBtn ? addBtn.querySelector('span') : null; // price text inside button

    function parsePriceVnd(text) { // renamed behavior: parse USD
      if (!text) return 0;
      var cleaned = ('' + text).replace(/[^0-9.]/g, '');
      if (!cleaned) return 0;
      var val = parseFloat(cleaned);
      return isNaN(val) ? 0 : val;
    }

    function formatVnd(amount) { // now format USD
      try {
        return '$' + (Number(amount)||0).toFixed(2);
      } catch (e) {
        return '$' + amount;
      }
    }

    function getUnitPrice() {
      // Prefer the standalone price label on the page
      if (priceLabel) {
        return parsePriceVnd(priceLabel.textContent);
      }
      // Fallback to the price inside button if present
      if (btnPriceSpan) {
        return parsePriceVnd(btnPriceSpan.textContent);
      }
      return 0;
    }

    function updateDisplayedPrice() {
      var qty = 1;
      if (qtyInput) {
        var v = parseInt(qtyInput.value || '1', 10);
        qty = Number.isFinite(v) && v > 0 ? v : 1;
      }
      var unit = getUnitPrice();
      if (btnPriceSpan && unit > 0) {
        btnPriceSpan.textContent = formatVnd(unit * qty);
      }
    }

    // Bind quantity increment/decrement updates
    if (qtyInput) {
      qtyInput.addEventListener('input', updateDisplayedPrice);
      // initial refresh shortly after load in case template filled values late
      setTimeout(updateDisplayedPrice, 0);
    }

    // Do not intercept Add to cart clicks when the button belongs to a POST form;
    // keep dynamic price updates. This allows the food-detail page to submit the form normally.
    if (addBtn) {
      var enclosingForm = addBtn.closest && addBtn.closest('form');
      var isPostForm = enclosingForm && enclosingForm.method && enclosingForm.method.toLowerCase() === 'post';
      if (!isPostForm) {
        // Only intercept when there is no POST form; otherwise, let the form submit normally
        addBtn.addEventListener('click', function(e) {
          e.preventDefault();
          var qty = 1;
          if (qtyInput) {
            var v = parseInt(qtyInput.value || '1', 10);
            qty = Number.isFinite(v) && v > 0 ? v : 1;
          }
          var foodId = addBtn.getAttribute('data-foodid');
          var notesEl = document.getElementById('notes');
          var notesVal = notesEl ? notesEl.value.trim() : '';
          var sizeId = 1; // internal default size
          if (!foodId) {
            console.debug('Add to cart: missing foodId');
            return;
          }
          var url = '/cart/add?foodId=' + encodeURIComponent(foodId)
                  + '&sizeId=' + encodeURIComponent(sizeId)
                  + '&quantity=' + encodeURIComponent(qty);
          if (notesVal) {
            url += '&notes=' + encodeURIComponent(notesVal);
          }
          window.location.assign(url);
        });
      }
    }

    // Intercept Remove link clicks on the cart page and convert to POST with confirm
    var removeLinks = document.querySelectorAll('a[href^="/cart/item/"][href$="/remove"]');
    if (removeLinks && removeLinks.length) {
      removeLinks.forEach(function(link){
        link.addEventListener('click', function(e){
          e.preventDefault();
          var href = link.getAttribute('href');
          if (!href) return;
          var ok = window.confirm('Bạn có chắc muốn xóa món này khỏi giỏ hàng?');
          if (!ok) return;

          // Build a POST form to submit to the remove endpoint
          var form = document.createElement('form');
          form.method = 'post';
          form.action = href;
          form.style.display = 'none';

          // Try to include CSRF token if present (reuse token from logout form)
          try {
            var csrfInput = document.querySelector('form[action$="/logout"] input[type="hidden"][name]');
            if (csrfInput) {
              var csrfName = csrfInput.getAttribute('name');
              var csrfValue = csrfInput.value;
              if (csrfName && csrfValue) {
                var hidden = document.createElement('input');
                hidden.type = 'hidden';
                hidden.name = csrfName;
                hidden.value = csrfValue;
                form.appendChild(hidden);
              }
            }
          } catch (err) {
            // ignore if CSRF not found; server may allow or redirect
            if (window.console) console.debug('CSRF not attached for remove:', err);
          }

          document.body.appendChild(form);
          form.submit();
        });
      });
    }

    // Route Checkout button on cart page to /checkout if it's a placeholder link
    (function(){
      try {
        var checkoutBtn = document.querySelector('.cart-actions a.btn:not(.secondary)');
        if (checkoutBtn) {
          // Remove any inline onclick to avoid legacy alert popup
          try { checkoutBtn.removeAttribute('onclick'); } catch(_) {}
          checkoutBtn.addEventListener('click', function(e){
            var href = checkoutBtn.getAttribute('href');
            // If href is '#' or empty (placeholder), navigate to /checkout
            if (!href || href === '#') {
              e.preventDefault();
              e.stopPropagation();
              window.location.assign('/checkout');
            }
          });
        }
      } catch(_e) {}
    })();
  } catch (e) {
    if (window && window.console) console.debug('cart.js init skipped:', e);
  }
});
