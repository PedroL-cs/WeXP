import './styles/global.css';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import { HomePage } from './pages/Home';
import { BrowserRouter, Route, Routes } from 'react-router';
import SearchPage from './pages/Search';

function App() {
  return (
    <BrowserRouter>
      <Header />
      <Sidebar />

      <div className='layout'>
        <Routes>
          <Route path='/' element={<HomePage />} />
          <Route path='/search' element={<SearchPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
