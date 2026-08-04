import React, { useEffect, useState } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import { getWelcomeMessage } from './services/api';
import './assets/styles.css';

function App() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    getWelcomeMessage()
      .then(data => setMessage(data.message))
      .catch(() => setMessage('Không thể kết nối đến server'));
  }, []);

  return (
    <div>
      <Navbar />
      <Home />
      <section style={{ padding: '24px' }}>
        <h3>Phản hồi từ backend</h3>
        <p>{message || 'Đang tải...'}</p>
      </section>
    </div>
  );
}

export default App;
