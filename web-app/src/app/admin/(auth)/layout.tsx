import { Cormorant_Garamond, IBM_Plex_Sans } from "next/font/google";
import "../admin.css";

const adminSerif = Cormorant_Garamond({
  subsets: ["latin"],
  weight: ["400", "600", "700"],
  variable: "--font-admin-serif",
});

const adminSans = IBM_Plex_Sans({
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
  variable: "--font-admin-sans",
});

export default function AdminAuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className={`admin-auth ${adminSerif.variable} ${adminSans.variable}`}>
      <div className="admin-shell__backdrop" aria-hidden="true" />
      {children}
    </div>
  );
}
