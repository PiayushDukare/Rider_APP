import React from "react";

export default function Home() {
  return (
    <div className="container">
      <nav className="navbar">
        <div className="logo">RIDER APP</div>
        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#hardware">Hardware</a>
          <a href="#download">Download</a>
        </div>
      </nav>

      <main className="hero">
        <div className="hero-glow"></div>
        <h1 className="title">
          TACTICAL COMMS.<br />
          <span className="highlight">NO COMPROMISE.</span>
        </h1>
        <p className="subtitle">
          Connect your squad with ultra-low latency voice, dynamic group tracking, and 
          smart HUD integration. Designed for riders, built for performance.
        </p>
        <div className="cta-group">
          <button className="btn-primary">Download for Android</button>
          <button className="btn-secondary">View Documentation</button>
        </div>
      </main>

      <section id="features" className="features">
        <div className="feature-card">
          <div className="feature-icon">🎙️</div>
          <h3 className="feature-title">Smart VOX Engine</h3>
          <p className="feature-desc">
            Our proprietary noise-canceling VOX engine dynamically adjusts to wind and 
            engine noise, ensuring your mic only opens when you speak.
          </p>
        </div>
        
        <div className="feature-card">
          <div className="feature-icon">🌐</div>
          <h3 className="feature-title">Unlimited Range</h3>
          <p className="feature-desc">
            Break free from Bluetooth mesh limits. Powered by LiveKit infrastructure, 
            stay connected with your convoy whether they are 50 feet or 50 miles away.
          </p>
        </div>

        <div className="feature-card">
          <div className="feature-icon">📍</div>
          <h3 className="feature-title">Real-Time Telemetry</h3>
          <p className="feature-desc">
            Instantly see where your squad is. Share locations, drop hazard pins, and 
            never lose a rider at a red light again.
          </p>
        </div>
      </section>

      <footer className="footer">
        <p>&copy; {new Date().getFullYear()} Rider APP Project. All rights reserved.</p>
      </footer>
    </div>
  );
}
