"use client";

import { useState } from "react";

type UserRole = "ADMIN" | "CUSTOMER";

type UserRow = {
  id: string;
  email: string | null;
  handle: string | null;
  displayName: string | null;
  role: UserRole | null;
  createdAt: string | null;
};

type UsersTableProps = {
  initialUsers: UserRow[];
};

export default function UsersTable({ initialUsers }: UsersTableProps) {
  const [users, setUsers] = useState<UserRow[]>(initialUsers);
  const [error, setError] = useState<string | null>(null);

  const updateRole = async (id: string, role: UserRole) => {
    setError(null);
    const previous = users;
    setUsers((prev) => prev.map((user) => (user.id === id ? { ...user, role } : user)));

    const response = await fetch("/api/admin/users/role", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, role }),
    });

    if (!response.ok) {
      setUsers(previous);
      const payload = await response.json().catch(() => null);
      setError(payload?.error ?? "Failed to update role.");
    }
  };

  if (!users.length) {
    return <div className="admin-empty">No users found yet.</div>;
  }

  return (
    <>
      {error ? <div className="admin-error" style={{ marginBottom: "16px" }}>{error}</div> : null}
      <table className="admin-table">
        <thead>
          <tr>
            <th>User</th>
            <th>Role</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>
                <strong>{user.displayName ?? user.email ?? user.id}</strong>
                <div style={{ color: "rgba(242, 230, 211, 0.6)" }}>
                  {user.handle ?? user.email ?? ""}
                </div>
              </td>
              <td>
                <div className="admin-role-switch">
                  {["CUSTOMER", "ADMIN"].map((role) => (
                    <button
                      key={`${user.id}-${role}`}
                      type="button"
                      aria-pressed={user.role === role}
                      onClick={() => updateRole(user.id, role as UserRole)}
                    >
                      {role}
                    </button>
                  ))}
                </div>
              </td>
              <td>
                {user.createdAt
                  ? new Date(user.createdAt).toLocaleString()
                  : "—"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
