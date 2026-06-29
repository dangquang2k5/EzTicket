import React from 'react';

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught an error:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ 
          padding: '30px', 
          background: 'rgba(239, 68, 68, 0.08)', 
          border: '1px solid rgba(239, 68, 68, 0.2)',
          color: '#fca5a5', 
          borderRadius: '12px', 
          margin: '30px',
          fontFamily: 'monospace',
          backdropFilter: 'blur(16px)'
        }} className="glass-panel">
          <h2 style={{ color: '#ef4444', marginBottom: '14px', fontSize: '1.4rem' }}>⚠️ Đã xảy ra lỗi giao diện!</h2>
          <p style={{ marginBottom: '10px', fontWeight: 'bold' }}>Chi tiết lỗi:</p>
          <pre style={{ 
            whiteSpace: 'pre-wrap', 
            background: 'rgba(0,0,0,0.3)', 
            padding: '15px', 
            borderRadius: '6px',
            fontSize: '0.85rem',
            overflowX: 'auto',
            marginBottom: '20px',
            color: '#fff'
          }}>{this.state.error?.toString()}</pre>
          
          <button 
            onClick={() => this.setState({ hasError: false, error: null })} 
            className="btn-primary" 
            style={{ display: 'inline-flex' }}
          >
            Tải lại Giao diện
          </button>
        </div>
      );
    }

    return this.props.children; 
  }
}
