/* ==========================================================================
   LeftoverLoop Registration JavaScript Handler
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  if (AuthManager.isAuthenticated()) {
    const user = AuthManager.getUser();
    AuthManager.redirectToUserDashboard(user.role);
    return;
  }

  const form = document.getElementById('registerForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fullName = document.getElementById('regFullName').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value.trim();
    const role = document.getElementById('regRole').value;
    const organizationName = document.getElementById('regOrgName').value.trim();
    const phone = document.getElementById('regPhone').value.trim();
    const address = document.getElementById('regAddress').value.trim();

    const submitBtn = document.getElementById('btnRegisterSubmit');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering Account...';

    try {
      const res = await ApiClient.register({
        fullName,
        email,
        password,
        role,
        organizationName,
        phone,
        address
      });

      ApiClient.setToken(res.token);
      AuthManager.setUser(res.user);
      showToast(`Welcome to LeftoverLoop, ${res.user.fullName}!`, 'success');

      setTimeout(() => {
        AuthManager.redirectToUserDashboard(res.user.role);
      }, 1000);
    } catch (err) {
      showToast(err.message || 'Registration failed', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Complete Registration';
    }
  });
});
