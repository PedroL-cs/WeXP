import './styles/global.css';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import { HomePage } from './pages/Home';
import { BrowserRouter, Route, Routes } from 'react-router';
import SearchPage from './pages/Search';
import GamePage from './pages/Game';

function App() {
  return (
    <BrowserRouter>
      <Header />
      <Sidebar />

      <div className='layout'>
        <Routes>
          <Route path='/' element={<HomePage />} />
          <Route path='/search' element={<SearchPage />} />
          <Route path='/games/:id' element={<GamePage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
