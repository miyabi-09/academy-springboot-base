// /static/js/skills-new-validate.js
'use strict';

document.addEventListener('DOMContentLoaded', () => {
  // このページだけで使うので、固定IDで取ります
  const form  = document.getElementById('createForm');
  const nameI = document.getElementById('name');
  const nameE = document.getElementById('nameError');          // 常設の空div
  const timeI = document.getElementById('studyTime');
  const timeE = document.getElementById('studyTimeError');      // 常設の空div
  const helpE = document.getElementById('studyTimeHelp');       // ヘルプ(任意)

  // どれか取れないなら何もしない（IDズレ検出用ログ）
  if (!form || !nameI || !nameE || !timeI || !timeE) {
    console.warn('[skills-new] missing elements', {form, nameI, nameE, timeI, timeE});
    return;
  }

  // ---- 項目名の検証 ----
  function validateName() {
    const v = (nameI.value || '').trim();
    let ok = true, msg = '';
    if (v.length === 0) {
      ok = false; msg = '項目名は必ず入力してください';
    } else if (v.length > 50) {
      ok = false; msg = '項目名は50文字以内で入力してください';
    }
    // ネイティブの吹き出しは使わない
    nameI.setCustomValidity('');
    // 見た目とメッセージ
    if (!ok) {
      nameI.setAttribute('aria-invalid','true');
      nameE.textContent = msg;
    } else {
      nameI.removeAttribute('aria-invalid');
      nameE.textContent = '';
    }
    return ok;
  }

  // ---- 学習時間(分)の検証 ----
  function validateMinutes() {
    const raw = (timeI.value || '').trim();
    let ok = true, msg = '';
    if (raw === '') {
      ok = false; msg = '学習時間を入力してください';
    } else if (!/^-?\d+$/.test(raw)) {
      ok = false; msg = '半角の整数で入力してください';
    } else {
      const n = Number(raw);
      if (n < 0)       { ok = false; msg = '学習時間は0以上で入力してください'; }
      else if (n > 1440){ ok = false; msg = '学習時間は1440以下で入力してください'; }
    }

    timeI.setCustomValidity('');
    if (!ok) {
      timeI.setAttribute('aria-invalid','true');
      timeE.textContent = msg;
      if (helpE) helpE.style.display = 'none';
    } else {
      timeI.removeAttribute('aria-invalid');
      timeE.textContent = '';
      if (helpE) helpE.style.display = '';
    }
    return ok;
  }

  // ---- ライブ検証（入力時）
  nameI.addEventListener('input',  validateName);
  timeI.addEventListener('input',  validateMinutes);

  // ステッパーボタンでも即時検証
  form.addEventListener('click', (e) => {
    const btn = e.target.closest('.number-stepper .step');
    if (!btn) return;
    // 数値が変わるので、inputイベント相当を飛ばす
    timeI.dispatchEvent(new Event('input', { bubbles: true }));
  });

  // ホイールで意図せず変わるのを抑止（任意）
  timeI.addEventListener('wheel', (e) => {
    if (document.activeElement === timeI) e.preventDefault();
  }, { passive: false });

  // ---- 送信時の最終チェック（両方必ず評価）
  form.addEventListener('submit', (e) => {
    const okName = validateName();
    const okTime = validateMinutes();
    if (!(okName && okTime)) {
      e.preventDefault();
      // 最初のエラーへフォーカス
      (document.querySelector('[aria-invalid="true"]') || nameI).focus();
    }
  });
});
