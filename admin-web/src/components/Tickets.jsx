import React, { useState } from 'react';
import { 
  Search, Ticket, Check, X, Phone, Calendar, 
  MapPin, DollarSign, Wallet, CreditCard, ShieldAlert 
} from 'lucide-react';
import { dataService } from '../services/dataService';

export default function Tickets({ tickets }) {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // ----------------------------------------------------
  // 💼 STATE CHANGERS (Xác nhận/Hủy đơn vé)
  // ----------------------------------------------------
  
  const handleConfirmPayment = async (ticketId) => {
    const paymentMethod = prompt("Nhập phương thức thanh toán (ví dụ: MOMO, BANKING, CASH):", "BANKING");
    if (paymentMethod === null) return; // User cancelled prompt
    
    try {
      await dataService.updateTicketStatus(ticketId, "Thành công", paymentMethod.toUpperCase().trim());
      alert("Đã xác nhận thanh toán đơn vé thành công!");
    } catch (err) {
      alert("Lỗi xác nhận: " + err.message);
    }
  };

  const handleCancelTicket = async (ticketId) => {
    if (confirm("Bạn có chắc chắn muốn HỦY đơn vé này không? Trạng thái sẽ cập nhật thành Đã hủy.")) {
      try {
        await dataService.updateTicketStatus(ticketId, "Đã hủy");
        alert("Đã hủy đơn vé thành công.");
      } catch (err) {
        alert("Lỗi hủy vé: " + err.message);
      }
    }
  };

  // Filter logic
  const filteredTickets = tickets.filter(t => {
    const matchesSearch = 
      t.orderCode.toLowerCase().includes(search.toLowerCase()) ||
      t.eventName.toLowerCase().includes(search.toLowerCase()) ||
      (t.customerPhone && t.customerPhone.includes(search));
      
    const matchesStatus = statusFilter === '' || t.status === statusFilter;
    
    return matchesSearch && matchesStatus;
  });

  const formatVND = (num) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const formatDateTime = (ts) => {
    if (!ts) return 'Chưa cập nhật';
    const d = ts.toDate ? ts.toDate() : (ts.seconds ? new Date(ts.seconds * 1000) : new Date(ts));
    return d.toLocaleString('vi-VN', { 
      hour: '2-digit', 
      minute: '2-digit', 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    });
  };

  return (
    <div className="animate-fade-in" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Title */}
      <div>
        <h2 style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
          Quản Lý Đơn Vé & Giao Dịch
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Tra cứu, phê duyệt thanh toán cho các đơn đặt vé đang chờ hoặc hủy vé khách hàng yêu cầu.
        </p>
      </div>

      {/* Search & Filters */}
      <div className="glass-panel" style={{ padding: '16px', display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            className="form-input"
            placeholder="Tìm theo Mã đơn (EZ-xxx), Tên Show, Số điện thoại..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ paddingLeft: '40px' }}
          />
        </div>

        {/* Status selector */}
        <select
          className="form-input"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          style={{ width: '220px' }}
        >
          <option value="">Tất cả trạng thái</option>
          <option value="Thành công">Thành công (Đã mua)</option>
          <option value="Đang chờ thanh toán">Đang chờ thanh toán</option>
          <option value="Đã hủy">Đã hủy</option>
        </select>
      </div>

      {/* Tickets Table */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Mã Đơn</th>
                <th>Sự Kiện & Suất Diễn</th>
                <th>Khách Hàng (SĐT)</th>
                <th>Hạng Vé & SL</th>
                <th>Tổng Cộng</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái</th>
                <th style={{ textAlign: 'right' }}>Duyệt Đơn</th>
              </tr>
            </thead>
            <tbody>
              {filteredTickets.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '40px' }}>
                    Không tìm thấy dữ liệu đơn đặt vé nào.
                  </td>
                </tr>
              ) : (
                filteredTickets.map((ticket) => {
                  
                  const statusClass = 
                    ticket.status === "Thành công" ? "badge-success" : 
                    ticket.status === "Đang chờ thanh toán" ? "badge-warning" : "badge-danger";

                  return (
                    <tr key={ticket.id}>
                      {/* Order Code */}
                      <td style={{ fontWeight: 600, fontFamily: 'monospace', color: '#fff' }}>
                        {ticket.orderCode || 'EZ-NONE'}
                      </td>

                      {/* Event Details */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <img 
                            src={ticket.imageUrl} 
                            alt={ticket.eventName}
                            style={{ width: '50px', height: '34px', borderRadius: '4px', objectFit: 'cover' }}
                          />
                          <div>
                            <span style={{ fontWeight: 600, display: 'block', fontSize: '0.85rem', color: '#fff' }}>{ticket.eventName}</span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '3px', marginTop: '2px' }}>
                              <Calendar size={10} /> {formatDateTime(ticket.eventDate)}
                            </span>
                          </div>
                        </div>
                      </td>

                      {/* Customer Info */}
                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#fff' }}>
                            <Phone size={10} style={{ color: 'var(--primary)' }} /> {ticket.customerPhone || 'Chưa cập nhật'}
                          </span>
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', display: 'block', marginTop: '2px' }}>
                            Lúc: {formatDateTime(ticket.createdAt)}
                          </span>
                        </div>
                      </td>

                      {/* Ticket Type & Qty */}
                      <td>
                        <div>
                          <span style={{ color: '#fff', fontSize: '0.85rem', fontWeight: 500 }}>{ticket.ticketTypeName}</span>
                          <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', display: 'block', marginTop: '2px' }}>
                            Số lượng: <b>{ticket.quantity}</b> × {formatVND(ticket.unitPrice)}
                          </span>
                        </div>
                      </td>

                      {/* Total Price */}
                      <td style={{ fontWeight: 600, color: '#fff', fontSize: '0.95rem' }}>
                        {formatVND(ticket.totalPrice)}
                      </td>

                      {/* Payment Method */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem' }}>
                          {ticket.paymentMethod === "MOMO" ? (
                            <span style={{ color: '#ec4899', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '2px' }}>
                              <Wallet size={12} /> MOMO
                            </span>
                          ) : ticket.paymentMethod === "BANKING" ? (
                            <span style={{ color: 'var(--info)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '2px' }}>
                              <CreditCard size={12} /> BANKING
                            </span>
                          ) : (
                            <span style={{ color: 'var(--text-muted)' }}>Chưa thanh toán</span>
                          )}
                        </div>
                      </td>

                      {/* Status */}
                      <td>
                        <span className={`badge ${statusClass}`}>
                          {ticket.status}
                        </span>
                      </td>

                      {/* Operations Approval */}
                      <td style={{ textAlign: 'right' }}>
                        {ticket.status === "Đang chờ thanh toán" ? (
                          <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleConfirmPayment(ticket.id)}
                              style={{
                                background: 'rgba(16, 185, 129, 0.08)',
                                border: '1px solid rgba(16, 185, 129, 0.2)',
                                color: 'var(--success)',
                                cursor: 'pointer',
                                padding: '6px 10px',
                                borderRadius: '6px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                transition: 'var(--transition-smooth)'
                              }}
                              className="approve-btn"
                            >
                              <Check size={12} /> Nhận tiền
                            </button>
                            <button
                              onClick={() => handleCancelTicket(ticket.id)}
                              style={{
                                background: 'rgba(239, 68, 68, 0.08)',
                                border: '1px solid rgba(239, 68, 68, 0.2)',
                                color: '#fca5a5',
                                cursor: 'pointer',
                                padding: '6px 10px',
                                borderRadius: '6px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                transition: 'var(--transition-smooth)'
                              }}
                              className="cancel-btn"
                            >
                              <X size={12} /> Hủy
                            </button>
                          </div>
                        ) : ticket.status === "Thành công" ? (
                          <button
                            onClick={() => handleCancelTicket(ticket.id)}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: 'var(--text-muted)',
                              cursor: 'pointer',
                              fontSize: '0.75rem',
                              textDecoration: 'underline'
                            }}
                          >
                            Hoàn vé/Hủy
                          </button>
                        ) : (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '3px', justifyContent: 'flex-end' }}>
                            <ShieldAlert size={12} /> Khóa đơn
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
        .approve-btn:hover {
          background: var(--success) !important;
          color: #fff !important;
          box-shadow: 0 0 10px var(--success-glow);
        }
        .cancel-btn:hover {
          background: var(--danger) !important;
          color: #fff !important;
          box-shadow: 0 0 10px var(--danger-glow);
        }
      `}</style>

    </div>
  );
}
