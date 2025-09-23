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

  // （保険）サーバが li を出せなかったときは当月+過去2ヶ月を自前で生成
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

  // 開閉
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    const open = btn.getAttribute('aria-expanded') === 'true';
    btn.setAttribute('aria-expanded', String(!open));
    menu.hidden = open;
  });

  // 選択 → hidden へ反映 → 送信
  menu.addEventListener('click', (e) => {
    const li = e.target.closest('li[role="option"]');
    if (!li) return;

    const value = li.getAttribute('data-value');  // "yyyy-MM"
    const text  = li.textContent.trim();         // "M月"

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

    form.submit(); // ★ /skills-legacy?month=yyyy-MM に送信（HTML側で変更済み）
  });

  // 外側クリック/ESCで閉じる
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

// ▲▼ステッパー
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
    input.dispatchEvent(new Event('change', { bubbles: true }));
  });
}

// 保存フォーム連携
function initSaveFormsGlue() {
  document.addEventListener('submit', (e) => {
    const form = e.target;
    if (!(form instanceof HTMLFormElement)) return;

    // 保存フォームの見分け方：hidden minutes を持っている
    const hidden = form.querySelector('input[type="hidden"][name="minutes"]');
    if (!hidden) return;

    // 同じ行の分数を取得
    const row = form.closest('tr');
    const minutesInput = row?.querySelector('.minutes-input');
    if (minutesInput) {
      hidden.value = minutesInput.value || '0';
    }
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

// --- 削除フォーム 連打防止（決定版） ---
(function installDeleteGuard() {
  if (window.__deleteGuardInstalled) return;
  window.__deleteGuardInstalled = true;

  // 1) submit は一度きり（ここで初回にフラグを立て、ボタン無効化）
  document.addEventListener('submit', (e) => {
    const form = e.target.closest('form.delete-form');
    if (!form) return;

    if (form.dataset.submitting === '1') {
      e.preventDefault(); // 2回目以降をブロック
      return;
    }
    form.dataset.submitting = '1'; // ★ 初回はここで立てる

    const btn = form.querySelector('button[type="submit"]');
    if (btn) btn.disabled = true;  // 見た目もロック
  }, { capture: true });

  // 2) クリック経路は「既に送信中なら止める」だけ（★ここでフラグは立てない）
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('form.delete-form button[type="submit"]');
    if (!btn) return;
    const form = btn.closest('form.delete-form');
    if (form.dataset.submitting === '1') {
      e.preventDefault(); // 2回目以降のクリックを無視
    }
    // フラグは submit 側で立てる
  }, { capture: true });

  console.info('[delete-guard] installed');
})();

// --- フラッシュ用モーダルを自動表示（deletedModal / addedModal どちらでも） ---
(function openFlashDialogOnce() {
  const dlg = document.getElementById('deletedModal') || document.getElementById('addedModal');
  if (!dlg) return; // フラッシュ無ければ何もしない（th:if で未描画）

  try {
    if (typeof dlg.showModal === 'function') {
      if (!dlg.open) dlg.showModal(); // HTMLDialogElement
    } else {
      dlg.setAttribute('open', '');   // 古いブラウザ向けフォールバック
    }
  } catch (e) {
    console.warn('[flash-dialog]', e);
    dlg.setAttribute('open', '');     // 念のため
  }

  // 閉じたらDOMから取り除いて残像防止（任意）
  const cleanup = () => dlg.remove();
  dlg.addEventListener('close', cleanup, { once: true });
  dlg.addEventListener('cancel', cleanup, { once: true });
})();
