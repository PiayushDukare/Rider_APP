"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { signInWithEmailAndPassword } from "firebase/auth";
import { firebaseAuth } from "@/utils/firebase/client";

export default function AdminLoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [accessKey, setAccessKey] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!firebaseAuth) {
      setError("Missing Firebase client configuration.");
      return;
    }

    try {
      setIsLoading(true);
      const credential = await signInWithEmailAndPassword(
        firebaseAuth,
        email,
        password
      );
      const idToken = await credential.user.getIdToken();

      const response = await fetch("/api/admin/session", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ idToken, accessKey }),
      });

      if (!response.ok) {
        const payload = await response.json().catch(() => null);
        setError(payload?.error ?? "Admin access denied.");
        setIsLoading(false);
        return;
      }

      router.replace("/admin");
    } catch (err) {
      setError("Login failed. Check your credentials and try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = async () => {
    await fetch("/api/admin/logout", { method: "POST" });
    setPassword("");
    setAccessKey("");
    setError("Session cleared. Please sign in again.");
  };

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
        <form className="admin-form" onSubmit={handleSubmit}>
          <div className="admin-field">
            <label htmlFor="email">Email address</label>
            <input
              id="email"
              name="email"
              type="email"
              placeholder="ops@ridervoice.io"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </div>
          <div className="admin-field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>
          <div className="admin-field">
            <label htmlFor="code">Access key</label>
            <input
              id="code"
              name="code"
              type="text"
              placeholder="RV-ADMIN-XXXX"
              value={accessKey}
              onChange={(event) => setAccessKey(event.target.value)}
            />
          </div>
          <button className="admin-button admin-button--primary" type="submit">
            {isLoading ? "Signing in..." : "Enter command console"}
          </button>
          <button
            className="admin-button admin-button--ghost"
            type="button"
            onClick={handleReset}
          >
            Reset access
          </button>
        </form>
        {error ? (
          <p style={{ marginTop: "16px" }} className="admin-error">
            {error}
          </p>
        ) : (
          <p style={{ marginTop: "16px" }} className="admin-muted">
            Sign in with your Firebase account. Admin access is granted only
            when your Supabase role is set to ADMIN.
          </p>
        )}
      </div>
    </div>
  );
}
