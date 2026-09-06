import './styles/global.css';
import { HomePage } from './pages/Home';
import { BrowserRouter, Route, Routes } from 'react-router';
import SearchPage from './pages/Search';
import GamePage from './pages/Game';
import NotFound from './pages/NotFound';
import Layout from './components/Layout';
import RegisterPage from './pages/Register';
import LoginPage from './pages/Login';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path='/' element={<HomePage />} />
          <Route path='/search' element={<SearchPage />} />
          <Route path='/game/:id' element={<GamePage />} />
          <Route path='*' element={<NotFound />} />
        </Route>

        <Route path='/login' element={<LoginPage />} />
        <Route path='/register' element={<RegisterPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
