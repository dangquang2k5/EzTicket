import React, { useState, useEffect } from 'react';
import { Send, Bell, Calendar, Eye, Trash2, CheckCircle2 } from 'lucide-react';
import { dataService } from '../services/dataService';

export default function Notifications({ events }) {
  const [notifs, setNotifs] = useState([]);
  
  // Form states
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [type, setType] = useState('SYSTEM');
  const [sending, setSending] = useState(false);

  // Subscribe to sent notifications history
  useEffect(() => {
    const unsub = dataService.subscribeNotifs((list) => {
      setNotifs(list);
    });
    return () => unsub();
  }, []);

  const handleSend = async (e) => {
    e.preventDefault();
    setSending(true);
    
    const notiPayload = {
      title,
      body,
      type,
      uid: "", // Empty uid means broad broadcast to all users
      isRead: false,
      eventId: "",
      eventName: "",
      eventImageUrl: "",
      eventDate: null
    };

    try {
      await dataService.sendNotification(notiPayload);
      alert("Đã gửi thông báo hệ thống và đồng bộ lên ứng dụng di động thành công!");
      
      // reset form
      setTitle('');
      setBody('');
    } catch (err) {
      alert("Không thể gửi thông báo: " + err.message);
    } finally {
      setSending(false);
    }
  };

  const formatDateTime = (ts) => {
    if (!ts) return 'Chưa cập nhật';
    const d = ts.toDate ? ts.toDate() : (ts.seconds ? new Date(ts.seconds * 1000) : new Date(ts));
    return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  return (
    <div className="animate-fade-in" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Title */}
      <div>
        <h2 style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
          Hệ Thống Gửi Thông Báo (Notifications)
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Tạo và gửi thông báo khuyến mãi, nhắc nhở lịch diễn, sự kiện sắp tới cho tất cả người dùng ứng dụng di động EzTicket.
        </p>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
        gap: '30px'
      }}>
        {/* Form Sender */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <h3 style={{ fontSize: '1.15rem', color: '#fff', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Bell size={18} style={{ color: 'var(--primary)' }} /> Soạn Thông Báo Mới
          </h3>

          <form onSubmit={handleSend} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Title */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                Tiêu đề thông báo *
              </label>
              <input 
                type="text" 
                required 
                className="form-input" 
                placeholder="Ví dụ: Vé concert Hà Anh Tuấn sắp cháy kho!" 
                value={title} 
                onChange={(e) => setTitle(e.target.value)} 
              />
            </div>

            {/* Body */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                Nội dung chi tiết thông báo *
              </label>
              <textarea 
                rows="4" 
                required 
                className="form-input" 
                placeholder="Soạn nội dung hấp dẫn để gửi đến điện thoại khách hàng..." 
                value={body} 
                onChange={(e) => setBody(e.target.value)} 
                style={{ resize: 'vertical' }}
              />
            </div>

            {/* Type */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                Loại thông báo (Nhãn ứng dụng)
              </label>
              <select className="form-input" value={type} onChange={(e) => setType(e.target.value)}>
                <option value="SYSTEM">Thông báo hệ thống chung (SYSTEM)</option>
                <option value="PROMO">Thông báo khuyến mãi (PROMO)</option>
              </select>
            </div>

            {/* Submit */}
            <button 
              type="submit" 
              className="btn-primary" 
              disabled={sending} 
              style={{ justifyContent: 'center', padding: '12px', marginTop: '10px' }}
            >
              <Send size={16} /> {sending ? 'Đang truyền tín hiệu...' : 'Phát Hành Thông Báo Ngay'}
            </button>
          </form>
        </div>

        {/* Sent History Log */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <h3 style={{ fontSize: '1.15rem', color: '#fff' }}>Lịch Sử Đã Phát Hành</h3>
          
          <div style={{ 
            maxHeight: '480px', 
            overflowY: 'auto', 
            display: 'flex', 
            flexDirection: 'column', 
            gap: '12px', 
            paddingRight: '6px' 
          }}>
            {notifs.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-secondary)' }}>
                Chưa có lịch sử thông báo nào được phát đi.
              </div>
            ) : (
              notifs.map((noti) => (
                <div 
                  key={noti.id} 
                  className="glass-panel" 
                  style={{ 
                    padding: '14px', 
                    background: 'rgba(255,255,255,0.01)', 
                    border: '1px solid rgba(255,255,255,0.04)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '8px' 
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>{noti.type}</span>
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{formatDateTime(noti.createdAt)}</span>
                  </div>
                  
                  <h4 style={{ fontSize: '0.9rem', color: '#fff', fontWeight: 600 }}>{noti.title}</h4>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: '1.3' }}>{noti.body}</p>
                  
                  {noti.eventName && (
                    <div style={{ 
                      marginTop: '6px', 
                      display: 'flex', 
                      alignItems: 'center', 
                      gap: '8px', 
                      background: 'rgba(0,0,0,0.15)', 
                      padding: '6px 10px', 
                      borderRadius: '6px',
                      fontSize: '0.75rem',
                      color: 'var(--text-secondary)'
                    }}>
                      <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--primary)' }}></span>
                      <span>Show liên kết: <b>{noti.eventName}</b></span>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>

    </div>
  );
}
