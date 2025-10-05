// /js/learning-chart.js  ←置き換え
document.addEventListener("DOMContentLoaded", initLearningChart);

async function initLearningChart() {
  const canvas = document.getElementById("learningChart");
  if (!canvas) { console.error("[CHART] #learningChart が見つかりません"); return; }
  if (!window.Chart) { console.error("[CHART] Chart.js が読み込まれていません"); return; }

  // Chart.js v4: 必要コンポーネントを登録（重複登録は無害）
  try {
    const { CategoryScale, LinearScale, BarController, BarElement, Legend, Title, Tooltip } = Chart;
    Chart.register(CategoryScale, LinearScale, BarController, BarElement, Legend, Title, Tooltip);
  } catch {}

  // 3ヶ月キー
  const ym = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`;
  const now = new Date();
  const monthKeys = [
    ym(new Date(now.getFullYear(), now.getMonth()-2, 1)),
    ym(new Date(now.getFullYear(), now.getMonth()-1, 1)),
    ym(new Date(now.getFullYear(), now.getMonth(),   1)),
  ];
  const LABELS = ["先々月","先月","今月"];
  const CATS   = ["バックエンド","フロントエンド","インフラ"]; // DBのカテゴリ名と完全一致させる

  // 1) APIを3回叩く（ログインが要るので same-origin）
  async function fetchMonth(m) {
    const res = await fetch(`/api/learning/chart?month=${encodeURIComponent(m)}`, {
      credentials: "same-origin",
      headers: { "Accept": "application/json" }
    });
    console.info("[CHART] fetch", m, "status:", res.status);
    const text = await res.text();
    try {
      const json = JSON.parse(text);
      return Array.isArray(json) ? json : [];
    } catch {
      console.warn("[CHART] 非JSON応答（ログイン切れ/テンプレ遷移の可能性）:", text.slice(0,200));
      return [];
    }
  }
  const monthsData = await Promise.all(monthKeys.map(fetchMonth));
  console.log("[CHART] monthKeys", monthKeys, "raw:", monthsData);

  // 2) 月→カテゴリ→分 に整形（足りないカテゴリは0）
  const byMonth = {};
  monthKeys.forEach((k,i) => {
    const map = {};
    (monthsData[i]||[]).forEach(r => {
      const name = (r.category ?? r["category"])?.trim();
      const mins = Number(r.totalMinutes ?? r["total_minutes"]);
      if (name) map[name] = Number.isFinite(mins) ? mins : 0;
    });
    byMonth[k] = map;
  });

  // 3) datasets（分のまま）— 表示が10分刻み＆最小100分
  const colors = ["rgb(243, 181, 194)", "#F7D1AA", "#FAE6B5"];
  const datasets = CATS.map((cat, idx) => ({
    label: cat,
    data: monthKeys.map(k => Number(byMonth[k]?.[cat]) || 0),
    backgroundColor: colors[idx] || "rgba(0,0,0,.2)",
    borderWidth: 0
  }));

  console.table(datasets.map(d => ({ label: d.label, [LABELS[0]]: d.data[0], [LABELS[1]]: d.data[1], [LABELS[2]]: d.data[2] })));

  // 4) Y軸：最低100分。最大値はデータの最大値に合わせる（端数OK）
  const rawMax = Math.max(0, ...datasets.flatMap(d => d.data));
  const yMax   = rawMax <= 100 ? 100 : rawMax;
  console.info("[CHART] rawMax =", rawMax, "yMax =", yMax);

  // 既存チャートがあれば破棄してから描画
  const old = Chart.getChart(canvas);
  if (old) old.destroy();

  new Chart(canvas, {
    type: "bar",
    data: { labels: LABELS, datasets },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: "top" },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.dataset.label}: ${Math.round(Number(ctx.parsed.y))}分`
          }
        }
      },
      scales: {
        x: { type: "category", offset: true },
        y: {
          type: "linear",
          beginAtZero: true,
          min: 0,
          max: yMax,
          ticks: {
            stepSize: 10,                 // 10分刻み表示
            callback: (v) => `${v}`,
          },
        }
      }
    }
  });

  // ヒント出し（全部0または取りこぼし）
  const allVals = datasets.flatMap(d => d.data);
  if (!allVals.some(v => v > 0)) {
    const catsReturned = Array.from(new Set(monthsData.flatMap(rows => rows.map(r => r.category ?? r["category"])))).join(" / ") || "なし";
    const hint = document.createElement("div");
  }
}
