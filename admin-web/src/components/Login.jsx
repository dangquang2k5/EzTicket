import React, { useState } from 'react';
import { dataService } from '../services/dataService';
import { Lock, Mail, Ticket, AlertCircle } from 'lucide-react';
import { isFirebaseConfigured } from '../config/firebase';

export default function Login({ onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await dataService.login(email, password);
      onLoginSuccess(user);
    } catch (err) {
      setError(err.message || 'Đã xảy ra lỗi đăng nhập!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      padding: '20px',
      background: 'radial-gradient(circle at top right, rgba(99, 102, 241, 0.12), transparent 40%), radial-gradient(circle at bottom left, rgba(139, 92, 246, 0.08), transparent 40%)'
    }}>
      <div className="glass-panel animate-fade-in" style={{
        width: '100%',
        maxWidth: '420px',
        padding: '40px 30px',
        border: '1px solid rgba(255, 255, 255, 0.06)',
        boxShadow: '0 20px 50px rgba(0, 0, 0, 0.4)'
      }}>
        {/* Brand Logo Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '60px',
            height: '60px',
            borderRadius: '16px',
            background: 'var(--primary-gradient)',
            boxShadow: '0 8px 24px var(--primary-glow)',
            marginBottom: '16px'
          }}>
            <Ticket size={30} color="#fff" />
          </div>
          <h1 style={{ fontSize: '2rem', marginBottom: '6px' }} className="text-gradient">EzTicket Admin</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Hệ thống quản trị và kiểm soát dữ liệu</p>
        </div>

        {/* Error message */}
        {error && (
          <div className="glass-panel animate-fade-in" style={{
            background: 'rgba(239, 68, 68, 0.08)',
            borderColor: 'rgba(239, 68, 68, 0.2)',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            color: '#fca5a5',
            fontSize: '0.85rem'
          }}>
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {/* Form Inputs */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={{ display: 'block', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '8px', fontWeight: 500 }}>
              Email Đăng Nhập
            </label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type="email"
                required
                className="form-input"
                placeholder="admin@ezticket.vn"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ paddingLeft: '40px' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '8px', fontWeight: 500 }}>
              Mật Khẩu
            </label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type="password"
                required
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ paddingLeft: '40px' }}
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
            style={{
              justifyContent: 'center',
              padding: '12px',
              fontSize: '1rem',
              marginTop: '10px'
            }}
          >
            {loading ? 'Đang xác thực...' : 'Đăng Nhập Quản Trị'}
          </button>
        </form>

        {/* Credentials Sandbox Helper */}
        {!isFirebaseConfigured && (
          <div style={{
            marginTop: '28px',
            padding: '14px',
            background: 'rgba(99, 102, 241, 0.05)',
            border: '1px dashed rgba(99, 102, 241, 0.2)',
            borderRadius: '10px',
            fontSize: '0.8rem',
            color: 'var(--text-secondary)',
            lineHeight: '1.4'
          }}>
            <strong style={{ color: 'var(--primary)', display: 'block', marginBottom: '4px' }}>💻 Chế độ Sandbox cục bộ đang bật!</strong>
            Bạn có thể đăng nhập ngay với thông tin mặc định:
            <div style={{ marginTop: '6px', fontFamily: 'monospace', color: '#fff' }}>
              Email: <b>admin@ezticket.vn</b><br />
              Mật khẩu: <b>admin123</b>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
