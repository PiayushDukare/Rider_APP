export default function AdminRolesPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">Policy</span>
      <h2>Role control matrix</h2>
      <p>Define what each role can access across live rides and operations.</p>
      <div className="admin-grid" style={{ marginTop: "20px" }}>
        <div className="admin-kpi">
          <span className="admin-tag">Admin</span>
          <strong>Full operational access</strong>
          <span className="admin-muted">
            Can manage rides, rooms, alerts, and assign roles.
          </span>
        </div>
        <div className="admin-kpi">
          <span className="admin-tag">Customer</span>
          <strong>Standard rider access</strong>
          <span className="admin-muted">
            Can join rides, manage profile, and view personal stats.
          </span>
        </div>
        <div className="admin-kpi">
          <span className="admin-tag">Guest</span>
          <strong>Limited onboarding</strong>
          <span className="admin-muted">
            Read-only ride briefings until verified by an admin.
          </span>
        </div>
      </div>
    </section>
  );
}
