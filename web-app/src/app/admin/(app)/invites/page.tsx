export default function AdminInvitesPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">Invitations</span>
      <h2>Invite pipeline</h2>
      <p>Track pending invites and keep onboarding clean.</p>
      <table className="admin-table" style={{ marginTop: "20px" }}>
        <thead>
          <tr>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Sent</th>
          </tr>
        </thead>
        <tbody>
          {[
            { email: "pilot@ridervoice.io", role: "Admin", status: "Sent", sent: "Today" },
            { email: "crew@ridervoice.io", role: "Customer", status: "Opened", sent: "Yesterday" },
            { email: "ridelead@ridervoice.io", role: "Customer", status: "Pending", sent: "2 days ago" },
          ].map((invite) => (
            <tr key={invite.email}
            >
              <td>{invite.email}</td>
              <td>{invite.role}</td>
              <td>
                <span className="admin-tag">{invite.status}</span>
              </td>
              <td>{invite.sent}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
