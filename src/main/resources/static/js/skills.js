/* skills.js — 月プルダウン + 分数ステッパー + 保存フォーム連携（サーバ描画前提） */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
  initMonthDropdown();
  initStepper();
  initSaveFormsGlue();
});

/* =========================
   月プルダウン（ボタン＋ul）
   - li[role="option"][data-value="yyyy-MM"] をクリックで
     hidden #monthInput にセットして /skills?month=yyyy-MM で送信
   ========================= */
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

    form.submit(); // /skills?month=yyyy-MM
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

/* =========================
   ▲▼ステッパー
   - 同じ行の input.minutes-input を +1 / -1
   ========================= */
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

/* =========================
   保存フォーム連携
   - 「保存」フォーム送信前に hidden[name="minutes"] へ
     行の .minutes-input の値を詰める
   ========================= */
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
