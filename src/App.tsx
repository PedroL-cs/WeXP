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
import GuidePage from './pages/Guide';
import CreateRevisionPage from './pages/CreateRevision';
import CreateGuidePage from './pages/CreateGuide';
import UserProfilePage from './pages/UserProfile';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route path='/' element={<HomePage />} />
            <Route path='/search' element={<SearchPage />} />
            <Route path='/games/:id' element={<GamePage />} />
            <Route
              path='/games/:gameId/achievements/:achievementId/guide'
              element={<GuidePage />}
            />
            <Route
              path='/games/:gameId/achievements/:achievementId/guide/revise'
              element={<CreateRevisionPage />}
            />
            <Route
              path='/games/:gameId/achievements/:achievementId/guide/create'
              element={<CreateGuidePage />}
            />
            <Route path='/profile' element={<UserProfilePage />} />
            <Route path='/users/:userId' element={<UserProfilePage />} />
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
