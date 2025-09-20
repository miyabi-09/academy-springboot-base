/* skills.js — 月プルダウン + 分数ステッパー + 保存フォーム連携（サーバ描画前提） */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
  initMonthDropdown();
  initStepper();
  initSaveFormsGlue();
});

// 月プルダウン（ボタン＋ul）
function initMonthDropdown() {
  const form  = document.getElementById('monthForm');
  const btn   = document.getElementById('monthBtn');
  const menu  = document.getElementById('monthMenu');
  const label = document.getElementById('monthLabel');
  const input = document.getElementById('monthInput');
  if (!form || !btn || !menu || !label || !input) return;

  if (menu.children.length === 0) {
    const selected = input.value || ym(0);
    [0, 1, 2].forEach(i => {
      const li = document.createElement('li');
      li.setAttribute('role', 'option');
      const v = ym(i);
      li.dataset.value = v;
      li.textContent = monthJpLabel(v);
      if (v === selected) {
        li.classList.add('is-selected');
        li.setAttribute('aria-selected', 'true');
      }
      menu.appendChild(li);
    });
    label.textContent = monthJpLabel(selected);
  }

  btn.addEventListener('click', (e) => {
    e.preventDefault();
    const open = btn.getAttribute('aria-expanded') === 'true';
    btn.setAttribute('aria-expanded', String(!open));
    menu.hidden = open;
  });

  menu.addEventListener('click', (e) => {
    const li = e.target.closest('li[role="option"]');
    if (!li) return;

    const value = li.getAttribute('data-value');
    const text  = li.textContent.trim();

    input.value = value;
    label.textContent = text;

    menu.querySelectorAll('li[role="option"]').forEach(x => {
      x.removeAttribute('aria-selected');
      x.classList.remove('is-selected');
    });
    li.setAttribute('aria-selected', 'true');
    li.classList.add('is-selected');

    menu.hidden = true;
    btn.setAttribute('aria-expanded', 'false');

    form.submit(); // /skills?month=yyyy-MM
  });

  document.addEventListener('click', (e) => {
    if (menu.hidden) return;
    if (e.target === btn || btn.contains(e.target) || menu.contains(e.target)) return;
    menu.hidden = true;
    btn.setAttribute('aria-expanded', 'false');
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      menu.hidden = true;
      btn.setAttribute('aria-expanded', 'false');
    }
  });
}

// ▲▼ステッパー（1分刻み・0〜1440の範囲）
function initStepper() {
  document.addEventListener('click', (e) => {
    const stepBtn = e.target.closest('.number-stepper .step');
    if (!stepBtn) return;

    const up = stepBtn.classList.contains('up');
    const input = stepBtn.closest('.number-stepper')?.querySelector('.minutes-input');
    if (!input) return;

    const min = +(input.min || 0);
    const max = +(input.max || 1440);
    let v = parseInt(input.value || '0', 10) || 0;

    v += up ? 1 : -1;
    if (v < min) v = min;
    if (v > max) v = max;

    input.value = String(v);
    input.dispatchEvent(new Event('input', { bubbles: true }));

  });

  // 入力確定時（change/blur）のみ 0〜1440 に正規化
function normalizeMinutesInput(el) {
  let s = el.value.trim();
  if (s === '') {                 // 空のまま確定したら 0
    el.value = '0';
    return;
  }
  let n = Number(s);
  if (!Number.isFinite(n)) n = 0;
  n = Math.round(Math.max(0, Math.min(n, 1440)));
  el.value = String(n);
}

document.addEventListener('change', (e) => {
  const el = e.target;
  if (el instanceof HTMLInputElement && el.classList.contains('minutes-input')) {
    normalizeMinutesInput(el);
  }
});

document.addEventListener('blur', (e) => {
  const el = e.target;
  if (el instanceof HTMLInputElement && el.classList.contains('minutes-input')) {
    normalizeMinutesInput(el);
  }
}, true); // キャプチャで blur を拾う


// 保存フォーム連携（hidden minutes に詰めてから送信）
function initSaveFormsGlue() {
  document.addEventListener('submit', (e) => {
    const form = e.target;
    if (!(form instanceof HTMLFormElement)) return;

    // 「保存フォーム」のみ対象：hidden minutes を持っているかで判定
    const hidden = form.querySelector('input[type="hidden"][name="minutes"]');
    if (!hidden) return;

    // まず同じ行から minutes-input を探す
    let minutesInput = form.closest('tr')?.querySelector('.minutes-input');

    // 行が掴めないケースの保険：id一致で探す
    if (!minutesInput) {
      const idField = form.querySelector('input[name="id"]');
      const idVal = idField && idField.value ? idField.value : null;
      if (idVal) {
        minutesInput = document.querySelector(`.minutes-input[data-id="${CSS.escape(idVal)}"]`);
      }
    }

    // それでも見つからなければ 0 扱い（送信は継続）
    let n = 0;
    if (minutesInput) {
      const v = Number(minutesInput.value);
      n = Number.isFinite(v) ? v : 0;
    }

    // 1分単位で 0〜1440 に丸めて hidden へ
    n = Math.round(Math.max(0, Math.min(n, 1440)));
    hidden.value = String(n);
  });
}

/* ---------- helpers ---------- */
function ym(back = 0) {
  const d = new Date();
  d.setDate(1);
  d.setMonth(d.getMonth() - back);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  return `${y}-${m}`; // yyyy-MM
}
function monthJpLabel(isoYm) {
  const m = Number(String(isoYm || '').split('-')[1] || 0);
  return m ? `${m}月` : '';
}

// --- 編集完了モーダルを表示（残像対策付き） ---
(function () {
  function showEditDoneModal() {
    const dlg = document.getElementById('deletedModal')
            || document.getElementById('addedModal')
            || document.getElementById('editDoneModal');
    if (!dlg) return;

    if (typeof dlg.showModal === 'function') {
      if (!dlg.open) dlg.showModal();
    } else {
      dlg.setAttribute('open', '');
    }

    const forceRepaint = () => { void document.body.offsetHeight; };
    dlg.addEventListener('close', () => {
      requestAnimationFrame(() => { forceRepaint(); dlg.remove(); });
    });
    dlg.addEventListener('cancel', () => {
      requestAnimationFrame(() => { forceRepaint(); dlg.remove(); });
    });
    dlg.querySelector('.modal-btn')?.addEventListener('click', () => dlg.close());
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', showEditDoneModal);
  } else {
    showEditDoneModal();
  }
})();
}
