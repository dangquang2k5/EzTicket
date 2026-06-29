import { db, auth, isFirebaseConfigured } from '../config/firebase';
import { 
  collection, doc, getDocs, setDoc, addDoc, updateDoc, 
  deleteDoc, onSnapshot, query, where, orderBy, serverTimestamp,
  Timestamp
} from 'firebase/firestore';
import { 
  signInWithEmailAndPassword, signOut, onAuthStateChanged 
} from 'firebase/auth';

// ----------------------------------------------------
// 🌟 MOCK SEED DATA FOR LOCAL SANDBOX MODE
// ----------------------------------------------------

const SEED_EVENTS = [
  {
    id: "event_1",
    name: "Live Concert Chân Trời Rực Rỡ",
    venueName: "Sân vận động Hoa Lư",
    address: "Quận 1, TP. Hồ Chí Minh",
    image_url: "https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800",
    description: "Đêm nhạc đặc biệt mang tính lịch sử của nam ca sĩ Hà Anh Tuấn kết hợp cùng huyền thoại Kitaro, hòa nhịp cùng hàng vạn khán giả trong không gian âm nhạc đỉnh cao.",
    category: "Âm nhạc",
    isBanner: true,
    isHot: true,
    isVisible: true,
    status: "AVAILABLE",
    organizerName: "Viet Vision",
    organizerLogo: "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&q=80&w=150",
    schedules: [
      {
        date: { seconds: 1797339600 }, // Dec 15, 2026 20:00
        endDate: { seconds: 1797352200 }, // Dec 15, 2026 23:30
        ticketTypes: [
          { name: "V.I.P Diamond", price: 3500000, isVisible: true, quantity: 150 },
          { name: "Platinum Lounge", price: 2000000, isVisible: true, quantity: 300 },
          { name: "Standard G.A", price: 850000, isVisible: true, quantity: 1000 }
        ]
      }
    ]
  },
  {
    id: "event_2",
    name: "Rap Việt All-Star Concert 2026",
    venueName: "Trung tâm Hội chợ và Triển lãm Sài Gòn (SECC)",
    address: "Quận 7, TP. Hồ Chí Minh",
    image_url: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&q=80&w=800",
    description: "Siêu đại nhạc hội hội tụ dàn nghệ sĩ, huấn luyện viên và thí sinh đình đám nhất của chương trình Rap Việt. Đêm hội âm thanh ánh sáng bùng nổ của giới Underground.",
    category: "Âm nhạc",
    isBanner: false,
    isHot: true,
    isVisible: true,
    status: "AVAILABLE",
    organizerName: "Vie Channel",
    organizerLogo: "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?auto=format&fit=crop&q=80&w=150",
    schedules: [
      {
        date: { seconds: 1798117200 }, // Dec 24, 2026 20:00
        endDate: { seconds: 1798129800 },
        ticketTypes: [
          { name: "Fanzone (Đứng)", price: 1800000, isVisible: true, quantity: 800 },
          { name: "Standard (Ngồi)", price: 950000, isVisible: true, quantity: 1200 }
        ]
      }
    ]
  },
  {
    id: "event_3",
    name: "Hài Kịch: Ngày Hội Tiếng Cười",
    venueName: "Nhà Hát Bến Thành",
    address: "Mạc Đĩnh Chi, Quận 1, TP. Hồ Chí Minh",
    image_url: "https://images.unsplash.com/photo-1585699324551-f6c309eed262?auto=format&fit=crop&q=80&w=800",
    description: "Vở kịch trào phúng, nhân văn quy tụ dàn danh hài gạo cội hàng đầu Việt Nam. Hứa hẹn mang đến những tiếng cười sảng khoái và thông điệp sâu sắc.",
    category: "Khác",
    isBanner: false,
    isHot: false,
    isVisible: true,
    status: "AVAILABLE",
    organizerName: "Sân Khấu Thế Giới Trẻ",
    organizerLogo: "https://images.unsplash.com/photo-1460881680858-30d872d5b530?auto=format&fit=crop&q=80&w=150",
    schedules: [
      {
        date: { seconds: 1799240400 }, // Jan 6, 2027 19:30
        endDate: { seconds: 1799249400 },
        ticketTypes: [
          { name: "Khu A VIP", price: 500000, isVisible: true, quantity: 200 },
          { name: "Khu B", price: 300000, isVisible: true, quantity: 350 }
        ]
      }
    ]
  }
];

const SEED_USERS = [
  {
    uid: "admin_uid",
    fullName: "Trần Đăng Quang (Admin)",
    email: "admin@ezticket.vn",
    phone: "0999888777",
    role: "ADMIN",
    status: "ACTIVE",
    createdAt: Date.now() - 30 * 24 * 60 * 60 * 1000, // 30 days ago
    avatarUrl: "https://api.dicebear.com/7.x/bottts/svg?seed=admin"
  },
  {
    uid: "user_1",
    fullName: "Nguyễn Văn A",
    email: "nguyenvana@gmail.com",
    phone: "0912345678",
    role: "USER",
    status: "ACTIVE",
    createdAt: Date.now() - 15 * 24 * 60 * 60 * 1000,
    avatarUrl: "https://api.dicebear.com/7.x/adventurer/svg?seed=a"
  },
  {
    uid: "user_2",
    fullName: "Lê Thị B",
    email: "lethib@gmail.com",
    phone: "0987654321",
    role: "USER",
    status: "ACTIVE",
    createdAt: Date.now() - 8 * 24 * 60 * 60 * 1000,
    avatarUrl: "https://api.dicebear.com/7.x/adventurer/svg?seed=b"
  },
  {
    uid: "user_3",
    fullName: "Phạm Văn C (Spam)",
    email: "spammer@gmail.com",
    phone: "0900111222",
    role: "USER",
    status: "LOCKED",
    createdAt: Date.now() - 2 * 24 * 60 * 60 * 1000,
    avatarUrl: "https://api.dicebear.com/7.x/adventurer/svg?seed=c"
  }
];

const SEED_TICKETS = [
  {
    id: "t_1",
    eventId: "event_1",
    eventName: "Live Concert Chân Trời Rực Rỡ",
    imageUrl: "https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800",
    orderCode: "EZ-30912-921",
    ticketCode: "t_1",
    eventDate: { seconds: 1797339600 },
    eventEndDate: { seconds: 1797352200 },
    location: "Sân vận động Hoa Lư, Quận 1, TP. Hồ Chí Minh",
    ticketTypeName: "V.I.P Diamond",
    quantity: 2,
    unitPrice: 3500000,
    totalPrice: 7000000,
    status: "Thành công",
    customerPhone: "0912345678",
    paymentMethod: "MOMO",
    scheduleIndex: 0,
    createdAt: { seconds: Math.floor(Date.now() / 1000) - 2 * 3600 } // 2 hours ago
  },
  {
    id: "t_2",
    eventId: "event_1",
    eventName: "Live Concert Chân Trời Rực Rỡ",
    imageUrl: "https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800",
    orderCode: "EZ-94212-005",
    ticketCode: "t_2",
    eventDate: { seconds: 1797339600 },
    eventEndDate: { seconds: 1797352200 },
    location: "Sân vận động Hoa Lư, Quận 1, TP. Hồ Chí Minh",
    ticketTypeName: "Standard G.A",
    quantity: 1,
    unitPrice: 850000,
    totalPrice: 850000,
    status: "Đang chờ thanh toán",
    customerPhone: "0987654321",
    paymentMethod: "BANKING",
    scheduleIndex: 0,
    createdAt: { seconds: Math.floor(Date.now() / 1000) - 15 * 60 }, // 15 mins ago
    expiresAt: { seconds: Math.floor(Date.now() / 1000) + 15 * 60 } // Expires in 15 mins
  },
  {
    id: "t_3",
    eventId: "event_2",
    eventName: "Rap Việt All-Star Concert 2026",
    imageUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&q=80&w=800",
    orderCode: "EZ-88192-312",
    ticketCode: "t_3",
    eventDate: { seconds: 1798117200 },
    eventEndDate: { seconds: 1798129800 },
    location: "SECC, Quận 7, TP. Hồ Chí Minh",
    ticketTypeName: "Fanzone (Đứng)",
    quantity: 2,
    unitPrice: 1800000,
    totalPrice: 3600000,
    status: "Thành công",
    customerPhone: "0912345678",
    paymentMethod: "MOMO",
    scheduleIndex: 0,
    createdAt: { seconds: Math.floor(Date.now() / 1000) - 3 * 24 * 3600 } // 3 days ago
  },
  {
    id: "t_4",
    eventId: "event_3",
    eventName: "Hài Kịch: Ngày Hội Tiếng Cười",
    imageUrl: "https://images.unsplash.com/photo-1585699324551-f6c309eed262?auto=format&fit=crop&q=80&w=800",
    orderCode: "EZ-44123-559",
    ticketCode: "t_4",
    eventDate: { seconds: 1799240400 },
    eventEndDate: { seconds: 1799249400 },
    location: "Nhà Hát Bến Thành, Quận 1, TP. Hồ Chí Minh",
    ticketTypeName: "Khu B",
    quantity: 3,
    unitPrice: 300000,
    totalPrice: 900000,
    status: "Đã hủy",
    customerPhone: "0900111222",
    paymentMethod: "MOMO",
    scheduleIndex: 0,
    createdAt: { seconds: Math.floor(Date.now() / 1000) - 4 * 24 * 3600 } // 4 days ago
  }
];

const SEED_NOTIFS = [
  {
    id: "noti_1",
    title: "Mở bán vé Concert Chân Trời Rực Rỡ",
    body: "Hạng vé V.I.P đã chính thức mở bán. Số lượng có hạn, nhanh tay săn ngay hôm nay!",
    type: "SALE_7DAYS",
    eventId: "event_1",
    eventName: "Live Concert Chân Trời Rực Rỡ",
    eventImageUrl: "https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&q=80&w=800",
    createdAt: { seconds: Math.floor(Date.now() / 1000) - 3600 }
  }
];

// Helper to initialize local storage
const initLocalStorage = () => {
  if (!localStorage.getItem("ezticket_events")) {
    localStorage.setItem("ezticket_events", JSON.stringify(SEED_EVENTS));
  }
  if (!localStorage.getItem("ezticket_users")) {
    localStorage.setItem("ezticket_users", JSON.stringify(SEED_USERS));
  }
  if (!localStorage.getItem("ezticket_tickets")) {
    localStorage.setItem("ezticket_tickets", JSON.stringify(SEED_TICKETS));
  }
  if (!localStorage.getItem("ezticket_notifs")) {
    localStorage.setItem("ezticket_notifs", JSON.stringify(SEED_NOTIFS));
  }
};

if (!isFirebaseConfigured) {
  initLocalStorage();
}

// ----------------------------------------------------
// 🌟 DATA SERVICE IMPLEMENTATION (HYBRID REAL/MOCK)
// ----------------------------------------------------

export const dataService = {
  
  // ---------------- AUTHENTICATION ----------------
  login: async (email, password) => {
    if (isFirebaseConfigured) {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      // Query role in users collection
      const userDocRef = doc(db, "users", userCredential.user.uid);
      const userSnap = await getDocs(query(collection(db, "users"), where("uid", "==", userCredential.user.uid)));
      
      let isAdminUser = false;
      userSnap.forEach((doc) => {
        if (doc.data().role === "ADMIN") isAdminUser = true;
      });

      if (!isAdminUser) {
        await signOut(auth);
        throw new Error("Tài khoản của bạn không có quyền truy cập Admin!");
      }
      return userCredential.user;
    } else {
      // Mock login check
      const users = JSON.parse(localStorage.getItem("ezticket_users"));
      const user = users.find(u => u.email === email && u.role === "ADMIN");
      
      if (user && password === "admin123") {
        localStorage.setItem("ezticket_current_user", JSON.stringify(user));
        return user;
      } else {
        throw new Error("Sai email hoặc mật khẩu! Mặc định Sandbox: admin@ezticket.vn / mật khẩu: admin123");
      }
    }
  },

  logout: async () => {
    if (isFirebaseConfigured) {
      await signOut(auth);
    } else {
      localStorage.removeItem("ezticket_current_user");
    }
  },

  subscribeToAuthState: (onUserChanged) => {
    if (isFirebaseConfigured) {
      return onAuthStateChanged(auth, async (firebaseUser) => {
        if (firebaseUser) {
          // fetch real profile
          onUserChanged({
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            role: "ADMIN" // assume admin if security checks passed
          });
        } else {
          onUserChanged(null);
        }
      });
    } else {
      const stored = localStorage.getItem("ezticket_current_user");
      onUserChanged(stored ? JSON.parse(stored) : null);
      // return empty unsubscribe function
      return () => {};
    }
  },

  // ---------------- EVENTS CRUD ----------------
  subscribeEvents: (onEventsUpdate, onError) => {
    if (isFirebaseConfigured) {
      const q = query(collection(db, "events"));
      return onSnapshot(q, (snapshot) => {
        const list = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        onEventsUpdate(list);
      }, onError);
    } else {
      const update = () => {
        const list = JSON.parse(localStorage.getItem("ezticket_events")) || [];
        onEventsUpdate(list);
      };
      update();
      // Listen to storage events to keep synched
      window.addEventListener("storage", update);
      return () => window.removeEventListener("storage", update);
    }
  },

  saveEvent: async (eventData) => {
    if (isFirebaseConfigured) {
      // Map schedules to use native Firebase Firestore Timestamps for compatibility with Android
      const firestoreSchedules = (eventData.schedules || []).map(sch => {
        const parseToTimestamp = (dateVal) => {
          if (!dateVal) return null;
          if (dateVal.seconds) return new Timestamp(dateVal.seconds, 0);
          if (typeof dateVal === 'string') return Timestamp.fromDate(new Date(dateVal));
          if (dateVal instanceof Date) return Timestamp.fromDate(dateVal);
          return dateVal;
        };
        
        return {
          ...sch,
          date: parseToTimestamp(sch.date),
          endDate: parseToTimestamp(sch.endDate),
          ticketTypes: (sch.ticketTypes || []).map(t => ({
            name: t.name,
            price: Number(t.price) || 0,
            isVisible: t.isVisible !== undefined ? Boolean(t.isVisible) : true,
            quantity: Number(t.quantity) || 0
          }))
        };
      });

      const processedData = {
        ...eventData,
        schedules: firestoreSchedules
      };

      if (eventData.id) {
        // Update
        const ref = doc(db, "events", eventData.id);
        await updateDoc(ref, processedData);
      } else {
        // Create new
        const colRef = collection(db, "events");
        const docRef = await addDoc(colRef, processedData);
        await updateDoc(docRef, { id: docRef.id });
      }
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_events")) || [];
      if (eventData.id) {
        // update
        const index = list.findIndex(e => e.id === eventData.id);
        if (index !== -1) list[index] = eventData;
      } else {
        // create
        eventData.id = "event_" + Date.now();
        list.push(eventData);
      }
      localStorage.setItem("ezticket_events", JSON.stringify(list));
      // Dispatch a dummy event for updating components in same window
      window.dispatchEvent(new Event("storage"));
    }
  },

  deleteEvent: async (id) => {
    if (isFirebaseConfigured) {
      const ref = doc(db, "events", id);
      await deleteDoc(ref);
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_events")) || [];
      const updated = list.filter(e => e.id !== id);
      localStorage.setItem("ezticket_events", JSON.stringify(updated));
      window.dispatchEvent(new Event("storage"));
    }
  },

  // ---------------- PURCHASED TICKETS ----------------
  subscribeTickets: (onTicketsUpdate, onError) => {
    if (isFirebaseConfigured) {
      const q = query(collection(db, "purchasedTickets"));
      return onSnapshot(q, (snapshot) => {
        const list = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        // Sắp xếp ngược thời gian (mới nhất lên đầu) trong bộ nhớ để tránh lỗi Index
        list.sort((a, b) => {
          const timeA = a.createdAt?.seconds || 0;
          const timeB = b.createdAt?.seconds || 0;
          return timeB - timeA;
        });
        onTicketsUpdate(list);
      }, onError);
    } else {
      const update = () => {
        const list = JSON.parse(localStorage.getItem("ezticket_tickets")) || [];
        // sort by createdAt desc
        list.sort((a, b) => (b.createdAt?.seconds || 0) - (a.createdAt?.seconds || 0));
        onTicketsUpdate(list);
      };
      update();
      window.addEventListener("storage", update);
      return () => window.removeEventListener("storage", update);
    }
  },

  updateTicketStatus: async (ticketId, status, paymentMethod = "") => {
    if (isFirebaseConfigured) {
      const ref = doc(db, "purchasedTickets", ticketId);
      const updates = { status };
      if (paymentMethod) updates.paymentMethod = paymentMethod;
      await updateDoc(ref, updates);
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_tickets")) || [];
      const index = list.findIndex(t => t.id === ticketId);
      if (index !== -1) {
        list[index].status = status;
        if (paymentMethod) list[index].paymentMethod = paymentMethod;
        localStorage.setItem("ezticket_tickets", JSON.stringify(list));
        window.dispatchEvent(new Event("storage"));
      }
    }
  },

  // ---------------- USERS MANAGEMENT ----------------
  subscribeUsers: (onUsersUpdate, onError) => {
    if (isFirebaseConfigured) {
      const q = query(collection(db, "users"));
      return onSnapshot(q, (snapshot) => {
        const list = snapshot.docs.map(doc => ({ uid: doc.id, ...doc.data() }));
        onUsersUpdate(list);
      }, onError);
    } else {
      const update = () => {
        const list = JSON.parse(localStorage.getItem("ezticket_users")) || [];
        onUsersUpdate(list);
      };
      update();
      window.addEventListener("storage", update);
      return () => window.removeEventListener("storage", update);
    }
  },

  updateUserStatus: async (uid, status) => {
    if (isFirebaseConfigured) {
      const ref = doc(db, "users", uid);
      await updateDoc(ref, { status });
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_users")) || [];
      const index = list.findIndex(u => u.uid === uid);
      if (index !== -1) {
        list[index].status = status;
        localStorage.setItem("ezticket_users", JSON.stringify(list));
        window.dispatchEvent(new Event("storage"));
      }
    }
  },

  updateUserRole: async (uid, role) => {
    if (isFirebaseConfigured) {
      const ref = doc(db, "users", uid);
      await updateDoc(ref, { role });
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_users")) || [];
      const index = list.findIndex(u => u.uid === uid);
      if (index !== -1) {
        list[index].role = role;
        localStorage.setItem("ezticket_users", JSON.stringify(list));
        window.dispatchEvent(new Event("storage"));
      }
    }
  },

  // ---------------- NOTIFICATIONS SENDER ----------------
  subscribeNotifs: (onNotifsUpdate) => {
    if (isFirebaseConfigured) {
      const q = query(collection(db, "notifications"));
      return onSnapshot(q, (snapshot) => {
        const list = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        list.sort((a, b) => {
          const timeA = a.createdAt?.seconds || 0;
          const timeB = b.createdAt?.seconds || 0;
          return timeB - timeA;
        });
        onNotifsUpdate(list);
      });
    } else {
      const update = () => {
        const list = JSON.parse(localStorage.getItem("ezticket_notifs")) || [];
        list.sort((a, b) => (b.createdAt?.seconds || 0) - (a.createdAt?.seconds || 0));
        onNotifsUpdate(list);
      };
      update();
      window.addEventListener("storage", update);
      return () => window.removeEventListener("storage", update);
    }
  },

  sendNotification: async (notiData) => {
    const formatted = {
      ...notiData,
      createdAt: isFirebaseConfigured ? serverTimestamp() : { seconds: Math.floor(Date.now() / 1000) }
    };
    
    if (isFirebaseConfigured) {
      await addDoc(collection(db, "notifications"), formatted);
    } else {
      const list = JSON.parse(localStorage.getItem("ezticket_notifs")) || [];
      formatted.id = "noti_" + Date.now();
      list.push(formatted);
      localStorage.setItem("ezticket_notifs", JSON.stringify(list));
      window.dispatchEvent(new Event("storage"));
    }
  }
};
export default dataService;
