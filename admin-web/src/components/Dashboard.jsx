import React from 'react';
import { 
  DollarSign, Ticket, Calendar, Users, 
  ArrowUpRight, AlertCircle, ShoppingBag, Landmark 
} from 'lucide-react';

export default function Dashboard({ events, tickets, users }) {
  
  // ----------------------------------------------------
  // 🌟 METRICS COMPUTATIONS
  // ----------------------------------------------------
  
  // 1. Total Revenue
  const totalRevenue = tickets
    .filter(t => t.status === "Thành công")
    .reduce((acc, t) => acc + (t.totalPrice || 0), 0);

  // 2. Total Tickets Sold (Successful ones)
  const ticketsSold = tickets
    .filter(t => t.status === "Thành công")
    .reduce((acc, t) => acc + (t.quantity || 1), 0);

  // 3. Active Events
  const activeEventsCount = events.filter(e => e.status === "AVAILABLE" && e.isVisible).length;

  // 4. Total Users
  const totalUsersCount = users.length;

  // 5. Recent 5 orders
  const recentTickets = tickets.slice(0, 5);

  // ----------------------------------------------------
  // 📈 CUSTOM SVG LINE CHART LOGIC (REVENUE LAST 7 DAYS)
  // ----------------------------------------------------
  // Let's create realistic revenue points for the last 7 days based on actual purchase dates
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - i);
    return {
      dateStr: d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
      dateKey: d.toDateString(),
      value: 0
    };
  }).reverse();

  // Populate data
  tickets.forEach(ticket => {
    if (ticket.status !== "Thành công") return;
    const ticketDate = ticket.createdAt?.toDate 
      ? ticket.createdAt.toDate() 
      : (ticket.createdAt?.seconds ? new Date(ticket.createdAt.seconds * 1000) : new Date(ticket.createdAt || Date.now()));
    
    const key = ticketDate.toDateString();
    const day = last7Days.find(d => d.dateKey === key);
    if (day) {
      day.value += (ticket.totalPrice || 0);
    }
  });

  // Calculate coordinates for SVG Path
  const chartWidth = 500;
  const chartHeight = 160;
  const paddingX = 40;
  const paddingY = 25;

  const maxVal = Math.max(...last7Days.map(d => d.value), 5000000); // at least 5 million for scale
  const minVal = 0;

  const points = last7Days.map((d, index) => {
    const x = paddingX + (index * (chartWidth - 2 * paddingX)) / (last7Days.length - 1);
    const y = chartHeight - paddingY - ((d.value - minVal) * (chartHeight - 2 * paddingY)) / (maxVal - minVal);
    return { x, y, ...d };
  });

  const pathD = points.reduce((acc, p, idx) => {
    return idx === 0 ? `M ${p.x} ${p.y}` : `${acc} L ${p.x} ${p.y}`;
  }, "");

  const areaD = points.length > 0 
    ? `${pathD} L ${points[points.length - 1].x} ${chartHeight - paddingY} L ${points[0].x} ${chartHeight - paddingY} Z`
    : "";

  // Helper to format currency
  const formatVND = (num) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  return (
    <div className="animate-fade-in" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Header Title */}
      <div>
        <h2 style={{ fontSize: '1.75rem', marginBottom: '4px', fontWeight: 700, fontFamily: 'var(--font-display)' }}>
          Bảng Điều Khiển Tổng Quan
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Số liệu thống kê tài chính và trạng thái bán vé thời gian thực.
        </p>
      </div>

      {/* KPI Cards Grid */}
      <div className="dashboard-grid">
        {/* Doanh thu */}
        <div className="glass-panel glass-card-glow" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: '12px',
            width: '48px',
            height: '48px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--success)'
          }}>
            <DollarSign size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500, textTransform: 'uppercase' }}>
              Tổng Doanh Thu
            </span>
            <h3 style={{ fontSize: '1.4rem', color: '#fff', marginTop: '4px' }}>
              {formatVND(totalRevenue)}
            </h3>
          </div>
        </div>

        {/* Vé bán */}
        <div className="glass-panel glass-card-glow" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{
            background: 'rgba(99, 102, 241, 0.1)',
            border: '1px solid rgba(99, 102, 241, 0.2)',
            borderRadius: '12px',
            width: '48px',
            height: '48px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--primary)'
          }}>
            <Ticket size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500, textTransform: 'uppercase' }}>
              Vé Đã Phát Hành
            </span>
            <h3 style={{ fontSize: '1.4rem', color: '#fff', marginTop: '4px' }}>
              {ticketsSold} vé
            </h3>
          </div>
        </div>

        {/* Sự kiện active */}
        <div className="glass-panel glass-card-glow" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{
            background: 'rgba(6, 182, 212, 0.1)',
            border: '1px solid rgba(6, 182, 212, 0.2)',
            borderRadius: '12px',
            width: '48px',
            height: '48px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--info)'
          }}>
            <Calendar size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500, textTransform: 'uppercase' }}>
              Sự Kiện Đang Chạy
            </span>
            <h3 style={{ fontSize: '1.4rem', color: '#fff', marginTop: '4px' }}>
              {activeEventsCount} Show
            </h3>
          </div>
        </div>

        {/* Người dùng */}
        <div className="glass-panel glass-card-glow" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{
            background: 'rgba(139, 92, 246, 0.1)',
            border: '1px solid rgba(139, 92, 246, 0.2)',
            borderRadius: '12px',
            width: '48px',
            height: '48px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#a855f7'
          }}>
            <Users size={24} />
          </div>
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500, textTransform: 'uppercase' }}>
              Khách Hàng Đăng Ký
            </span>
            <h3 style={{ fontSize: '1.4rem', color: '#fff', marginTop: '4px' }}>
              {totalUsersCount} thành viên
            </h3>
          </div>
        </div>
      </div>

      {/* Main Charts & Analytics View */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
        gap: '30px'
      }}>
        {/* Biểu đồ Doanh thu (Glow Line Chart) */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ fontSize: '1.1rem', color: '#fff' }}>Doanh Thu 7 Ngày Qua (VND)</h3>
            <span style={{ color: 'var(--success)', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 600 }}>
              <ArrowUpRight size={14} /> Hoạt động realtime
            </span>
          </div>

          <div style={{ width: '100%', position: 'relative' }}>
            {/* SVG line graph */}
            <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} style={{ width: '100%', overflow: 'visible' }}>
              <defs>
                <linearGradient id="lineGlow" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="var(--primary)" stopOpacity="0.4" />
                  <stop offset="100%" stopColor="var(--primary)" stopOpacity="0.0" />
                </linearGradient>
              </defs>

              {/* Grid Lines */}
              {[0, 1, 2, 3].map((g, i) => {
                const y = paddingY + (i * (chartHeight - 2 * paddingY)) / 3;
                const gridVal = maxVal - (i * maxVal) / 3;
                return (
                  <g key={i}>
                    <line x1={paddingX} y1={y} x2={chartWidth - paddingX} y2={y} stroke="rgba(255,255,255,0.03)" strokeWidth="1" />
                    <text x={paddingX - 8} y={y + 4} fill="var(--text-muted)" fontSize="8" textAnchor="end">
                      {gridVal >= 1000000 ? `${(gridVal/1000000).toFixed(1)}M` : gridVal}
                    </text>
                  </g>
                );
              })}

              {/* Area under the line */}
              {areaD && <path d={areaD} fill="url(#lineGlow)" />}

              {/* Line path */}
              {pathD && (
                <path 
                  d={pathD} 
                  fill="none" 
                  stroke="var(--primary)" 
                  strokeWidth="3" 
                  strokeLinecap="round"
                  style={{ filter: 'drop-shadow(0px 4px 6px var(--primary-glow))' }}
                />
              )}

              {/* Data points */}
              {points.map((p, idx) => (
                <g key={idx} className="chart-dot-group">
                  <circle cx={p.x} cy={p.y} r="4" fill="#fff" stroke="var(--primary)" strokeWidth="2" style={{ transition: 'all 0.2s' }} />
                  <circle cx={p.x} cy={p.y} r="8" fill="var(--primary)" opacity="0" className="chart-hover-ring" style={{ cursor: 'pointer', transition: 'all 0.2s' }} />
                  {/* Tooltip on hover */}
                  <title>{`${p.dateStr}: ${formatVND(p.value)}`}</title>
                </g>
              ))}

              {/* X Axis Labels */}
              {points.map((p, idx) => (
                <text key={idx} x={p.x} y={chartHeight - 5} fill="var(--text-secondary)" fontSize="9" textAnchor="middle">
                  {p.dateStr}
                </text>
              ))}
            </svg>
          </div>
        </div>

        {/* Thống kê phương thức thanh toán */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ fontSize: '1.1rem', color: '#fff' }}>Thống Kê Phương Thức Thanh Toán</h3>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Tổng {tickets.filter(t => t.status === "Thành công").length} đơn thành công
            </span>
          </div>

          {(() => {
            const successTickets = tickets.filter(t => t.status === "Thành công");
            const totalSuccess = successTickets.length;
            const totalSuccessRevenue = successTickets.reduce((s, t) => s + (t.totalPrice || 0), 0);

            // Phương thức thanh toán đã biết (luôn hiển thị) + tự phát hiện thêm
            const knownMethods = {
              'MOMO': { label: 'Ví MoMo', color: '#a855f7', gradient: 'linear-gradient(90deg, #a855f7, #ec4899)' },
              'BANKING': { label: 'Chuyển khoản ngân hàng', color: '#06b6d4', gradient: 'linear-gradient(90deg, #06b6d4, #10b981)' },
              'CASH': { label: 'Tiền mặt', color: '#f59e0b', gradient: 'linear-gradient(90deg, #f59e0b, #ef4444)' },
              'ZALOPAY': { label: 'ZaloPay', color: '#0068ff', gradient: 'linear-gradient(90deg, #0068ff, #00c2ff)' },
              'VNPAY': { label: 'VNPay', color: '#ef4444', gradient: 'linear-gradient(90deg, #ef4444, #f97316)' },
            };

            // Tự phát hiện tất cả payment methods thực tế từ dữ liệu
            const allMethods = new Set();
            successTickets.forEach(t => {
              if (t.paymentMethod) allMethods.add(t.paymentMethod.toUpperCase().trim());
            });
            // Thêm các method chưa biết vào danh sách
            const extraColors = ['#14b8a6', '#8b5cf6', '#64748b', '#e11d48'];
            let extraIdx = 0;
            allMethods.forEach(m => {
              if (!knownMethods[m]) {
                const c = extraColors[extraIdx % extraColors.length];
                knownMethods[m] = { label: m, color: c, gradient: `linear-gradient(90deg, ${c}, ${c}88)` };
                extraIdx++;
              }
            });

            // Tạo danh sách thống kê, sắp xếp theo doanh thu giảm dần
            const methodStats = Object.entries(knownMethods)
              .map(([key, meta]) => {
                const methodTickets = successTickets.filter(t => (t.paymentMethod || '').toUpperCase().trim() === key);
                const count = methodTickets.length;
                const revenue = methodTickets.reduce((s, t) => s + (t.totalPrice || 0), 0);
                const qty = methodTickets.reduce((s, t) => s + (t.quantity || 1), 0);
                const pct = totalSuccess > 0 ? (count / totalSuccess) * 100 : 0;
                return { key, ...meta, count, revenue, qty, pct };
              })
              .filter(m => m.count > 0 || ['MOMO', 'BANKING'].includes(m.key)) // Luôn hiển thị MOMO & BANKING
              .sort((a, b) => b.revenue - a.revenue);

            // Thống kê đơn chờ & đã hủy
            const pendingTickets = tickets.filter(t => t.status === "Đang chờ thanh toán");
            const cancelledTickets = tickets.filter(t => t.status === "Đã hủy");

            return (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                {/* Từng phương thức thanh toán */}
                {methodStats.map(m => (
                  <div key={m.key}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem', marginBottom: '6px' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: m.color, flexShrink: 0 }}></span>
                        <span style={{ fontWeight: 500 }}>{m.label}</span>
                      </span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                          {m.count} đơn · {m.qty} vé
                        </span>
                        <span style={{ fontWeight: 700, color: '#fff', minWidth: '100px', textAlign: 'right' }}>
                          {formatVND(m.revenue)}
                        </span>
                      </span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <div style={{ flex: 1, height: '8px', background: 'rgba(255,255,255,0.04)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{
                          width: `${m.pct}%`,
                          height: '100%',
                          background: m.gradient,
                          borderRadius: '4px',
                          transition: 'width 0.6s ease'
                        }}></div>
                      </div>
                      <span style={{ fontSize: '0.75rem', fontWeight: 600, color: m.color, minWidth: '42px', textAlign: 'right' }}>
                        {m.pct.toFixed(1)}%
                      </span>
                    </div>
                  </div>
                ))}

                {/* Tổng hợp tình trạng đơn */}
                <div style={{
                  marginTop: '8px',
                  background: 'rgba(255,255,255,0.02)',
                  border: '1px solid rgba(255,255,255,0.06)',
                  borderRadius: '10px',
                  padding: '14px 16px',
                  display: 'grid',
                  gridTemplateColumns: 'repeat(3, 1fr)',
                  gap: '12px',
                  textAlign: 'center'
                }}>
                  <div>
                    <div style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--success)' }}>{totalSuccess}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>Thành công</div>
                    <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginTop: '2px' }}>{formatVND(totalSuccessRevenue)}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '1.15rem', fontWeight: 700, color: '#f59e0b' }}>{pendingTickets.length}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>Chờ thanh toán</div>
                    <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginTop: '2px' }}>{formatVND(pendingTickets.reduce((s, t) => s + (t.totalPrice || 0), 0))}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '1.15rem', fontWeight: 700, color: '#ef4444' }}>{cancelledTickets.length}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>Đã hủy</div>
                    <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginTop: '2px' }}>{formatVND(cancelledTickets.reduce((s, t) => s + (t.totalPrice || 0), 0))}</div>
                  </div>
                </div>

                {/* Info banner */}
                <div style={{
                  background: 'rgba(99, 102, 241, 0.03)',
                  border: '1px solid rgba(99, 102, 241, 0.1)',
                  borderRadius: '10px',
                  padding: '12px',
                  display: 'flex',
                  gap: '10px',
                  fontSize: '0.8rem',
                  color: 'var(--text-secondary)'
                }}>
                  <Landmark size={18} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                  <div>
                    Thống kê tự động cập nhật realtime từ Firestore. Phương thức thanh toán mới sẽ tự xuất hiện khi có đơn.
                  </div>
                </div>
              </div>
            );
          })()}
        </div>
      </div>

      {/* Recent Orders log section */}
      <div className="glass-panel animate-fade-in" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ fontSize: '1.15rem', color: '#fff' }}>Đơn Mua Vé Gần Đây</h3>
          <span style={{
            fontSize: '0.8rem',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}>
            <ShoppingBag size={14} /> Hiển thị {recentTickets.length} giao dịch mới nhất
          </span>
        </div>

        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Mã Đơn Hàng</th>
                <th>Tên Sự Kiện</th>
                <th>Hạng Vé</th>
                <th>Khách Hàng (SĐT)</th>
                <th>Tổng Cộng</th>
                <th>Phương Thức</th>
                <th>Trạng Thái</th>
              </tr>
            </thead>
            <tbody>
              {recentTickets.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '30px' }}>
                    Chưa có vé nào được mua hoặc giao dịch phát sinh.
                  </td>
                </tr>
              ) : (
                recentTickets.map((ticket) => {
                  const statusClass = 
                    ticket.status === "Thành công" ? "badge-success" : 
                    ticket.status === "Đang chờ thanh toán" ? "badge-warning" : "badge-danger";

                  return (
                    <tr key={ticket.id}>
                      <td style={{ fontWeight: 600, fontFamily: 'monospace' }}>{ticket.orderCode || 'EZ-NONE'}</td>
                      <td>{ticket.eventName}</td>
                      <td>
                        <span style={{ background: 'rgba(255,255,255,0.05)', padding: '3px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>
                          {ticket.ticketTypeName} × {ticket.quantity}
                        </span>
                      </td>
                      <td>{ticket.customerPhone || 'Chưa cung cấp'}</td>
                      <td style={{ fontWeight: 600, color: '#fff' }}>{formatVND(ticket.totalPrice)}</td>
                      <td>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 500 }}>
                          {ticket.paymentMethod || 'N/A'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${statusClass}`}>
                          {ticket.status}
                        </span>
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
        .chart-dot-group:hover .chart-hover-ring {
          opacity: 0.15 !important;
          r: 12px !important;
        }
        .chart-dot-group:hover circle:first-child {
          r: 6px !important;
          fill: var(--primary) !important;
          stroke: #fff !important;
        }
      `}</style>

    </div>
  );
}
