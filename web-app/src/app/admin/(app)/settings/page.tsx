export default function AdminSettingsPage() {
  return (
    <section className="admin-card">
      <span className="admin-section-pill">Preferences</span>
      <h2>Operational settings</h2>
      <p>Adjust alerts, room behavior, and service targets.</p>
      <form className="admin-form" style={{ marginTop: "20px" }}>
        <div className="admin-field">
          <label htmlFor="alert-level">Alert escalation threshold</label>
          <select id="alert-level" name="alert-level">
            <option>Standard (3 alerts)</option>
            <option>Strict (2 alerts)</option>
            <option>High tolerance (5 alerts)</option>
          </select>
        </div>
        <div className="admin-field">
          <label htmlFor="latency-target">Audio latency target</label>
          <input
            id="latency-target"
            name="latency-target"
            type="text"
            placeholder="120 ms"
          />
        </div>
        <div className="admin-field">
          <label htmlFor="policy">Incident policy note</label>
          <textarea
            id="policy"
            name="policy"
            placeholder="Keep rider safety and regroup procedures up to date."
          />
        </div>
        <button className="admin-button admin-button--primary" type="submit">
          Save settings
        </button>
      </form>
    </section>
  );
}
