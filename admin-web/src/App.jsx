import React, { useState, useEffect } from 'react';
import { dataService } from './services/dataService';
import Login from './components/Login';
import Sidebar from './components/Sidebar';
import Dashboard from './components/Dashboard';
import Events from './components/Events';
import Tickets from './components/Tickets';
import Users from './components/Users';
import Notifications from './components/Notifications';
import { isFirebaseConfigured } from './config/firebase';
import ErrorBoundary from './components/ErrorBoundary';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [loading, setLoading] = useState(true);
  
  // Realtime Database lists
  const [events, setEvents] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [users, setUsers] = useState([]);

  // ----------------------------------------------------
  // 🔐 LISTEN TO AUTH STATE
  // ----------------------------------------------------
  useEffect(() => {
    const unsub = dataService.subscribeToAuthState((user) => {
      setCurrentUser(user);
      setLoading(false);
    });
    return () => unsub();
  }, []);

  // ----------------------------------------------------
  // 📡 LISTEN TO REALTIME DATA STREAMS WHEN AUTHENTICATED
  // ----------------------------------------------------
  useEffect(() => {
    if (!currentUser) {
      // Clear data states on logout
      setEvents([]);
      setTickets([]);
      setUsers([]);
      return;
    }

    // Subscribe to events
    const unsubEvents = dataService.subscribeEvents(
      (list) => setEvents(list),
      (err) => console.error("Events stream error: ", err)
    );

    // Subscribe to purchased tickets
    const unsubTickets = dataService.subscribeTickets(
      (list) => setTickets(list),
      (err) => console.error("Tickets stream error: ", err)
    );

    // Subscribe to user profiles
    const unsubUsers = dataService.subscribeUsers(
      (list) => setUsers(list),
      (err) => console.error("Users stream error: ", err)
    );

    // Cleanup listeners
    return () => {
      unsubEvents();
      unsubTickets();
      unsubUsers();
    };
  }, [currentUser]);

  // Handle loading screen
  if (loading) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'var(--bg-deep)',
        color: 'var(--text-secondary)'
      }}>
        {/* Simple spinning glowing circle */}
        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '50%',
          border: '3px solid rgba(99, 102, 241, 0.1)',
          borderTopColor: 'var(--primary)',
          animation: 'spin 1s linear infinite',
          boxShadow: '0 0 15px var(--primary-glow)',
          marginBottom: '20px'
        }}></div>
        <span style={{ fontSize: '0.95rem', letterSpacing: '0.05em', fontFamily: 'var(--font-display)' }}>
          ĐANG TẢI DỮ LIỆU...
        </span>
        <style>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  // If not logged in, show beautiful Login page
  if (!currentUser) {
    return (
      <Login onLoginSuccess={(user) => setCurrentUser(user)} />
    );
  }

  // Render proper components based on activeTab
  const renderTabContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <Dashboard events={events} tickets={tickets} users={users} />;
      case 'events':
        return <Events events={events} />;
      case 'tickets':
        return <Tickets tickets={tickets} />;
      case 'users':
        return <Users users={users} currentUser={currentUser} />;
      case 'notifications':
        return <Notifications events={events} />;
      default:
        return <Dashboard events={events} tickets={tickets} users={users} />;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'var(--bg-deep)' }}>
      {/* Sidebar navigation */}
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        onLogout={async () => {
          await dataService.logout();
          setCurrentUser(null);
        }} 
        currentUser={currentUser}
      />

      {/* Main app panel area */}
      <main style={{ 
        flex: 1, 
        height: '100vh', 
        overflowY: 'auto',
        position: 'relative'
      }}>
        {/* Floating background neon gradients */}
        <div style={{
          position: 'absolute',
          top: '10%',
          right: '5%',
          width: '350px',
          height: '350px',
          background: 'rgba(99, 102, 241, 0.04)',
          filter: 'blur(100px)',
          borderRadius: '50%',
          pointerEvents: 'none',
          zIndex: 0
        }}></div>
        <div style={{
          position: 'absolute',
          bottom: '15%',
          left: '10%',
          width: '400px',
          height: '400px',
          background: 'rgba(139, 92, 246, 0.03)',
          filter: 'blur(120px)',
          borderRadius: '50%',
          pointerEvents: 'none',
          zIndex: 0
        }}></div>

        {/* Content area wrapper */}
        <div style={{ position: 'relative', zIndex: 1 }}>
          <ErrorBoundary>
            {renderTabContent()}
          </ErrorBoundary>
        </div>
      </main>
    </div>
  );
}
