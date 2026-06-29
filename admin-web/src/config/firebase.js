import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

// Paste your Firebase web app configuration here from the Firebase Console:
// Settings -> General -> Your apps -> Web apps -> SDK setup and configuration
const firebaseConfig = {
  apiKey: "AIzaSyBZ2nN-Trzf3DbIEA4J9b3f68wriiGCvqA",
  authDomain: "ezticket-50343.firebaseapp.com",
  projectId: "ezticket-50343",
  storageBucket: "ezticket-50343.firebasestorage.app",
  messagingSenderId: "72820498326",
  appId: "1:72820498326:web:9a8aa8b7fc4d6807776337",
  measurementId: "G-W2J8Z3V67D"
};

// Check if the user has replaced the default placeholders with real credentials
export const isFirebaseConfigured = 
  firebaseConfig.apiKey && 
  !firebaseConfig.apiKey.includes("YOUR_API_KEY_HERE");

let app = null;
let auth = null;
let db = null;

if (isFirebaseConfigured) {
  try {
    app = initializeApp(firebaseConfig);
    auth = getAuth(app);
    db = getFirestore(app);
    console.log("🔥 Firebase connected successfully in REAL mode.");
  } catch (error) {
    console.error("❌ Failed to initialize Firebase: ", error);
  }
} else {
  console.log("💻 Running in LOCAL SANDBOX MODE (using LocalStorage). To use real Firestore, update src/config/firebase.js");
}

export { auth, db };
export default firebaseConfig;
