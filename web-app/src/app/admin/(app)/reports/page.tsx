export default function AdminReportsPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">Reports</span>
      <h2>Signal and safety reports</h2>
      <p>Operational summaries for the last 30 days.</p>
      <div className="admin-split" style={{ marginTop: "20px" }}>
        <div className="admin-kpi">
          <span className="admin-tag">Response time</span>
          <strong>8m 14s</strong>
          <span className="admin-muted">Median to resolve incidents</span>
        </div>
        <div className="admin-kpi">
          <span className="admin-tag">Audio uptime</span>
          <strong>99.2%</strong>
          <span className="admin-muted">Across 12 regions</span>
        </div>
        <div className="admin-kpi">
          <span className="admin-tag">Route compliance</span>
          <strong>94%</strong>
          <span className="admin-muted">Based on convoy check-ins</span>
        </div>
      </div>
      <div className="admin-card" style={{ marginTop: "20px" }}>
        <h2>Monthly impact</h2>
        <div className="admin-chart">
          {[45, 62, 58, 80, 72, 90].map((height, index) => (
            <div
              key={`report-bar-${index}`}
              className="admin-chart__bar"
              style={{ height: `${height}%` }}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
