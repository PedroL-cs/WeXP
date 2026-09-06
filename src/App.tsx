import './styles/global.css';
import { HomePage } from './pages/Home';
import { BrowserRouter, Route, Routes } from 'react-router';
import SearchPage from './pages/Search';
import GamePage from './pages/Game';
import NotFound from './pages/NotFound';
import Layout from './components/Layout';
import RegisterPage from './pages/Register';
import LoginPage from './pages/Login';
import { AuthProvider } from './contexts/AuthContext';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route path='/' element={<HomePage />} />
            <Route path='/search' element={<SearchPage />} />
            <Route path='/games/:id' element={<GamePage />} />
            <Route path='*' element={<NotFound />} />
          </Route>

          <Route path='/login' element={<LoginPage />} />
          <Route path='/register' element={<RegisterPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
