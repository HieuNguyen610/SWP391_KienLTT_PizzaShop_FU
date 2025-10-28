(function(){
  function $(sel, root){ return (root||document).querySelector(sel); }
  function $all(sel, root){ return Array.prototype.slice.call((root||document).querySelectorAll(sel)); }

  var modalEl, titleEl, msgEl, btnConfirm, btnCancel;
  var onConfirmCb = null, onCancelCb = null;

  function open(opts){
    if (!modalEl) return;
    var title = opts && opts.title ? opts.title : 'Confirm';
    var message = opts && opts.message ? opts.message : '';
    var confirmText = opts && opts.confirmText ? opts.confirmText : 'OK';
    var cancelText = opts && opts.cancelText ? opts.cancelText : 'Cancel';
    onConfirmCb = typeof opts.onConfirm === 'function' ? opts.onConfirm : null;
    onCancelCb = typeof opts.onCancel === 'function' ? opts.onCancel : null;

    titleEl.textContent = title;
    msgEl.textContent = message;
    btnConfirm.textContent = confirmText;
    btnCancel.textContent = cancelText;

    modalEl.classList.remove('hidden');
    modalEl.setAttribute('aria-hidden', 'false');
  }

  function close(){
    if (!modalEl) return;
    modalEl.classList.add('hidden');
    modalEl.setAttribute('aria-hidden', 'true');
  }

  function wire(){
    modalEl = document.getElementById('app-modal');
    if (!modalEl) return;
    titleEl = document.getElementById('modal-title');
    msgEl = document.getElementById('modal-message');
    btnConfirm = document.getElementById('modal-confirm');
    btnCancel = document.getElementById('modal-cancel');

    // Close interactions
    $all('[data-close]', modalEl).forEach(function(el){
      el.addEventListener('click', function(){ close(); if (onCancelCb) onCancelCb(); });
    });
    btnCancel && btnCancel.addEventListener('click', function(){ close(); if (onCancelCb) onCancelCb(); });
    btnConfirm && btnConfirm.addEventListener('click', function(){ close(); if (onConfirmCb) onConfirmCb(); });

    // Global form confirm handler
    document.addEventListener('submit', function(e){
      var form = e.target;
      if (!(form instanceof HTMLFormElement)) return;
      if (form.dataset.skipConfirm === 'true') return; // allow programmatic submits
      var message = form.getAttribute('data-confirm');
      if (!message) return;
      e.preventDefault();
      open({
        title: form.getAttribute('data-confirm-title') || 'Confirm',
        message: message,
        confirmText: form.getAttribute('data-confirm-ok') || 'OK',
        cancelText: form.getAttribute('data-confirm-cancel') || 'Cancel',
        onConfirm: function(){
          // prevent re-intercept
          form.dataset.skipConfirm = 'true';
          try { form.submit(); } finally { delete form.dataset.skipConfirm; }
        },
        onCancel: function(){}
      });
    });
  }

  document.addEventListener('DOMContentLoaded', wire);

  window.AppModal = { open: open, close: close };
})();

