import { Outlet } from 'react-router';
import Header from '../Header';
import Sidebar from '../Sidebar';

function Layout() {
  return (
    <>
      <Header />
      <Sidebar />

      <div className='layout'>
        <Outlet />
      </div>
    </>
  );
}

export default Layout;
