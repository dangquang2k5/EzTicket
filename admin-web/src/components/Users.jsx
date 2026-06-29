import React, { useState } from 'react';
import { 
  Search, Shield, User, Ban, CheckCircle, 
  Trash2, UserX, UserCheck, Calendar 
} from 'lucide-react';
import { dataService } from '../services/dataService';

export default function Users({ users, currentUser }) {
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // ----------------------------------------------------
  // 💼 STATE CHANGERS (Khóa/Mở tài khoản, Đổi quyền)
  // ----------------------------------------------------

  const handleToggleStatus = async (user) => {
    // Prevent blocking oneself
    if (user.uid === currentUser?.uid) {
      alert("Bạn không thể tự khóa tài khoản của chính mình!");
      return;
    }

    const nextStatus = user.status === "ACTIVE" ? "LOCKED" : "ACTIVE";
    const confirmMsg = nextStatus === "LOCKED" 
      ? `Bạn có chắc muốn KHÓA tài khoản của ${user.fullName}? Người dùng này sẽ không thể mua vé.`
      : `Bạn có muốn MỞ KHÓA tài khoản của ${user.fullName}?`;

    if (confirm(confirmMsg)) {
      try {
        await dataService.updateUserStatus(user.uid, nextStatus);
        alert("Đã cập nhật trạng thái tài khoản thành công!");
      } catch (err) {
        alert("Cập nhật thất bại: " + err.message);
      }
    }
  };

  const handleToggleRole = async (user) => {
    if (user.uid === currentUser?.uid) {
      alert("Bạn không thể tự hạ quyền quản trị của chính mình!");
      return;
    }

    const nextRole = user.role === "ADMIN" ? "USER" : "ADMIN";
    const confirmMsg = nextRole === "ADMIN" 
      ? `Bạn có chắc muốn NÂNG CẤP tài khoản ${user.fullName} lên quyền QUẢN TRỊ VIÊN (ADMIN)?`
      : `HẠ QUYỀN tài khoản ${user.fullName} xuống Người dùng thường (USER)?`;

    if (confirm(confirmMsg)) {
      try {
        await dataService.updateUserRole(user.uid, nextRole);
        alert("Đã thay đổi vai trò tài khoản thành công!");
      } catch (err) {
        alert("Cập nhật vai trò thất bại: " + err.message);
      }
    }
  };

  // Filter
  const filteredUsers = users.filter(u => {
    const matchesSearch = 
      u.fullName.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      (u.phone && u.phone.includes(search));

    const matchesRole = roleFilter === '' || u.role === roleFilter;
    const matchesStatus = statusFilter === '' || u.status === statusFilter;

    return matchesSearch && matchesRole && matchesStatus;
  });

  const formatDate = (epochMillis) => {
    if (!epochMillis) return 'N/A';
    const d = new Date(epochMillis);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  return (
    <div className="animate-fade-in" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Title */}
      <div>
        <h2 style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
          Quản Lý Khách Hàng & Thành Viên
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Kiểm soát quyền truy cập của người dùng di động, mở/khóa tài khoản hoặc phân quyền admin phụ.
        </p>
      </div>

      {/* Filters */}
      <div className="glass-panel" style={{ padding: '16px', display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            className="form-input"
            placeholder="Tìm theo Tên thành viên, Email, Số điện thoại..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ paddingLeft: '40px' }}
          />
        </div>

        {/* Role Selector */}
        <select
          className="form-input"
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          style={{ width: '160px' }}
        >
          <option value="">Tất cả quyền</option>
          <option value="USER">USER (Khách hàng)</option>
          <option value="ADMIN">ADMIN (Quản trị)</option>
        </select>

        {/* Status Selector */}
        <select
          className="form-input"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          style={{ width: '160px' }}
        >
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE">ACTIVE (Đang chạy)</option>
          <option value="LOCKED">LOCKED (Đã khóa)</option>
        </select>
      </div>

      {/* Users table */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Ảnh Đại Diện</th>
                <th>Họ Tên</th>
                <th>Thông Tin Liên Hệ</th>
                <th>Ngày Tham Gia</th>
                <th>Vai Trò</th>
                <th>Trạng Thái</th>
                <th style={{ textAlign: 'right' }}>Thao Tác Cài Đặt</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '40px' }}>
                    Không có người dùng nào trùng khớp với bộ lọc của bạn.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => {
                  
                  const isBlocked = user.status === "LOCKED";
                  const isAdmin = user.role === "ADMIN";

                  return (
                    <tr key={user.uid}>
                      {/* Avatar */}
                      <td>
                        <img 
                          src={user.avatarUrl || `https://api.dicebear.com/7.x/adventurer/svg?seed=${user.uid}`} 
                          alt={user.fullName}
                          style={{ 
                            width: '40px', 
                            height: '40px', 
                            borderRadius: '50%', 
                            background: '#fff',
                            border: `2px solid ${isAdmin ? 'var(--primary)' : 'rgba(255,255,255,0.08)'}` 
                          }}
                        />
                      </td>

                      {/* Name */}
                      <td style={{ fontWeight: 600, color: '#fff' }}>
                        {user.fullName}
                        {user.uid === currentUser?.uid && (
                          <span style={{ fontSize: '0.7rem', color: 'var(--primary)', marginLeft: '6px', background: 'rgba(99, 102, 241, 0.1)', padding: '2px 6px', borderRadius: '4px' }}>
                            BẠN
                          </span>
                        )}
                      </td>

                      {/* Contact */}
                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          <span style={{ color: 'var(--text-primary)', fontWeight: 500, display: 'block' }}>{user.email}</span>
                          <span style={{ color: 'var(--text-secondary)', display: 'block', marginTop: '2px' }}>{user.phone || 'Chưa cung cấp SĐT'}</span>
                        </div>
                      </td>

                      {/* Creation Date */}
                      <td>
                        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <Calendar size={12} /> {formatDate(user.createdAt)}
                        </span>
                      </td>

                      {/* Role */}
                      <td>
                        <span className={`badge ${isAdmin ? 'badge-success' : 'badge-info'}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Shield size={10} /> {user.role}
                        </span>
                      </td>

                      {/* Status */}
                      <td>
                        <span className={`badge ${isBlocked ? 'badge-danger' : 'badge-success'}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          {isBlocked ? <Ban size={10} /> : <CheckCircle size={10} />}
                          {user.status}
                        </span>
                      </td>

                      {/* Actions (Block/Unblock, Promote/Demote) */}
                      <td style={{ textAlign: 'right' }}>
                        {user.uid !== currentUser?.uid ? (
                          <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                            {/* Promote / Demote */}
                            <button
                              onClick={() => handleToggleRole(user)}
                              style={{
                                background: 'rgba(255,255,255,0.03)',
                                border: '1px solid var(--border-light)',
                                color: 'var(--text-primary)',
                                cursor: 'pointer',
                                padding: '6px 12px',
                                borderRadius: '6px',
                                fontSize: '0.75rem',
                                fontWeight: 500,
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                transition: 'var(--transition-smooth)'
                              }}
                              className="user-action-btn"
                            >
                              {isAdmin ? <UserX size={12} /> : <UserCheck size={12} />}
                              {isAdmin ? 'Hạ Admin' : 'Lên Admin'}
                            </button>

                            {/* Block / Unblock */}
                            <button
                              onClick={() => handleToggleStatus(user)}
                              style={{
                                background: isBlocked ? 'rgba(16, 185, 129, 0.08)' : 'rgba(239, 68, 68, 0.08)',
                                border: isBlocked ? '1px solid rgba(16, 185, 129, 0.2)' : '1px solid rgba(239, 68, 68, 0.2)',
                                color: isBlocked ? 'var(--success)' : '#fca5a5',
                                cursor: 'pointer',
                                padding: '6px 12px',
                                borderRadius: '6px',
                                fontSize: '0.75rem',
                                fontWeight: 500,
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                transition: 'var(--transition-smooth)'
                              }}
                              className={isBlocked ? "unban-btn" : "ban-btn"}
                            >
                              <Ban size={12} />
                              {isBlocked ? 'Mở Khóa' : 'Khóa Nick'}
                            </button>
                          </div>
                        ) : (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontStyle: 'italic' }}>
                            Không khả dụng
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      <style>{`
        .user-action-btn:hover {
          background: rgba(255,255,255,0.08) !important;
          border-color: var(--primary) !important;
        }
        .ban-btn:hover {
          background: var(--danger) !important;
          color: #fff !important;
        }
        .unban-btn:hover {
          background: var(--success) !important;
          color: #fff !important;
        }
      `}</style>

    </div>
  );
}
