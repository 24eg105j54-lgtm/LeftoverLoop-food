/* ==========================================================================
   LeftoverLoop Receiver Portal JavaScript
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  loadReceiverListings();

  document.getElementById('requestFoodForm').addEventListener('submit', handleFoodRequestSubmit);
  document.getElementById('aiMatchForm').addEventListener('submit', handleAiMatchSubmit);
});

let allListings = [];

async function loadReceiverListings() {
  try {
    const category = document.getElementById('filterCategory').value;
    const location = document.getElementById('filterLocation').value.trim();

    allListings = await ApiClient.getActiveDonations(category, location);
    renderReceiverListings(allListings);
  } catch (err) {
    showToast(err.message || 'Failed to load food listings', 'error');
  }
}

function applyFilters() {
  loadReceiverListings();
}

function renderReceiverListings(listings) {
  const grid = document.getElementById('receiverListingsGrid');
  if (!listings || listings.length === 0) {
    grid.innerHTML = '<div class="glass-card" style="grid-column: 1/-1; text-align: center; padding: 3rem;"><p style="color: var(--text-muted);">No active food donations found matching your filters.</p></div>';
    return;
  }

  const sortVal = document.getElementById('filterSort').value;
  if (sortVal === 'quantity') {
    listings.sort((a, b) => b.remainingQuantity - a.remainingQuantity);
  } else {
    listings.sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate));
  }

  grid.innerHTML = listings.map(item => {
    const risk = typeof getFoodRiskInfo === 'function' ? getFoodRiskInfo(item) : null;
    const expiryAlert = typeof getExpiryAlertBadge === 'function' ? getExpiryAlertBadge(item.expiryDate) : '';

    return `
    <div class="glass-card donation-card">
      <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1555244162-803834f70033?w=600'}" class="donation-image" alt="${item.title}" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1555244162-803834f70033?w=600';">
      <div class="donation-header">
        <h3 class="donation-title">${item.title}</h3>
        ${renderStatusBadge(item.statusColor, item.statusColor === 'RED' ? 'EXPIRING SOON' : item.status)}
      </div>
      <div class="donation-meta">
        <div class="donation-meta-item">📍 ${item.location}</div>
        <div class="donation-meta-item">🕒 Expiry: ${formatDate(item.expiryDate)} (${getTimeRemainingText(item.expiryDate)}) ${expiryAlert}</div>
        <div class="donation-meta-item">🏷️ Category: ${item.category} • Posted by ${item.donorOrganization || item.donorName}</div>
        ${risk ? `
          <div style="margin-top: 0.4rem;">
            <span class="badge ${risk.badgeClass}" title="${risk.disclaimer}" style="font-size: 0.75rem;">🛡️ ${risk.label}: ${risk.reason}</span>
          </div>
        ` : ''}
      </div>
      <div class="quantity-bar">
        <div class="quantity-fill" style="width: ${(item.remainingQuantity / item.initialQuantity) * 100}%;"></div>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.9rem; margin-bottom: 0.75rem;">
        <span style="font-weight: 700; color: var(--accent-green);">${item.remainingQuantity} / ${item.initialQuantity} ${item.unit} AVAILABLE</span>
      </div>
      <div style="display: flex; gap: 0.5rem;">
        <a href="food-details.html?id=${item.id}" class="btn btn-secondary" style="flex: 1; font-size: 0.85rem;">Details</a>
        <button onclick="openRequestModal(${item.id})" class="btn btn-primary" style="flex: 1.5; font-size: 0.85rem;">Request Food</button>
      </div>
    </div>
  `;
  }).join('');
}

function openRequestModal(donationId) {
  if (!AuthManager.isAuthenticated()) {
    showToast('Please login to request food donations', 'warning');
    setTimeout(() => window.location.href = 'login.html', 1000);
    return;
  }

  const item = allListings.find(d => d.id === donationId);
  if (!item) return;

  document.getElementById('reqDonationId').value = item.id;
  document.getElementById('reqModalTitle').textContent = `Request ${item.title}`;
  document.getElementById('reqModalDetails').innerHTML = `
    Category: <strong>${item.category}</strong> | Location: <strong>${item.location}</strong>
  `;
  document.getElementById('reqAvailableText').textContent = `${item.remainingQuantity} ${item.unit}`;
  document.getElementById('reqQuantity').max = item.remainingQuantity;
  document.getElementById('reqQuantity').value = Math.min(5, item.remainingQuantity);

  document.getElementById('requestModal').classList.add('active');
}

function closeRequestModal() {
  document.getElementById('requestModal').classList.remove('active');
}

async function handleFoodRequestSubmit(e) {
  e.preventDefault();
  const donationId = parseInt(document.getElementById('reqDonationId').value);
  const quantity = parseInt(document.getElementById('reqQuantity').value);
  const notes = document.getElementById('reqNotes').value.trim();

  const submitBtn = document.getElementById('btnSubmitRequest');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Submitting Request...';

  try {
    const res = await ApiClient.createRequest(donationId, quantity, notes);
    showToast(`Request submitted! Collection Code: ${res.collectionCode}`, 'success');
    closeRequestModal();
    loadReceiverListings();

    setTimeout(() => {
      window.location.href = `receipt.html?id=${res.id}`;
    }, 1200);
  } catch (err) {
    showToast(err.message || 'Request failed', 'error');
    submitBtn.disabled = false;
    submitBtn.textContent = 'Submit Request';
  }
}

function openAiMatchModal() {
  document.getElementById('aiMatchModal').classList.add('active');
}

function closeAiMatchModal() {
  document.getElementById('aiMatchModal').classList.remove('active');
}

async function handleAiMatchSubmit(e) {
  e.preventDefault();
  const category = document.getElementById('aiCategory').value;
  const quantity = parseInt(document.getElementById('aiQuantity').value);
  const receiverType = document.getElementById('aiReceiverType').value;
  const location = document.getElementById('filterLocation').value.trim();

  const resultsDiv = document.getElementById('aiResultsContainer');
  resultsDiv.innerHTML = '<p style="color: var(--text-muted); text-align: center; padding: 1.5rem;">🤖 AI Engine Analyzing listings and running match algorithm...</p>';

  try {
    const res = await ApiClient.getAiRecommendations(category, location, quantity, receiverType);
    
    let html = `
      <div style="background: rgba(139, 92, 246, 0.15); border: 1px solid rgba(139, 92, 246, 0.4); padding: 0.85rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.85rem;">
        <div><strong>Engine:</strong> ${res.modelUsed} ${res.liveCloudAi ? '🟢 [Cloud AI Live Mode]' : '🔵 [Smart Match Algorithm]'}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem;">${res.summaryRationale}</div>
      </div>
    `;

    if (!res.recommendations || res.recommendations.length === 0) {
      html += '<p style="color: var(--text-muted);">No recommendations found.</p>';
    } else {
      html += res.recommendations.map(rec => `
        <div style="background: rgba(15, 23, 42, 0.6); border: 1px solid var(--glass-border); padding: 0.85rem; border-radius: 8px; margin-bottom: 0.75rem; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-weight: 700; font-size: 1rem;">${rec.donation.title} <span class="badge badge-green" style="margin-left: 0.4rem;">${rec.matchScore}% AI Match</span></div>
            <div style="font-size: 0.8rem; color: var(--accent-green); margin-top: 0.2rem;">💡 ${rec.matchReason}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">${rec.donation.remainingQuantity} ${rec.donation.unit} available at ${rec.donation.location}</div>
          </div>
          <button onclick="closeAiMatchModal(); openRequestModal(${rec.donation.id});" class="btn btn-primary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;">Request</button>
        </div>
      `).join('');
    }

    resultsDiv.innerHTML = html;
  } catch (err) {
    resultsDiv.innerHTML = `<p style="color: var(--accent-red);">AI Match Error: ${err.message}</p>`;
  }
}
