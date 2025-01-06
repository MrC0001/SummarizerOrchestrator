// Define a color palette
const providerColors = {
  Anthropic: 'rgba(75, 192, 100, 0.5)',      // green
  'Hugging Face': 'rgba(255, 103, 1, 0.5)', // red
  'Vertex AI': 'rgba(54, 162, 235, 0.5)',    // blue
  // Add more providers as needed
};

const defaultColor = 'rgba(153, 102, 255, 0.5)'; // fallback

function getProviderColor(providerName, type = 'background') {
  const base = providerColors[providerName] || defaultColor;
  return type === 'border' ? base.replace('0.5', '1') : base;
}

// =============== CHART RENDER FUNCTIONS ===============

// ROUGE
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

// BERT Radar
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

// BLEU Doughnut
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

// METEOR Bar Chart
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

// Length Ratio Line Chart
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

// Redundancy Pie Chart
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

// ROUGE Average Mini Bar Chart
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

// Structural vs. Quality Scatter Plot
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

// Quality Composite Gauge Chart
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




// =============== MAIN DOMContentLoaded ===============
document.addEventListener('DOMContentLoaded', () => {
  // Updated to .summary-box
  const summaryBoxes = document.querySelectorAll('.summary-box');

  // Build the summaries array from data attributes and filter out invalid entries
  const summaries = Array.from(summaryBoxes)
    .map(box => ({
      providerName: box.getAttribute('data-providerName'),
      metrics: {
        rouge1:        parseFloat(box.getAttribute('data-rouge1')) || 0,
        rouge2:        parseFloat(box.getAttribute('data-rouge2')) || 0,
        rougeL:        parseFloat(box.getAttribute('data-rougeL')) || 0,
        bertPrecision: parseFloat(box.getAttribute('data-bertPrecision')) || 0,
        bertRecall:    parseFloat(box.getAttribute('data-bertRecall')) || 0,
        bertF1:        parseFloat(box.getAttribute('data-bertF1')) || 0,
        bleu:          parseFloat(box.getAttribute('data-bleu')) || 0,
        meteor:        parseFloat(box.getAttribute('data-meteor')) || 0,
        lengthRatio:   parseFloat(box.getAttribute('data-lengthRatio')) || 0,
        redundancy:    parseFloat(box.getAttribute('data-redundancy')) || 0,
        createdAt:     box.getAttribute('data-createdAt') || 'N/A'
      }
    }))
    .filter(s => s.providerName !== null); // Filter out null providers

  // If no summaries, do nothing
  if (!summaries.length) return;

  // Get chart canvases
  const rougeBarEl        = document.getElementById('rouge-bar-chart');
  const bertRadarEl       = document.getElementById('bert-radar-chart');
  const bleuDoughnutEl    = document.getElementById('bleu-progress-circle');
  const meteorBarEl = document.getElementById('meteor-bar-chart');
  const lengthRatioLineEl = document.getElementById('length-ratio-line-chart');
  const redundancyPieEl = document.getElementById('redundancy-pie-chart');
  const rougeAverageBarEl = document.getElementById('rouge-average-bar-chart');
  const structuralQualityScatterEl = document.getElementById('structural-quality-scatter');
  const qualityCompositeGaugeEl = document.getElementById('quality-composite-gauge');

  // Render ROUGE
  if (rougeBarEl) {
    renderRougeBarChart(rougeBarEl.getContext('2d'), summaries);
  }

  // Render BERT
  if (bertRadarEl) {
    renderBertRadarChart(bertRadarEl.getContext('2d'), summaries);
  }

  // Render BLEU
  if (bleuDoughnutEl) {
    renderBleuProgressCircle(bleuDoughnutEl.getContext('2d'), summaries);
  }
    if (meteorBarEl) renderMeteorBarChart(meteorBarEl.getContext('2d'), summaries);
  if (lengthRatioLineEl) renderLengthRatioLineChart(lengthRatioLineEl.getContext('2d'), summaries);
  if (redundancyPieEl) renderRedundancyPieChart(redundancyPieEl.getContext('2d'), summaries);

  if (rougeAverageBarEl) {
    renderRougeAverageChart(rougeAverageBarEl.getContext('2d'), summaries);
  }

  if (structuralQualityScatterEl) {
    renderStructuralVsQualityScatter(structuralQualityScatterEl.getContext('2d'), summaries);
  }

  if (qualityCompositeGaugeEl) {
    renderQualityCompositeGauge(qualityCompositeGaugeEl.getContext('2d'), summaries);
  }
});
