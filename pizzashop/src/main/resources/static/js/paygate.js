(function(){
  function ready(fn){ if(document.readyState !== 'loading'){ fn(); } else { document.addEventListener('DOMContentLoaded', fn); } }

  ready(function(){
    var payBtn = document.getElementById('payNow');
    var cancelEl = document.querySelector('.pg-foot');

    // Make cancel return to checkout if it's not an anchor
    if (cancelEl && cancelEl.tagName !== 'A') {
      cancelEl.style.cursor = 'pointer';
      cancelEl.addEventListener('click', function(){ window.location.href = '/checkout'; });
    }

    if (!payBtn) return; // Not on paygate page

    // If Pay Now is within a form, do nothing and let the browser submit
    var enclosingForm = payBtn.closest && payBtn.closest('form');
    if (enclosingForm) {
      return;
    }

    // Legacy AJAX fallback (not used if form present)
    payBtn.addEventListener('click', async function(){
      if (payBtn.disabled) return;
      var old = payBtn.textContent;
      payBtn.disabled = true;
      payBtn.textContent = 'Processing...';
      try {
        var res = await fetch('/api/payments/confirm', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({})
        });
        var data = null;
        if (res.ok) {
          try { data = await res.json(); } catch (_) {}
        }
        if (data && data.success) {
          var ref = encodeURIComponent(data.orderRef || '');
          window.location.href = '/payment/success?orderRef=' + ref;
        } else {
          alert('Payment failed. Please try again.');
          payBtn.disabled = false;
          payBtn.textContent = old;
        }
      } catch (e) {
        alert('Network error. Please try again.');
        payBtn.disabled = false;
        payBtn.textContent = old;
      }
    });
  });
})();
