/* ==========================================================================
   LeftoverLoop Auth & Session Manager
   ========================================================================== */

class AuthManager {

  static getUser() {
    const userJson = localStorage.getItem('leftoverloop_user');
    return userJson ? JSON.parse(userJson) : null;
  }

  static setUser(user) {
    localStorage.setItem('leftoverloop_user', JSON.stringify(user));
  }

  static isAuthenticated() {
    return !!ApiClient.getToken() && !!this.getUser();
  }

  static logout() {
    ApiClient.clearToken();
    window.location.href = 'login.html';
  }

  static requireAuth(allowedRoles = []) {
    if (!this.isAuthenticated()) {
      window.location.href = 'login.html';
      return false;
    }

    if (allowedRoles.length > 0) {
      const user = this.getUser();
      if (!allowedRoles.includes(user.role)) {
        alert(`Access Denied: Your role (${user.role.replace('ROLE_', '')}) does not have permission for this dashboard.`);
        this.redirectToUserDashboard(user.role);
        return false;
      }
    }
    return true;
  }

  static redirectToUserDashboard(role) {
    switch (role) {
      case 'ROLE_DONOR':
        window.location.href = 'donor.html';
        break;
      case 'ROLE_RECEIVER':
        window.location.href = 'receiver.html';
        break;
      case 'ROLE_ORGANIZATION':
        window.location.href = 'organization.html';
        break;
      case 'ROLE_ADMIN':
        window.location.href = 'admin.html';
        break;
      default:
        window.location.href = 'index.html';
    }
  }

  static updateNavbar() {
    const navAuthContainer = document.getElementById('navAuthContainer');
    if (!navAuthContainer) return;

    if (this.isAuthenticated()) {
      const user = this.getUser();
      const roleName = user.role.replace('ROLE_', '');
      let dashboardLink = 'donor.html';
      if (user.role === 'ROLE_RECEIVER') dashboardLink = 'receiver.html';
      if (user.role === 'ROLE_ORGANIZATION') dashboardLink = 'organization.html';
      if (user.role === 'ROLE_ADMIN') dashboardLink = 'admin.html';

      navAuthContainer.innerHTML = `
        <span class="nav-role-badge badge-green">${roleName}</span>
        <a href="${dashboardLink}" class="nav-link">${user.fullName}</a>
        <button onclick="AuthManager.logout()" class="btn btn-secondary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;">Logout</button>
      `;
    } else {
      navAuthContainer.innerHTML = `
        <a href="login.html" class="nav-link">Login</a>
        <a href="register.html" class="btn btn-primary">Register</a>
      `;
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  AuthManager.updateNavbar();
});
