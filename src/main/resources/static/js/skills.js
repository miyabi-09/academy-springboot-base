/* skills.js — 月プルダウン + 分数ステッパー + 保存フォーム連携 + 削除連打ガード */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
  initMonthDropdown();
  initStepper();
  initMinutesGuards(); 
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

    form.submit(); // /skills-legacy?month=yyyy-MM（HTMLのactionに依存）
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

// ---- minutes入力のクランプ＆ガード ----
function clampInt(v, min = 0, max = 1440) {
  v = parseInt(v ?? '0', 10);
  if (!Number.isFinite(v)) v = 0;
  if (v < min) v = min;
  if (v > max) v = max;
  return v;
}

// ---- minutes入力のガード（ネイティブ検証を生かす）----
function initMinutesGuards() {
  // 入力時にだけ妥当性メッセージを付け外し
  document.addEventListener('input', (e) => {
    const inp = e.target.closest('.minutes-input');
    if (!inp) return;

    const v = inp.value === '' ? null : Number(inp.value);
    // 空は required に任せる。数字で 0〜1440 だけ OK
    if (v === null) {
      inp.setCustomValidity(''); // required が面倒を見る
    } else if (Number.isNaN(v) || v < 0 || v > 1440) {
      inp.setCustomValidity('0〜1440の数字で入力してください');
    } else {
      inp.setCustomValidity('');
    }
  });

  // ホイールで勝手に値が動くのを防ぐ（任意）
  document.addEventListener('wheel', (e) => {
    const inp = e.target.closest('.minutes-input');
    if (!inp || document.activeElement !== inp) return;
    e.preventDefault();
  }, { passive: false });
}

// 置き場所：skills.js の initSaveFormsGlue の上あたりに追記
function validateMinutesInput(inp) {
  const msgEl = inp.closest('td')?.querySelector('.field-error');
  const raw = inp.value;
  let msg = '';

  if (raw === '' || raw == null) {
    msg = '学習時間は必須です';
  } else if (!/^-?\d+$/.test(raw)) {
    msg = '半角の整数で入力してください';
  } else {
    const v = Number(raw);
    if (v < 0) msg = '0以上の数字で入力してください';
    else if (v > 1440) msg = '1440以下で入力してください';
  }

  if (msgEl) msgEl.textContent = msg;
  inp.classList.toggle('is-invalid', !!msg);
  return msg === '';
}

// 入力中はエラーの消し/出しだけ行う（値は勝手に直さない）
function initMinutesGuards() {
  document.addEventListener('input', (e) => {
    const inp = e.target.closest('.minutes-input');
    if (!inp) return;
    validateMinutesInput(inp);
  });

  // 任意：フォーカスが外れた時にも再検証
  document.addEventListener('blur', (e) => {
    const inp = e.target.closest('.minutes-input');
    if (!inp) return;
    validateMinutesInput(inp);
  }, true);

  // 任意：ホイールでの意図しない変化を防ぐ
  document.addEventListener('wheel', (e) => {
    const inp = e.target.closest('.minutes-input');
    if (!inp || document.activeElement !== inp) return;
    e.preventDefault();
  }, { passive: false });
}



// 保存フォーム連携（hidden minutes に入力値を詰める）

function initSaveFormsGlue() {
  document.addEventListener('submit', (e) => {
    const form = e.target;
    if (!(form instanceof HTMLFormElement)) return;

    // 保存フォームの見分け方：hidden minutes を持っている
    const hidden = form.querySelector('input[type="hidden"][name="minutes"]');
    if (!hidden) return;

    // 同じ行の minutes を取得
    const row = form.closest('tr');
    const minutesInput = row?.querySelector('.minutes-input');
    if (!minutesInput) return;

    // ★ ここがポイント：ネイティブ検証を発火
    if (!minutesInput.reportValidity()) {
      e.preventDefault();      // 送信を止める
      minutesInput.focus();    // フォーカスを戻す
      return;
    }

    // 妥当ならそのまま値を詰めて送信
    hidden.value = minutesInput.value || '0';
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
    

    // 既に送信中ならブロック
    if (form.dataset.submitting === '1') {
      e.preventDefault();
      return;
    }
    // 初回送信：フラグを立てて見た目もロック
    form.dataset.submitting = '1';
    const btn = form.querySelector('button[type="submit"]');
    if (btn) btn.disabled = true;
  }, { capture: true });

  // 2) クリック経路：既に送信中なら止める（ここではフラグは立てない）
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('form.delete-form button[type="submit"]');
    if (!btn) return;
    const form = btn.closest('form.delete-form');
    if (form.dataset.submitting === '1') {
      e.preventDefault();
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
  
  // 閉じたらDOMから取り除いて残像防止
  const cleanup = () => dlg.remove();
  dlg.addEventListener('close', cleanup, { once: true });
  dlg.addEventListener('cancel', cleanup, { once: true });
})();
