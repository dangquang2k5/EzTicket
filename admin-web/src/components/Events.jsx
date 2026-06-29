import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { 
  Plus, Search, Edit2, Trash2, Calendar, 
  MapPin, Tag, Eye, EyeOff, Flame, AlertCircle, X, Clock 
} from 'lucide-react';
import { dataService } from '../services/dataService';

export default function Events({ events }) {
  // Get all unique categories dynamically from the loaded events
  const categories = Array.from(new Set((events || []).map(e => e.category).filter(Boolean)));

  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  
  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const eventsPerPage = 10;

  // Reset to page 1 when search filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [search, categoryFilter]);
  
  // Form states
  const [name, setName] = useState('');
  const [venueName, setVenueName] = useState('');
  const [address, setAddress] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState(categories[0] || '');
  const [status, setStatus] = useState('AVAILABLE');
  const [organizerName, setOrganizerName] = useState('');
  const [organizerLogo, setOrganizerLogo] = useState('');
  const [isBanner, setIsBanner] = useState(false);
  const [isHot, setIsHot] = useState(false);
  const [isVisible, setIsVisible] = useState(true);
  const [customCategory, setCustomCategory] = useState('');
  const [showCustomInput, setShowCustomInput] = useState(false);
  
  // Dynamic Schedules list
  // [{ date: "YYYY-MM-DDTHH:MM", endDate: "YYYY-MM-DDTHH:MM", ticketTypes: [{ name: "VIP", price: 1000000, isVisible: true, quantity: 100 }] }]
  const [schedules, setSchedules] = useState([]);

  // ----------------------------------------------------
  // 🔄 FORM BINDINGS & HELPERS
  // ----------------------------------------------------
  
  const openCreateModal = () => {
    setEditingEvent(null);
    setName('');
    setVenueName('');
    setAddress('');
    setImageUrl('');
    setDescription('');
    setCategory(categories[0] || '');
    setCustomCategory('');
    setShowCustomInput(false);
    setStatus('AVAILABLE');
    setOrganizerName('');
    setOrganizerLogo('');
    setIsBanner(false);
    setIsHot(false);
    setIsVisible(true);
    
    // Start with empty schedules
    setSchedules([]);
    
    setIsModalOpen(true);
  };

  const openEditModal = (event) => {
    setEditingEvent(event);
    setName(event.name || '');
    setVenueName(event.venueName || '');
    setAddress(event.address || '');
    setImageUrl(event.image_url || '');
    setDescription(event.description || '');
    setCategory(event.category || categories[0] || '');
    setCustomCategory('');
    setShowCustomInput(false);
    setStatus(event.status || 'AVAILABLE');
    setOrganizerName(event.organizerName || '');
    setOrganizerLogo(event.organizerLogo || '');
    setIsBanner(event.isBanner || false);
    setIsHot(event.isHot || false);
    setIsVisible(event.isVisible !== undefined ? event.isVisible : true);

    // Convert firebase seconds timestamps to local YYYY-MM-DDTHH:MM strings
    const formattedSchedules = (Array.isArray(event.schedules) ? event.schedules : []).map(sch => {
      const parseDate = (ts) => {
        try {
          if (!ts) return '';
          let d;
          if (ts.toDate) {
            d = ts.toDate();
          } else if (ts.seconds) {
            d = new Date(ts.seconds * 1000);
          } else {
            d = new Date(ts);
          }
          if (isNaN(d.getTime())) return '';
          const tzoffset = d.getTimezoneOffset() * 60000;
          return new Date(d.getTime() - tzoffset).toISOString().slice(0, 16);
        } catch (err) {
          console.error("Error parsing date: ", err);
          return '';
        }
      };
      
      return {
        date: parseDate(sch.date),
        endDate: parseDate(sch.endDate),
        ticketTypes: (Array.isArray(sch.ticketTypes) ? sch.ticketTypes : []).map(t => ({
          name: t.name || '',
          price: Number(t.price) || 0,
          isVisible: t.isVisible !== undefined ? t.isVisible : true,
          quantity: Number(t.quantity) || 0
        }))
      };
    });
    
    setSchedules(formattedSchedules);
    setIsModalOpen(true);
  };

  // Schedule management
  const addSchedule = () => {
    setSchedules([...schedules, {
      date: '',
      endDate: '',
      ticketTypes: []
    }]);
  };

  const removeSchedule = (sIdx) => {
    if (schedules.length === 1) {
      alert("Sự kiện bắt buộc phải có ít nhất một suất diễn!");
      return;
    }
    setSchedules(schedules.filter((_, idx) => idx !== sIdx));
  };

  // Ticket type management in schedules
  const addTicketType = (sIdx) => {
    setSchedules(prevSchedules => {
      return prevSchedules.map((sch, idx) => {
        if (idx !== sIdx) return sch;
        return {
          ...sch,
          ticketTypes: [
            ...sch.ticketTypes,
            {
              name: '',
              price: '',
              isVisible: true,
              quantity: ''
            }
          ]
        };
      });
    });
  };

  const removeTicketType = (sIdx, tIdx) => {
    if (schedules[sIdx].ticketTypes.length === 1) {
      alert("Suất diễn phải có ít nhất một hạng vé!");
      return;
    }
    setSchedules(prevSchedules => {
      return prevSchedules.map((sch, idx) => {
        if (idx !== sIdx) return sch;
        return {
          ...sch,
          ticketTypes: sch.ticketTypes.filter((_, idx2) => idx2 !== tIdx)
        };
      });
    });
  };

  const handleTicketTypeChange = (sIdx, tIdx, field, val) => {
    setSchedules(prevSchedules => {
      return prevSchedules.map((sch, idx) => {
        if (idx !== sIdx) return sch;
        return {
          ...sch,
          ticketTypes: sch.ticketTypes.map((ticket, idx2) => {
            if (idx2 !== tIdx) return ticket;
            return {
              ...ticket,
              [field]: field === 'price' || field === 'quantity'
                ? (val === '' ? '' : Number(val))
                : val
            };
          })
        };
      });
    });
  };

  // ----------------------------------------------------
  // 💾 CRUD TRANSACTIONS
  // ----------------------------------------------------

  const handleSave = async (e) => {
    e.preventDefault();
    
    // Validation
    if (schedules.length === 0) {
      alert("Sự kiện bắt buộc phải có ít nhất một suất diễn!");
      return;
    }

    if (schedules.some(sch => !sch.date || !sch.endDate)) {
      alert("Vui lòng nhập đầy đủ thời gian diễn ra và kết thúc của các suất diễn!");
      return;
    }

    if (schedules.some(sch => !sch.ticketTypes || sch.ticketTypes.length === 0)) {
      alert("Mỗi suất diễn bắt buộc phải có ít nhất một hạng vé!");
      return;
    }

    // Convert local schedule ISO dates back to Firebase serializable structures (Seconds/Timestamps)
    const processedSchedules = schedules.map(sch => {
      const secStart = Math.floor(new Date(sch.date).getTime() / 1000);
      const secEnd = Math.floor(new Date(sch.endDate).getTime() / 1000);
      return {
        date: { seconds: secStart },
        endDate: { seconds: secEnd },
        ticketTypes: sch.ticketTypes.map(t => ({
          name: t.name,
          price: Number(t.price),
          isVisible: Boolean(t.isVisible),
          quantity: Number(t.quantity)
        }))
      };
    });

    const finalCategory = category === '__NEW__' ? customCategory.trim() : category;
    if (category === '__NEW__' && !customCategory.trim()) {
      alert("Vui lòng nhập tên danh mục mới!");
      return;
    }

    const eventPayload = {
      name,
      venueName,
      address,
      image_url: imageUrl,
      description,
      category: finalCategory,
      status,
      organizerName,
      organizerLogo,
      isBanner,
      isHot,
      isVisible,
      schedules: processedSchedules
    };

    if (editingEvent) {
      eventPayload.id = editingEvent.id;
    }

    try {
      await dataService.saveEvent(eventPayload);
      setIsModalOpen(false);
    } catch (err) {
      alert("Không thể lưu sự kiện: " + err.message);
    }
  };

  const handleDelete = async (id) => {
    if (confirm("Hành động này sẽ XÓA VĨNH VIỄN sự kiện này khỏi hệ thống. Bạn có chắc chắn muốn thực hiện?")) {
      try {
        await dataService.deleteEvent(id);
      } catch (err) {
        alert("Không thể xóa sự kiện: " + err.message);
      }
    }
  };

  // Filtered Events List
  const filteredEvents = events.filter(event => {
    const matchesSearch = event.name.toLowerCase().includes(search.toLowerCase()) || 
                          event.venueName.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = categoryFilter === '' || event.category === categoryFilter;
    return matchesSearch && matchesCategory;
  });

  // Pagination calculations
  const indexOfLastEvent = currentPage * eventsPerPage;
  const indexOfFirstEvent = indexOfLastEvent - eventsPerPage;
  const currentEvents = filteredEvents.slice(indexOfFirstEvent, indexOfLastEvent);
  const totalPages = Math.ceil(filteredEvents.length / eventsPerPage);

  // categories list is defined dynamically at the top of the component

  const formatVND = (num) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  return (
    <div className="animate-fade-in" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Header section */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px' }}>
        <div>
          <h2 style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
            Quản Lý Sự Kiện ({events.length})
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Thêm mới, sửa đổi thông tin, suất chiếu và kiểm soát giá của từng hạng vé sự kiện.
          </p>
        </div>
        
        <button className="btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Thêm Sự Kiện Mới
        </button>
      </div>

      {/* Search & Filters */}
      <div className="glass-panel" style={{ padding: '16px', display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center', flex: 1 }}>
          {/* Search Input */}
          <div style={{ position: 'relative', flex: 1, minWidth: '240px' }}>
            <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-input"
              placeholder="Tìm theo tên sự kiện, rạp hát..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: '40px' }}
            />
          </div>

          {/* Category filter */}
          <select
            className="form-input"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            style={{ width: '180px' }}
          >
            <option value="">Tất cả danh mục</option>
            {categories.map((c, idx) => (
              <option key={idx} value={c}>{c}</option>
            ))}
          </select>
        </div>

        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 500 }}>
          Hiển thị: <span style={{ color: '#fff', fontWeight: 600 }}>{filteredEvents.length}</span> / <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{events.length}</span> sự kiện
        </div>
      </div>

      {/* Events Table / Grid */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Thông tin sự kiện</th>
                <th>Địa điểm</th>
                <th>Danh mục</th>
                <th>Giá thấp nhất</th>
                <th>Số lượng vé</th>
                <th>Các nhãn cấu hình</th>
                <th>Trạng thái</th>
                <th style={{ textAlign: 'right' }}>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {currentEvents.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '40px' }}>
                    Không tìm thấy sự kiện nào trùng khớp với bộ lọc của bạn.
                  </td>
                </tr>
              ) : (
                currentEvents.map((event) => {
                  
                  // Compute min price dynamically
                  const prices = (Array.isArray(event.schedules) ? event.schedules : [])
                    .flatMap(s => Array.isArray(s.ticketTypes) ? s.ticketTypes : [])
                    .map(t => Number(t.price) || 0);
                  const minPrice = prices.length > 0 ? Math.min(...prices) : 0;

                  // Compute total tickets quantity dynamically
                  const totalTickets = (Array.isArray(event.schedules) ? event.schedules : [])
                    .flatMap(s => Array.isArray(s.ticketTypes) ? s.ticketTypes : [])
                    .reduce((sum, t) => sum + (Number(t.quantity) || 0), 0);

                  return (
                    <tr key={event.id}>
                      {/* Event Banner & Info */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                          <img 
                            src={event.image_url} 
                            alt={event.name} 
                            style={{ width: '80px', height: '48px', borderRadius: '6px', objectFit: 'cover' }}
                          />
                          <div>
                            <h4 style={{ color: '#fff', fontSize: '0.95rem', fontWeight: 600 }}>{event.name}</h4>
                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', marginTop: '2px' }}>
                              {event.organizerName}
                            </span>
                          </div>
                        </div>
                      </td>

                      {/* Venue Location */}
                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#fff', fontWeight: 500 }}>
                            <MapPin size={12} style={{ color: 'var(--primary)' }} /> {event.venueName}
                          </span>
                          <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.8rem', marginTop: '2px' }}>
                            {event.address}
                          </span>
                        </div>
                      </td>

                      {/* Category */}
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                          <Tag size={12} /> {event.category}
                        </span>
                      </td>

                      {/* Price Tier */}
                      <td style={{ fontWeight: 600, color: '#fff' }}>
                        {minPrice > 0 ? formatVND(minPrice) : 'Liên hệ'}
                      </td>

                      {/* Ticket Quantity */}
                      <td style={{ fontWeight: 500, color: 'var(--text-secondary)' }}>
                        <span style={{ color: '#fff', fontWeight: 600 }}>{totalTickets}</span> vé
                      </td>

                      {/* Attribute Badges */}
                      <td>
                        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                          {event.isHot && (
                            <span className="badge badge-danger" style={{ display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                              <Flame size={10} /> Hot
                            </span>
                          )}
                          {event.isBanner && (
                            <span className="badge badge-info">Banner</span>
                          )}
                          {event.isVisible ? (
                            <span className="badge badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                              <Eye size={10} /> Hiện
                            </span>
                          ) : (
                            <span className="badge badge-warning" style={{ display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                              <EyeOff size={10} /> Ẩn
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Ticket Availability Status */}
                      <td>
                        {event.status === "AVAILABLE" && <span className="badge badge-success">Còn Vé</span>}
                        {event.status === "COMING_SOON" && <span className="badge badge-info">Sắp Mở Bán</span>}
                        {event.status === "SOLD_OUT" && <span className="badge badge-danger">Hết Vé</span>}
                        {event.status === "CANCELLED" && <span className="badge badge-warning">Đã Hủy</span>}
                        {!["AVAILABLE", "COMING_SOON", "SOLD_OUT", "CANCELLED"].includes(event.status) && (
                          <span className="badge badge-danger">{event.status || "Hủy / Hết Vé"}</span>
                        )}
                      </td>

                      {/* Actions */}
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                          <button
                            onClick={() => openEditModal(event)}
                            style={{
                              background: 'rgba(255,255,255,0.03)',
                              border: '1px solid var(--border-light)',
                              color: 'var(--text-primary)',
                              cursor: 'pointer',
                              padding: '6px',
                              borderRadius: '6px',
                              display: 'flex',
                              alignItems: 'center',
                              transition: 'var(--transition-smooth)'
                            }}
                            className="action-btn-edit"
                          >
                            <Edit2 size={14} />
                          </button>
                          <button
                            onClick={() => handleDelete(event.id)}
                            style={{
                              background: 'rgba(239, 68, 68, 0.05)',
                              border: '1px solid rgba(239, 68, 68, 0.1)',
                              color: '#fca5a5',
                              cursor: 'pointer',
                              padding: '6px',
                              borderRadius: '6px',
                              display: 'flex',
                              alignItems: 'center',
                              transition: 'var(--transition-smooth)'
                            }}
                            className="action-btn-delete"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination UI Controls */}
        {totalPages > 1 && (
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '8px',
            marginTop: '20px',
            flexWrap: 'wrap'
          }}>
            <button
              disabled={currentPage === 1}
              type="button"
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              className="btn-secondary"
              style={{ padding: '6px 12px', opacity: currentPage === 1 ? 0.4 : 1, cursor: currentPage === 1 ? 'not-allowed' : 'pointer' }}
            >
              Trước
            </button>
            
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
              <button
                key={page}
                type="button"
                onClick={() => setCurrentPage(page)}
                style={{
                  padding: '6.5px 12px',
                  borderRadius: '6px',
                  border: '1px solid',
                  borderColor: currentPage === page ? 'var(--primary)' : 'var(--border-light)',
                  background: currentPage === page ? 'var(--primary-gradient)' : 'transparent',
                  color: '#fff',
                  cursor: 'pointer',
                  fontWeight: currentPage === page ? '600' : 'normal',
                  transition: 'var(--transition-smooth)'
                }}
              >
                {page}
              </button>
            ))}

            <button
              disabled={currentPage === totalPages}
              type="button"
              onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              className="btn-secondary"
              style={{ padding: '6px 12px', opacity: currentPage === totalPages ? 0.4 : 1, cursor: currentPage === totalPages ? 'not-allowed' : 'pointer' }}
            >
              Sau
            </button>
          </div>
        )}
      </div>

      {/* ----------------------------------------------------
          🌟 DYNAMIC CREATION & EDIT MODAL
          ---------------------------------------------------- */}
      {isModalOpen && createPortal(
        <div className="modal-overlay">
          <div className="modal-content glass-panel animate-fade-in" style={{ padding: '30px', border: '1px solid rgba(255,255,255,0.1)' }}>
            
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-light)', paddingBottom: '16px', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '1.35rem', color: '#fff' }}>
                {editingEvent ? 'Chỉnh Sửa Sự Kiện' : 'Thêm Sự Kiện Mới'}
              </h3>
              <button 
                onClick={() => setIsModalOpen(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
              
              {/* Basic Section */}
              <div>
                <h4 style={{ fontSize: '1rem', color: 'var(--primary)', marginBottom: '12px', fontWeight: 600 }}>1. Thông tin chung</h4>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div style={{ gridColumn: 'span 2' }}>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Tên sự kiện *</label>
                    <input type="text" required className="form-input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Live concert Chân Trời Rực Rỡ..." />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Tên địa điểm *</label>
                    <input type="text" required className="form-input" value={venueName} onChange={(e) => setVenueName(e.target.value)} placeholder="Sân vận động Hoa Lư" />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Địa chỉ cụ thể *</label>
                    <input type="text" required className="form-input" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Quận 1, TP. Hồ Chí Minh" />
                  </div>
                  <div style={{ gridColumn: 'span 2' }}>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Đường dẫn ảnh Banner (Image URL)</label>
                    <input type="text" className="form-input" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} />
                  </div>
                  <div style={{ gridColumn: 'span 2' }}>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Mô tả ngắn gọn *</label>
                    <textarea rows="3" required className="form-input" value={description} onChange={(e) => setDescription(e.target.value)} style={{ resize: 'vertical' }} placeholder="Tóm tắt chương trình..." />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Danh mục *</label>
                    <select 
                      className="form-input" 
                      value={category} 
                      onChange={(e) => {
                        const val = e.target.value;
                        if (val === '__NEW__') {
                          setShowCustomInput(true);
                          setCategory('__NEW__');
                        } else {
                          setShowCustomInput(false);
                          setCategory(val);
                        }
                      }}
                    >
                      {categories.map((c, idx) => <option key={idx} value={c}>{c}</option>)}
                      <option value="__NEW__">+ Thêm danh mục mới...</option>
                    </select>
                    {showCustomInput && (
                      <input 
                        type="text" 
                        required 
                        className="form-input" 
                        style={{ marginTop: '8px' }} 
                        placeholder="Nhập tên danh mục mới..." 
                        value={customCategory} 
                        onChange={(e) => setCustomCategory(e.target.value)} 
                      />
                    )}
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Trạng thái bán *</label>
                    <select className="form-input" value={status} onChange={(e) => setStatus(e.target.value)}>
                      <option value="AVAILABLE">AVAILABLE (Còn vé)</option>
                      <option value="COMING_SOON">COMING_SOON (Sắp mở bán)</option>
                      <option value="SOLD_OUT">SOLD_OUT (Hết vé)</option>
                      <option value="CANCELLED">CANCELLED (Hủy diễn)</option>
                    </select>
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Tên đơn vị tổ chức</label>
                    <input type="text" className="form-input" value={organizerName} onChange={(e) => setOrganizerName(e.target.value)} placeholder="Viet Vision" />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>Đường dẫn ảnh Logo tổ chức</label>
                    <input type="text" className="form-input" value={organizerLogo} onChange={(e) => setOrganizerLogo(e.target.value)} />
                  </div>
                </div>
              </div>

              {/* Toggles */}
              <div>
                <h4 style={{ fontSize: '1rem', color: 'var(--primary)', marginBottom: '12px', fontWeight: 600 }}>2. Thiết lập hiển thị</h4>
                <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '0.9rem' }}>
                    <input type="checkbox" checked={isHot} onChange={(e) => setIsHot(e.target.checked)} style={{ transform: 'scale(1.2)', accentColor: 'var(--primary)' }} />
                    Sự kiện HOT nổi bật
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '0.9rem' }}>
                    <input type="checkbox" checked={isBanner} onChange={(e) => setIsBanner(e.target.checked)} style={{ transform: 'scale(1.2)', accentColor: 'var(--primary)' }} />
                    Đặt lên Banner trang chủ
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '0.9rem' }}>
                    <input type="checkbox" checked={isVisible} onChange={(e) => setIsVisible(e.target.checked)} style={{ transform: 'scale(1.2)', accentColor: 'var(--primary)' }} />
                    Công khai (Hiện sự kiện với khách hàng)
                  </label>
                </div>
              </div>

              {/* Dynamic Schedules Section */}
              <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <h4 style={{ fontSize: '1rem', color: 'var(--primary)', fontWeight: 600 }}>3. Suất diễn & Hạng vé *</h4>
                  <button type="button" className="btn-secondary" onClick={addSchedule} style={{ padding: '6px 12px', fontSize: '0.8rem' }}>
                    <Plus size={14} /> Thêm suất diễn
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  {schedules.map((sch, sIdx) => (
                    <div key={sIdx} className="glass-panel" style={{ padding: '16px', background: 'rgba(0,0,0,0.2)', border: '1px solid rgba(255,255,255,0.04)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                        <span style={{ fontSize: '0.9rem', color: '#fff', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <Clock size={14} style={{ color: 'var(--primary)' }} /> Suất diễn #{sIdx + 1}
                        </span>
                        <button type="button" onClick={() => removeSchedule(sIdx)} style={{ background: 'none', border: 'none', color: 'var(--danger)', cursor: 'pointer', fontSize: '0.8rem' }}>
                          Xóa suất diễn này
                        </button>
                      </div>

                      {/* Time selector (Table format) */}
                      <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '16px' }}>
                        <thead>
                          <tr>
                            <th style={{ background: 'transparent', borderBottom: '1px solid rgba(255,255,255,0.08)', color: 'var(--text-secondary)', padding: '6px 8px', fontSize: '0.8rem', fontWeight: 500, textAlign: 'left' }}>Bắt đầu diễn ra *</th>
                            <th style={{ background: 'transparent', borderBottom: '1px solid rgba(255,255,255,0.08)', color: 'var(--text-secondary)', padding: '6px 8px', fontSize: '0.8rem', fontWeight: 500, textAlign: 'left' }}>Kết thúc dự kiến *</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td style={{ border: 'none', padding: '8px 4px' }}>
                              <input type="datetime-local" className="form-input" value={sch.date} onChange={(e) => {
                                const updated = [...schedules];
                                updated[sIdx].date = e.target.value;
                                setSchedules(updated);
                              }} required />
                            </td>
                            <td style={{ border: 'none', padding: '8px 4px' }}>
                              <input type="datetime-local" className="form-input" value={sch.endDate} onChange={(e) => {
                                const updated = [...schedules];
                                updated[sIdx].endDate = e.target.value;
                                setSchedules(updated);
                              }} required />
                            </td>
                          </tr>
                        </tbody>
                      </table>

                      {/* Ticket types nested */}
                      <div style={{ borderTop: '1px dashed rgba(255,255,255,0.06)', paddingTop: '12px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500 }}>Danh sách hạng vé:</span>
                          <button type="button" onClick={() => addTicketType(sIdx)} style={{ background: 'none', border: 'none', color: 'var(--info)', cursor: 'pointer', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '2px' }}>
                            <Plus size={12} /> Hạng vé
                          </button>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                          {sch.ticketTypes.map((ticket, tIdx) => (
                            <div key={tIdx} style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
                              <input type="text" placeholder="Tên vé (VIP, Standard...)" className="form-input" style={{ flex: 2, minWidth: '130px' }} value={ticket.name} onChange={(e) => handleTicketTypeChange(sIdx, tIdx, 'name', e.target.value)} required />
                              <input type="number" placeholder="Giá" className="form-input" style={{ flex: 1.5, minWidth: '90px' }} value={ticket.price} onChange={(e) => handleTicketTypeChange(sIdx, tIdx, 'price', e.target.value)} required />
                              <input type="number" placeholder="Số lượng" className="form-input" style={{ flex: 1, minWidth: '70px' }} value={ticket.quantity} onChange={(e) => handleTicketTypeChange(sIdx, tIdx, 'quantity', e.target.value)} required />
                              
                              <label style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.8rem', cursor: 'pointer' }}>
                                <input type="checkbox" checked={ticket.isVisible} onChange={(e) => handleTicketTypeChange(sIdx, tIdx, 'isVisible', e.target.checked)} />
                                Hiện
                              </label>

                              <button type="button" onClick={() => removeTicketType(sIdx, tIdx)} style={{ background: 'none', border: 'none', color: 'var(--danger)', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '4px' }}>
                                <X size={14} />
                              </button>
                            </div>
                          ))}
                        </div>
                      </div>

                    </div>
                  ))}
                </div>
              </div>

              {/* Actions footer */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', borderTop: '1px solid var(--border-light)', paddingTop: '20px', marginTop: '10px' }}>
                <button type="button" className="btn-secondary" onClick={() => setIsModalOpen(false)}>Hủy</button>
                <button type="submit" className="btn-primary">Lưu thông tin</button>
              </div>

            </form>
          </div>
        </div>,
        document.body
      )}

      <style>{`
        .action-btn-edit:hover {
          background: rgba(255,255,255,0.08) !important;
          border-color: var(--primary) !important;
        }
        .action-btn-delete:hover {
          background: rgba(239, 68, 68, 0.15) !important;
          border-color: var(--danger) !important;
        }
      `}</style>

    </div>
  );
}
