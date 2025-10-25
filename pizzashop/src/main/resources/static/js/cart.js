document.addEventListener('DOMContentLoaded', function() {
  try {
    var addBtn = document.querySelector('.fd-addtocart');
    var qtyInput = document.getElementById('qty');
    var priceLabel = document.querySelector('.fd-price'); // top price display
    var btnPriceSpan = addBtn ? addBtn.querySelector('span') : null; // price text inside button

    function parsePriceVnd(text) {
      if (!text) return 0;
      // remove any non-digit
      var digits = ('' + text).replace(/[^0-9]/g, '');
      if (!digits) return 0;
      return parseInt(digits, 10);
    }

    function formatVnd(amount) {
      try {
        return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') + 'đ';
      } catch (e) {
        return amount + 'đ';
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
          var sizeId = 1; // default until FoodSize is wired in UI
          if (!foodId) {
            console.debug('Add to cart: missing foodId');
            return;
          }
          var url = '/cart/add?foodId=' + encodeURIComponent(foodId)
                  + '&sizeId=' + encodeURIComponent(sizeId)
                  + '&quantity=' + encodeURIComponent(qty);
          window.location.assign(url);
        });
      }
    }
  } catch (e) {
    if (window && window.console) console.debug('cart.js init skipped:', e);
  }
});
