import './styles/global.css';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import { HomePage } from './pages/Home';

function App() {
  return (
    <>
      <Header />
      <Sidebar />

      <div className='layout'>
        <HomePage />
      </div>
    </>
  );
}

export default App;
