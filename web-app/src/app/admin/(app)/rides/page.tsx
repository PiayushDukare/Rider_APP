export default function AdminRidesPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">Fleet</span>
      <h2>Ride roster</h2>
      <p>Track convoy status, route discipline, and coordinator assignment.</p>
      <table className="admin-table" style={{ marginTop: "20px" }}>
        <thead>
          <tr>
            <th>Ride</th>
            <th>Route</th>
            <th>Status</th>
            <th>Lead</th>
          </tr>
        </thead>
        <tbody>
          {[
            {
              ride: "Western Ridge Patrol",
              route: "Colfax Loop",
              status: "In motion",
              lead: "Maya D.",
            },
            {
              ride: "Night Signal Run",
              route: "Harbor Cut",
              status: "Holding",
              lead: "Rafi K.",
            },
            {
              ride: "Coastal Sweep",
              route: "Sable Highway",
              status: "Escorted",
              lead: "Nora P.",
            },
          ].map((row) => (
            <tr key={row.ride}>
              <td>{row.ride}</td>
              <td>{row.route}</td>
              <td>
                <span className="admin-tag admin-tag--success">
                  {row.status}
                </span>
              </td>
              <td>{row.lead}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
