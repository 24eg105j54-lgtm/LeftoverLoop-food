/* ==========================================================================
   LeftoverLoop Shared App Utilities
   ========================================================================== */

function showToast(message, type = 'info') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '❌';
  if (type === 'warning') icon = '⚠️';

  toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function renderStatusBadge(statusColor, text) {
  let badgeClass = 'badge-green';
  const val = String(text || statusColor || '').toUpperCase();
  const color = String(statusColor || '').toUpperCase();

  if (color === 'RED' || val === 'EXPIRED' || val === 'REJECTED' || val === 'CANCELLED' || val === 'EXPIRING SOON') {
    badgeClass = 'badge-red';
  } else if (color === 'ORANGE' || val === 'PENDING' || val === 'PARTIALLY_RESERVED') {
    badgeClass = 'badge-orange';
  } else if (color === 'BLUE' || val === 'ACCEPTED' || val === 'READY_FOR_COLLECTION' || val === 'READY') {
    badgeClass = 'badge-blue';
  } else if (color === 'PURPLE' || val === 'COLLECTED' || val === 'COMPLETED') {
    badgeClass = 'badge-purple';
  } else if (color === 'GREEN' || val === 'AVAILABLE') {
    badgeClass = 'badge-green';
  }

  return `<span class="badge ${badgeClass}">${text || statusColor}</span>`;
}

function formatDate(dateString) {
  if (!dateString) return 'N/A';
  const d = new Date(dateString);
  return d.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function getTimeRemainingText(expiryDateString) {
  if (!expiryDateString) return 'No expiry';
  const expiry = new Date(expiryDateString).getTime();
  const now = new Date().getTime();
  const diff = expiry - now;

  if (diff <= 0) return 'EXPIRED';

  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

  if (hours > 24) {
    const days = Math.floor(hours / 24);
    return `${days} day${days > 1 ? 's' : ''} left`;
  }

  return `${hours}h ${minutes}m left`;
}

function getExpiryAlertBadge(expiryDateString) {
  if (!expiryDateString) return '';
  const expiry = new Date(expiryDateString).getTime();
  const now = new Date().getTime();
  const diff = expiry - now;

  if (diff <= 0) {
    return `<span class="badge badge-red" style="font-size: 0.75rem;">⛔ Expired</span>`;
  }

  const hours = diff / (1000 * 60 * 60);
  if (hours <= 4) {
    return `<span class="badge badge-red" style="font-size: 0.75rem;">⚠️ Urgent: < 4h left</span>`;
  }
  if (hours <= 12) {
    return `<span class="badge badge-orange" style="font-size: 0.75rem;">⚠️ Expiring Today</span>`;
  }
  if (hours <= 24) {
    return `<span class="badge badge-orange" style="font-size: 0.75rem;">⏰ < 24h left</span>`;
  }
  return '';
}

function getFoodRiskInfo(item) {
  if (!item) return { level: 'LOW', label: 'Low Risk', badgeClass: 'badge-green', reason: 'Fresh batch', disclaimer: 'Informational check' };

  const now = new Date().getTime();
  const expiry = item.expiryDate ? new Date(item.expiryDate).getTime() : null;
  const hoursLeft = expiry ? (expiry - now) / (1000 * 60 * 60) : 999;

  let level = 'LOW';
  let label = 'Low Risk';
  let badgeClass = 'badge-green';
  let reasons = [];

  const category = (item.category || '').toLowerCase();
  const instructions = (item.pickupInstructions || '').toLowerCase();
  const isPerishable = category.includes('prepared') || category.includes('dairy') || category.includes('produce') || category.includes('bakery');

  if (hoursLeft <= 0 || item.expired || item.status === 'EXPIRED') {
    level = 'HIGH';
    label = 'Expired';
    badgeClass = 'badge-red';
    reasons.push('Item has passed its posted expiry threshold.');
  } else if (hoursLeft <= 4) {
    level = 'HIGH';
    label = 'High Urgency';
    badgeClass = 'badge-red';
    reasons.push(`Expiring in less than 4 hours (${Math.max(1, Math.round(hoursLeft))}h remaining).`);
  } else if (hoursLeft <= 12) {
    level = 'MEDIUM';
    label = 'Medium Risk';
    badgeClass = 'badge-orange';
    reasons.push(`Expiring within 12 hours (${Math.round(hoursLeft)}h remaining).`);
  } else if (hoursLeft <= 24) {
    level = 'MEDIUM';
    label = 'Medium Risk';
    badgeClass = 'badge-orange';
    reasons.push('Expiring within 24 hours.');
  } else {
    reasons.push('Ample shelf life remaining.');
  }

  if (isPerishable && !instructions.includes('refrigerat') && !instructions.includes('cold') && !instructions.includes('chill') && !instructions.includes('cool')) {
    if (level === 'LOW') {
      level = 'MEDIUM';
      label = 'Medium Risk';
      badgeClass = 'badge-orange';
    }
    reasons.push('Perishable item without explicit cold storage notes.');
  }

  return {
    level,
    label,
    badgeClass,
    reason: reasons.join(' • '),
    disclaimer: 'Informational guidance only; inspect food safety prior to consumption.'
  };
}

function generateQrCodeUrl(dataText) {
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(dataText)}&color=10B981&bgcolor=1E293B`;
}
