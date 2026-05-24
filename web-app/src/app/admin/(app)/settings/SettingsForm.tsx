"use client";

import { useState } from "react";

type SettingsPayload = {
  alertThreshold: number | null;
  latencyTargetMs: number | null;
  policyNote: string;
};

type SettingsFormProps = {
  initialSettings: SettingsPayload | null;
};

export default function SettingsForm({ initialSettings }: SettingsFormProps) {
  const [alertThreshold, setAlertThreshold] = useState<string>(
    initialSettings?.alertThreshold?.toString() ?? ""
  );
  const [latencyTargetMs, setLatencyTargetMs] = useState<string>(
    initialSettings?.latencyTargetMs?.toString() ?? ""
  );
  const [policyNote, setPolicyNote] = useState<string>(
    initialSettings?.policyNote ?? ""
  );
  const [message, setMessage] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSaving(true);
    setMessage(null);

    const payload: SettingsPayload = {
      alertThreshold: alertThreshold ? Number(alertThreshold) : null,
      latencyTargetMs: latencyTargetMs ? Number(latencyTargetMs) : null,
      policyNote,
    };

    const response = await fetch("/api/admin/settings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const body = await response.json().catch(() => null);
      setMessage(body?.error ?? "Failed to save settings.");
    } else {
      setMessage("Settings saved.");
    }

    setIsSaving(false);
  };

  return (
    <form className="admin-form" style={{ marginTop: "20px" }} onSubmit={handleSubmit}>
      <div className="admin-field">
        <label htmlFor="alert-level">Alert escalation threshold</label>
        <input
          id="alert-level"
          name="alert-level"
          type="number"
          min={1}
          value={alertThreshold}
          onChange={(event) => setAlertThreshold(event.target.value)}
          placeholder=""
        />
      </div>
      <div className="admin-field">
        <label htmlFor="latency-target">Audio latency target (ms)</label>
        <input
          id="latency-target"
          name="latency-target"
          type="number"
          min={0}
          value={latencyTargetMs}
          onChange={(event) => setLatencyTargetMs(event.target.value)}
          placeholder=""
        />
      </div>
      <div className="admin-field">
        <label htmlFor="policy">Incident policy note</label>
        <textarea
          id="policy"
          name="policy"
          value={policyNote}
          onChange={(event) => setPolicyNote(event.target.value)}
        />
      </div>
      <button className="admin-button admin-button--primary" type="submit">
        {isSaving ? "Saving..." : "Save settings"}
      </button>
      {message ? (
        <p className={message.includes("Failed") ? "admin-error" : "admin-muted"} style={{ marginTop: "12px" }}>
          {message}
        </p>
      ) : null}
    </form>
  );
}
