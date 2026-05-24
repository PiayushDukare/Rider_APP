export default function AdminRoomsPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">LiveKit</span>
      <h2>Rooms & audio streams</h2>
      <p>Monitor LiveKit rooms and keep audio quality within spec.</p>
      <div className="admin-grid" style={{ marginTop: "20px" }}>
        {[
          {
            name: "ridge-west-ops",
            riders: "12",
            quality: "Stable",
          },
          {
            name: "night-signal",
            riders: "7",
            quality: "Jitter",
          },
          {
            name: "coastal-sweep",
            riders: "18",
            quality: "Stable",
          },
        ].map((room) => (
          <div key={room.name} className="admin-kpi">
            <span className="admin-tag">{room.name}</span>
            <strong>{room.riders} riders</strong>
            <span className="admin-muted">Audio: {room.quality}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
