"use client";

import { useState } from "react";

type UserRole = "Customer" | "Admin";

type UserRow = {
  id: string;
  name: string;
  handle: string;
  role: UserRole;
  status: string;
  lastActive: string;
};

const initialUsers: UserRow[] = [
  {
    id: "1",
    name: "Maya Dobrev",
    handle: "@mayad",
    role: "Admin",
    status: "Verified",
    lastActive: "2 min ago",
  },
  {
    id: "2",
    name: "Rafi Khan",
    handle: "@rafik",
    role: "Customer",
    status: "Active",
    lastActive: "10 min ago",
  },
  {
    id: "3",
    name: "Nora Patel",
    handle: "@norap",
    role: "Customer",
    status: "Invited",
    lastActive: "1 hr ago",
  },
  {
    id: "4",
    name: "Diego Ramos",
    handle: "@diegor",
    role: "Admin",
    status: "Active",
    lastActive: "5 min ago",
  },
];

export default function AdminUsersPage() {
  const [users, setUsers] = useState<UserRow[]>(initialUsers);

  const updateRole = (id: string, role: UserRole) => {
    setUsers((prev) =>
      prev.map((user) => (user.id === id ? { ...user, role } : user))
    );
  };

  return (
    <section className="admin-card">
      <span className="admin-section-pill">People</span>
      <h2>All users</h2>
      <p>Switch roles instantly to move riders between Customer and Admin.</p>
      <table className="admin-table" style={{ marginTop: "20px" }}>
        <thead>
          <tr>
            <th>User</th>
            <th>Status</th>
            <th>Role</th>
            <th>Last active</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>
                <strong>{user.name}</strong>
                <div style={{ color: "rgba(242, 230, 211, 0.6)" }}>
                  {user.handle}
                </div>
              </td>
              <td>
                <span className="admin-tag">{user.status}</span>
              </td>
              <td>
                <div className="admin-role-switch">
                  {["Customer", "Admin"].map((role) => (
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
              <td>{user.lastActive}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
