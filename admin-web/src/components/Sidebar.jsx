import React, { useState } from 'react';
import { 
  LayoutDashboard, Calendar, Ticket, Users, Bell, LogOut, 
  Menu, ChevronLeft, ChevronRight 
} from 'lucide-react';
import { dataService } from '../services/dataService';
import { isFirebaseConfigured } from '../config/firebase';

export default function Sidebar({ activeTab, setActiveTab, onLogout, currentUser }) {
  const [collapsed, setCollapsed] = useState(false);

  const menuItems = [
    { id: 'dashboard', label: 'Tổng quan', icon: LayoutDashboard },
    { id: 'events', label: 'Sự kiện (CRUD)', icon: Calendar },
    { id: 'tickets', label: 'Đơn hàng & Vé', icon: Ticket },
    { id: 'users', label: 'Thành viên', icon: Users },
    { id: 'notifications', label: 'Thông báo', icon: Bell }
  ];

  const handleLogout = async () => {
    if (confirm("Bạn có chắc chắn muốn đăng xuất không?")) {
      await dataService.logout();
      onLogout();
    }
  };

  return (
    <div style={{
      width: collapsed ? '80px' : '260px',
      height: '100vh',
      position: 'sticky',
      top: 0,
      background: 'var(--bg-dark)',
      borderRight: '1px solid var(--border-light)',
      display: 'flex',
      flexDirection: 'column',
      transition: 'var(--transition-smooth)',
      zIndex: 100
    }}>
      {/* Header section */}
      <div style={{
        padding: '24px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: collapsed ? 'center' : 'space-between',
        borderBottom: '1px solid var(--border-light)',
        minHeight: '80px'
      }}>
        {!collapsed && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Ticket size={24} style={{ color: 'var(--primary)' }} />
            <span style={{ fontSize: '1.25rem', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
              EzTicket <span style={{ color: 'var(--primary)', fontSize: '0.75rem', verticalAlign: 'super' }}>PRO</span>
            </span>
          </div>
        )}
        <button 
          onClick={() => setCollapsed(!collapsed)}
          style={{
            border: 'none',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            padding: '4px',
            borderRadius: '4px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'rgba(255,255,255,0.02)'
          }}
        >
          {collapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
        </button>
      </div>

      {/* Profile summary in Sidebar */}
      {!collapsed && (
        <div style={{
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          background: 'rgba(255,255,255,0.01)',
          borderBottom: '1px solid rgba(255,255,255,0.02)'
        }}>
          <img 
            src={currentUser?.avatarUrl || "https://api.dicebear.com/7.x/bottts/svg?seed=admin"} 
            alt="Admin avatar" 
            style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#fff', border: '2px solid var(--primary)' }}
          />
          <div style={{ overflow: 'hidden' }}>
            <h4 style={{ fontSize: '0.85rem', color: '#fff', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
              {currentUser?.fullName || 'Nguyễn Văn Admin'}
            </h4>
            <span style={{ fontSize: '0.75rem', color: 'var(--success)', fontWeight: 600, display: 'block' }}>
              • {isFirebaseConfigured ? 'Live Admin' : 'Sandbox Admin'}
            </span>
          </div>
        </div>
      )}

      {/* Menu items */}
      <nav style={{ padding: '20px 10px', display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                padding: collapsed ? '12px 0' : '12px 16px',
                justifyContent: collapsed ? 'center' : 'flex-start',
                borderRadius: '8px',
                border: 'none',
                background: isActive ? 'var(--primary-gradient)' : 'transparent',
                color: isActive ? '#fff' : 'var(--text-secondary)',
                cursor: 'pointer',
                textAlign: 'left',
                fontWeight: isActive ? 600 : 500,
                fontSize: '0.95rem',
                boxShadow: isActive ? '0 4px 12px var(--primary-glow)' : 'none',
                transition: 'var(--transition-smooth)'
              }}
            >
              <Icon size={20} style={{ flexShrink: 0 }} />
              {!collapsed && <span>{item.label}</span>}
            </button>
          );
        })}
      </nav>

      {/* Footer logout section */}
      <div style={{
        padding: '20px 10px',
        borderTop: '1px solid var(--border-light)'
      }}>
        <button
          onClick={handleLogout}
          style={{
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            padding: collapsed ? '12px 0' : '12px 16px',
            justifyContent: collapsed ? 'center' : 'flex-start',
            borderRadius: '8px',
            border: 'none',
            background: 'rgba(239, 68, 68, 0.05)',
            color: '#fca5a5',
            cursor: 'pointer',
            fontWeight: 500,
            fontSize: '0.95rem',
            transition: 'var(--transition-smooth)'
          }}
          className="logout-button-hover"
        >
          <LogOut size={20} style={{ flexShrink: 0 }} />
          {!collapsed && <span>Đăng Xuất</span>}
        </button>
      </div>

      <style>{`
        .logout-button-hover:hover {
          background: rgba(239, 68, 68, 0.15) !important;
          color: #ef4444 !important;
        }
      `}</style>
    </div>
  );
}
