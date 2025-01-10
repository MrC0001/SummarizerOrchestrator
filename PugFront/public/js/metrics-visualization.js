// Define a color palette
const providerColors = {
  Anthropic: 'rgba(75, 192, 100, 0.5)',
  'Hugging Face': 'rgba(255, 103, 1, 0.5)',
  'Vertex AI': 'rgba(54, 162, 235, 0.5)',
  'vLLM': 'rgba(238, 39, 39, 0.5)',
  'Mistral AI': 'rgba(178, 39, 238, 0.5)',
  // Add more providers as needed
};

const defaultColor = 'rgba(153, 102, 255, 0.5)'; // fallback

/**
 * Get the color for a given provider.
 * @param {string} providerName - The name of the provider.
 * @param {string} [type='background'] - The type of color ('background' or 'border').
 * @returns {string} The color for the provider.
 */
function getProviderColor(providerName, type = 'background') {
  const base = providerColors[providerName] || defaultColor;
  return type === 'border' ? base.replace('0.5', '1') : base;
}

// =============== CHART RENDER FUNCTIONS ===============

/**
 * Render a ROUGE bar chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderRougeBarChart(ctx, summaries) {
  const labels = ['ROUGE-1', 'ROUGE-2', 'ROUGE-L'];
  const datasets = summaries.map(s => ({
    label: s.providerName,
    data: [s.metrics.rouge1, s.metrics.rouge2, s.metrics.rougeL],
    backgroundColor: getProviderColor(s.providerName),
    borderColor: getProviderColor(s.providerName, 'border'),
    borderWidth: 1
  }));

  new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets },
    options: { responsive: true, scales: { y: { beginAtZero: true } } }
  });
}

/**
 * Render a BERT radar chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderBertRadarChart(ctx, summaries) {
  const labels = ['Precision', 'Recall', 'F1'];
  const datasets = summaries.map(s => ({
    label: s.providerName,
    data: [s.metrics.bertPrecision, s.metrics.bertRecall, s.metrics.bertF1],
    backgroundColor: getProviderColor(s.providerName),
    borderColor: getProviderColor(s.providerName, 'border'),
    borderWidth: 1
  }));

  new Chart(ctx, {
    type: 'radar',
    data: { labels, datasets },
    options: { responsive: true, scales: { r: { beginAtZero: true } } }
  });
}

/**
 * Render a BLEU doughnut chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderBleuProgressCircle(ctx, summaries) {
  const providerNames = summaries.map(s => s.providerName);
  const bleuScores = summaries.map(s => s.metrics.bleu);

  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: providerNames,
      datasets: [{
        label: 'BLEU',
        data: bleuScores,
        backgroundColor: providerNames.map(name => getProviderColor(name)),
        borderColor: providerNames.map(name => getProviderColor(name, 'border')),
        borderWidth: 1
      }]
    },
    options: { responsive: true, cutout: '70%' }
  });
}

/**
 * Render a METEOR bar chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderMeteorBarChart(ctx, summaries) {
  const labels = ['METEOR'];
  const datasets = summaries.map(s => ({
    label: s.providerName,
    data: [s.metrics.meteor],
    backgroundColor: getProviderColor(s.providerName),
    borderColor: getProviderColor(s.providerName, 'border'),
    borderWidth: 1,
  }));

  new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets },
    options: { responsive: true, scales: { y: { beginAtZero: true } } },
  });
}

/**
 * Render a length ratio line chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderLengthRatioLineChart(ctx, summaries) {
  const labels = summaries.map(s => s.providerName);
  const datasets = [
    {
      label: 'Length Ratio',
      data: summaries.map(s => s.metrics.lengthRatio),
      backgroundColor: 'rgba(75, 192, 192, 0.2)',
      borderColor: 'rgba(75, 192, 192, 1)',
      borderWidth: 2,
      fill: false,
    },
  ];

  new Chart(ctx, {
    type: 'line',
    data: { labels, datasets },
    options: { responsive: true, scales: { y: { beginAtZero: true } } },
  });
}

/**
 * Render a redundancy pie chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderRedundancyPieChart(ctx, summaries) {
  const labels = summaries.map(s => s.providerName);
  const redundancyScores = summaries.map(s => s.metrics.redundancy);

  new Chart(ctx, {
    type: 'pie',
    data: {
      labels,
      datasets: [
        {
          label: 'Redundancy',
          data: redundancyScores,
          backgroundColor: labels.map(name => getProviderColor(name)),
          borderColor: labels.map(name => getProviderColor(name, 'border')),
          borderWidth: 1,
        },
      ],
    },
    options: { responsive: true },
  });
}

/**
 * Render a ROUGE average mini bar chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderRougeAverageChart(ctx, summaries) {
  const labels = summaries.map(s => s.providerName);
  const rougeAverages = summaries.map(
    s =>
      ((s.metrics.rouge1 + s.metrics.rouge2 + s.metrics.rougeL) / 3).toFixed(2)
  );

  const datasets = [
    {
      label: 'ROUGE Average',
      data: rougeAverages,
      backgroundColor: labels.map(name => getProviderColor(name)),
      borderColor: labels.map(name => getProviderColor(name, 'border')),
      borderWidth: 1,
    },
  ];

  new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets },
    options: { responsive: true, scales: { y: { beginAtZero: true } } },
  });
}

/**
 * Render a structural vs. quality scatter plot.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderStructuralVsQualityScatter(ctx, summaries) {
  const data = summaries.map(s => ({
    x: s.metrics.lengthRatio,
    y: s.metrics.redundancy,
    label: s.providerName,
    backgroundColor: getProviderColor(s.providerName),
    borderColor: getProviderColor(s.providerName, 'border'),
    borderWidth: 1,
  }));

  new Chart(ctx, {
    type: 'scatter',
    data: {
      datasets: data.map(d => ({
        label: d.label,
        data: [{ x: d.x, y: d.y }],
        backgroundColor: d.backgroundColor,
        borderColor: d.borderColor,
        borderWidth: d.borderWidth,
      })),
    },
    options: {
      responsive: true,
      scales: {
        x: { title: { display: true, text: 'Length Ratio' }, beginAtZero: true },
        y: { title: { display: true, text: 'Redundancy' }, beginAtZero: true },
      },
    },
  });
}

/**
 * Render a quality composite gauge chart.
 * @param {CanvasRenderingContext2D} ctx - The canvas rendering context.
 * @param {Array} summaries - The summaries data.
 */
function renderQualityCompositeGauge(ctx, summaries) {
  const compositeScores = summaries.map(
    s =>
      (
        0.5 * (s.metrics.bertF1 || 0) +
        0.3 * (s.metrics.bleu || 0) +
        0.2 * (s.metrics.meteor || 0)
        
      ).toFixed(2)
  );

  const labels = summaries.map(s => s.providerName);

  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [
        {
          label: 'Quality Composite',
          data: compositeScores,
          backgroundColor: labels.map(name => getProviderColor(name)),
          borderColor: labels.map(name => getProviderColor(name, 'border')),
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      plugins: {
        tooltip: {
          callbacks: {
            label: context =>
              `${context.label}: ${context.raw} (Weighted Composite)`,
          },
        },
      },
      cutout: '70%',
    },
  });
}

/**
 * Render all charts with the provided summaries.
 * @param {Array} summaries - The summaries data used for rendering charts.
 */
function renderAllCharts(summaries) {
  const chartElements = {
    rougeBarEl: document.getElementById('rouge-bar-chart'),
    bertRadarEl: document.getElementById('bert-radar-chart'),
    bleuDoughnutEl: document.getElementById('bleu-progress-circle'),
    meteorBarEl: document.getElementById('meteor-bar-chart'),
    lengthRatioLineEl: document.getElementById('length-ratio-line-chart'),
    redundancyPieEl: document.getElementById('redundancy-pie-chart'),
    rougeAverageBarEl: document.getElementById('rouge-average-bar-chart'),
    structuralQualityScatterEl: document.getElementById('length-vs-redundancy-scatter'),
    qualityCompositeGaugeEl: document.getElementById('quality-composite-gauge')
  };

  if (chartElements.rougeBarEl) renderRougeBarChart(chartElements.rougeBarEl.getContext('2d'), summaries);
  if (chartElements.bertRadarEl) renderBertRadarChart(chartElements.bertRadarEl.getContext('2d'), summaries);
  if (chartElements.bleuDoughnutEl) renderBleuProgressCircle(chartElements.bleuDoughnutEl.getContext('2d'), summaries);
  if (chartElements.meteorBarEl) renderMeteorBarChart(chartElements.meteorBarEl.getContext('2d'), summaries);
  if (chartElements.lengthRatioLineEl) renderLengthRatioLineChart(chartElements.lengthRatioLineEl.getContext('2d'), summaries);
  if (chartElements.redundancyPieEl) renderRedundancyPieChart(chartElements.redundancyPieEl.getContext('2d'), summaries);
  if (chartElements.rougeAverageBarEl) renderRougeAverageChart(chartElements.rougeAverageBarEl.getContext('2d'), summaries);
  if (chartElements.structuralQualityScatterEl) renderStructuralVsQualityScatter(chartElements.structuralQualityScatterEl.getContext('2d'), summaries);
  if (chartElements.qualityCompositeGaugeEl) renderQualityCompositeGauge(chartElements.qualityCompositeGaugeEl.getContext('2d'), summaries);
}

// =============== MAIN DOMContentLoaded ===============
document.addEventListener('DOMContentLoaded', () => {
  const summaryBoxes = document.querySelectorAll('.summary-box');

  const summaries = Array.from(summaryBoxes)
    .map(box => ({
      providerName: box.getAttribute('data-providerName'),
      metrics: {
        rouge1: parseFloat(box.getAttribute('data-rouge1')) || 0,
        rouge2: parseFloat(box.getAttribute('data-rouge2')) || 0,
        rougeL: parseFloat(box.getAttribute('data-rougeL')) || 0,
        bertPrecision: parseFloat(box.getAttribute('data-bertPrecision')) || 0,
        bertRecall: parseFloat(box.getAttribute('data-bertRecall')) || 0,
        bertF1: parseFloat(box.getAttribute('data-bertF1')) || 0,
        bleu: parseFloat(box.getAttribute('data-bleu')) || 0,
        meteor: parseFloat(box.getAttribute('data-meteor')) || 0,
        lengthRatio: parseFloat(box.getAttribute('data-lengthRatio')) || 0,
        redundancy: parseFloat(box.getAttribute('data-redundancy')) || 0,
        createdAt: box.getAttribute('data-createdAt') || 'N/A'
      }
    }))
    .filter(s => s.providerName !== null);

  if (!summaries.length) return;

  renderAllCharts(summaries);
});
