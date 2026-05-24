export default function AdminLoginPage() {
  return (
    <div className="admin-auth__panel">
      <div className="admin-auth__hero">
        <span className="admin-section-pill">Rider Voice Admin</span>
        <h1>Command access for live convoy operations.</h1>
        <p>
          Secure the crew, monitor LiveKit rooms, and keep rides running clean.
          This console is designed for high focus and fast decisions.
        </p>
        <div className="admin-auth__stat">
          <span>Active convoys</span>
          <strong>18 in motion</strong>
        </div>
        <div className="admin-auth__stat">
          <span>Last incident</span>
          <strong>Resolved 14 min ago</strong>
        </div>
      </div>
      <div className="admin-card">
        <h2>Operator sign-in</h2>
        <form className="admin-form">
          <div className="admin-field">
            <label htmlFor="email">Email address</label>
            <input id="email" name="email" type="email" placeholder="ops@ridervoice.io" />
          </div>
          <div className="admin-field">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" placeholder="••••••••" />
          </div>
          <div className="admin-field">
            <label htmlFor="code">Access key</label>
            <input id="code" name="code" type="text" placeholder="RV-ADMIN-XXXX" />
          </div>
          <button className="admin-button admin-button--primary" type="submit">
            Enter command console
          </button>
          <button className="admin-button admin-button--ghost" type="button">
            Reset access
          </button>
        </form>
        <p style={{ marginTop: "16px" }} className="admin-muted">
          This UI expects a Firebase Auth login flow to call
          <strong> /api/admin/session</strong> with an ID token, which sets the
          secure session cookie for admin access.
        </p>
      </div>
    </div>
  );
}
