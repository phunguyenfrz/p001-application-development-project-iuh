const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3000';

export async function getWelcomeMessage() {
  const response = await fetch(`${API_BASE_URL}/api/hello`);
  if (!response.ok) {
    throw new Error('Không thể lấy dữ liệu từ server');
  }
  return response.json();
}
