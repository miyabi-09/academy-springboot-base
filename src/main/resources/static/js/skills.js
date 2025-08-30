/* =========================
    skills.js  (フロント用)
    - 自作プルダウン（過去3ヶ月）
    - ダミーデータでテーブル描画
    - 保存/削除の疑似動作
   ========================= */

'use strict';

// ---------- ユーティリティ ----------
function ym(back = 0) {
    const d = new Date();
    d.setMonth(d.getMonth() - back, 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
  return `${y}-${m}`; // yyyy-MM
}
function monthJpLabel(isoYm) {
  // "2025-08" -> "8月"
    const m = Number(String(isoYm || '').split('-')[1] || 0);
    return m ? `${m}月` : '今月';
}
function escapeHtml(s){
    return String(s).replace(/[&<>"']/g, m => ({
    '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
    }[m]));
}

// ---------- ダミーデータ（month -> category -> rows） ----------
const DUMMY = {
    [ym(0)]: {
    'バックエンド': [
        { id: 1, name: 'Java',         minutes: 30 },
        { id: 2, name: 'Ruby',         minutes: 30 },
        { id: 3, name: 'Spring Boot',  minutes: 30 },
        { id: 4, name: 'PHP',          minutes: 30 },
    ],
    'フロントエンド': [
        { id: 7, name: 'HTML', minutes: 30 },
        { id: 8, name: 'CSS',  minutes: 30 },
    ],
    'インフラ': [
        { id: 9,  name: 'AWS',    minutes: 30 },
        { id: 10, name: 'Heroku', minutes: 30 },
    ],
    },
    [ym(1)]: {
    'バックエンド':   [{ id: 11, name: 'Java',        minutes: 20 }],
    'フロントエンド': [{ id: 12, name: 'HTML',        minutes: 20 }],
    'インフラ':       [{ id: 13, name: 'AWS',         minutes: 20 }],
    },
    [ym(2)]: {
    'バックエンド':   [{ id: 14, name: 'Spring Boot', minutes: 40 }],
    'フロントエンド': [{ id: 15, name: 'CSS',         minutes: 40 }],
    'インフラ':       [],
    },
};

// ---------- 初期化 ----------
document.addEventListener('DOMContentLoaded', () => {
  setupCustomMonthDropdown();     // 自作プルダウンのイベント
  const initial = getSelectedMonth();  // hidden に入ってる yyyy-MM（無ければ今月）

  // 初期描画（ダミー）
    renderAll(initial);

  // ここから下は保存/削除のイベント（イベント委任）
    document.addEventListener('click', onTableButtonClick);
});

// ---------- 自作プルダウン ----------
function setupCustomMonthDropdown() {
    const form  = document.getElementById('monthForm');
    const btn   = document.getElementById('monthBtn');
    const menu  = document.getElementById('monthMenu');
    const label = document.getElementById('monthLabel');
    const input = document.getElementById('monthInput');

    if (!form || !btn || !menu || !label || !input) return;

    // （保険）menu の li が空なら JS で過去3ヶ月を生成
    if (menu.children.length === 0) {
    [0,1,2].forEach(i => {
        const li = document.createElement('li');
        const value = ym(i);
        li.dataset.value = value;
        li.textContent = monthJpLabel(value);
        if (i === 0) li.classList.add('is-selected');
        menu.appendChild(li);
    });
    // ラベルも設定（hidden に値があるならそれ優先）
    label.textContent = monthJpLabel(input.value || ym(0));
    }

    // 開閉
    btn.addEventListener('click', (e) => {
    e.preventDefault();
    const open = btn.getAttribute('aria-expanded') === 'true';
    btn.setAttribute('aria-expanded', String(!open));
    menu.hidden = open;
    });

    // 項目選択 → hidden へセット → 送信（GET）
    menu.addEventListener('click', (e) => {
    const li = e.target.closest('li');
    if (!li) return;

    input.value = li.dataset.value;
    label.textContent = li.textContent.trim();

    menu.querySelectorAll('li').forEach(x => x.classList.toggle('is-selected', x === li));

    menu.hidden = true;
    btn.setAttribute('aria-expanded', 'false');

    // フォーム送信（/skills?month=yyyy-MM）
    form.submit();
    });

    // 外側クリックで閉じる
    document.addEventListener('click', (e) => {
  // ▲/▼ クリックで1分増減
  if (e.target.matches('.number-stepper .step')) {
    const isUp = e.target.classList.contains('up');
    const input = e.target.closest('.number-stepper').querySelector('.minutes-input');
    if (!input) return;

    const min = +(input.min || 0);
    const max = +(input.max || 1440);
    let v = parseInt(input.value || '0', 10) || 0;

    v += isUp ? 1 : -1;
    if (v < min) v = min;
    if (v > max) v = max;

    input.value = v;
    // 変更イベントを発火（必要なら保存ボタンの活性制御などに使える）
    input.dispatchEvent(new Event('change', { bubbles: true }));
  }

  // （ここに既存の .save-btn / .delete-btn の処理が続く）
});


    // Escで閉じる
    document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        menu.hidden = true;
        btn.setAttribute('aria-expanded', 'false');
    }
    });
}

function getSelectedMonth(){
  // サーバが埋めた hidden の month 値（yyyy-MM）
    const input = document.getElementById('monthInput');
    return (input && input.value) ? input.value : ym(0);
}

// ---------- テーブル描画 ----------
function renderAll(month) {
    renderCategory('バックエンド', 'tbody-backend', month);
    renderCategory('フロントエンド', 'tbody-frontend', month);
    renderCategory('インフラ',     'tbody-infra',     month);
}

function renderCategory(category, tbodyId, month) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;

  tbody.replaceChildren();

  const items = (DUMMY[month]?.[category] || [])
    .slice()
    .sort((a, b) => a.name.localeCompare(b.name, 'ja'));

  if (items.length === 0) {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td colspan="3" style="text-align:center;color:#666;">データがありません</td>`;
    tbody.appendChild(tr);
    return;
  }

  for (const item of items) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${escapeHtml(item.name)}</td>
      <td>
        <div class="number-stepper">
          <input type="number"
                 class="learning-hours-input minutes-input"
                 min="0" max="1440" step="1" inputmode="numeric"
                 value="${Number(item.minutes) || 0}"
                 data-id="${item.id}" data-category="${category}">
          <button type="button" class="step up"   aria-label="1分増やす">▲</button>
          <button type="button" class="step down" aria-label="1分減らす">▼</button>
        </div>
      </td>
      <td class="input-button-wrapper">
        <button class="small-btn save-btn"
                data-id="${item.id}" data-category="${category}">学習時間を保存する</button>
        <button class="delete-btn"
                data-id="${item.id}" data-category="${category}">削除する</button>
      </td>
    `;
    tbody.appendChild(tr);
  }
}


// ---------- 保存/削除（フロントだけの疑似動作） ----------
function onTableButtonClick(e){
const month = getSelectedMonth();

    // 保存
    if (e.target.matches('.save-btn')) {
    const id       = Number(e.target.dataset.id);
    const category = e.target.dataset.category;
    const input    = e.target.closest('tr').querySelector('.minutes-input');
    const minutes  = Number(input.value);

    const rec = (DUMMY[month]?.[category] || []).find(r => r.id === id);
    if (rec) rec.minutes = minutes;

    // 本実装では fetch('/skills/update', { method:'POST', body:... }) へ置換
    alert('学習時間を保存しました。');
    }

    // 削除
    if (e.target.matches('.delete-btn')) {
    const id       = Number(e.target.dataset.id);
    const category = e.target.dataset.category;
    if (!confirm('削除してよろしいですか？')) return;

    const list = (DUMMY[month]?.[category] || []);
    const idx  = list.findIndex(r => r.id === id);
    if (idx >= 0) list.splice(idx, 1);

    renderCategory(category, tbodyIdFromCategory(category), month);
    }
}

function tbodyIdFromCategory(category){
    return category === 'バックエンド' ? 'tbody-backend'
        : category === 'フロントエンド' ? 'tbody-frontend'
        : 'tbody-infra';
}
