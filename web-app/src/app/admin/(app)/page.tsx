export default function AdminDashboardPage() {
  return (
    <>
      <section className="admin-card">
        <span className="admin-section-pill">Live overview</span>
        <h2>Convoy command overview</h2>
        <p>
          A pulse on active rides, audio rooms, and incident resolution. Keep
          the most critical signals in view and respond fast.
        </p>
        <div className="admin-grid" style={{ marginTop: "20px" }}>
          {[
            { label: "Active rides", value: "24", note: "8 high priority" },
            { label: "Live rooms", value: "16", note: "2 unstable" },
            { label: "Active alerts", value: "3", note: "1 safety" },
            { label: "Dispatch SLA", value: "92%", note: "Last 7 days" },
          ].map((kpi) => (
            <div key={kpi.label} className="admin-kpi">
              <span className="admin-tag">{kpi.label}</span>
              <strong>{kpi.value}</strong>
              <span className="admin-muted">{kpi.note}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="admin-split">
        <div className="admin-card">
          <h2>Live operations board</h2>
          <table className="admin-table">
            <thead>
              <tr>
                <th>Ride</th>
                <th>Status</th>
                <th>Lead</th>
                <th>Members</th>
              </tr>
            </thead>
            <tbody>
              {[
                {
                  ride: "Western Ridge Patrol",
                  status: "In motion",
                  lead: "Maya D.",
                  members: "12",
                },
                {
                  ride: "Night Signal Run",
                  status: "Holding",
                  lead: "Rafi K.",
                  members: "7",
                },
                {
                  ride: "Coastal Sweep",
                  status: "Escorted",
                  lead: "Nora P.",
                  members: "18",
                },
              ].map((row) => (
                <tr key={row.ride}>
                  <td>{row.ride}</td>
                  <td>
                    <span className="admin-tag admin-tag--success">
                      {row.status}
                    </span>
                  </td>
                  <td>{row.lead}</td>
                  <td>{row.members}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="admin-card">
          <h2>Signal health</h2>
          <div className="admin-chart">
            {[30, 55, 42, 70, 52, 90].map((height, index) => (
              <div
                key={`bar-${index}`}
                className="admin-chart__bar"
                style={{ height: `${height}%` }}
              />
            ))}
          </div>
          <p style={{ marginTop: "16px" }}>
            Audio latency holding steady across regions. Two rooms flagged for
            elevated jitter.
          </p>
        </div>
      </section>
    </>
  );
}
