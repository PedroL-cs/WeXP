import { Outlet } from 'react-router';
import { useState } from 'react';
import Header from '../Header';
import Sidebar from '../Sidebar';
import Breadcrumbs from '../Breadcrumbs';
import Footer from '../Footer';

function Layout() {
  const [search, setSearch] = useState('');
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  return (
    <>
      <Header
        search={search}
        setSearch={setSearch}
        isSearchOpen={isSearchOpen}
        setIsSearchOpen={setIsSearchOpen}
      />
      <Sidebar />

      <div className='layout'>
        <div className='layout-content'>
          <Breadcrumbs />
          <Outlet />
        </div>
        <Footer />
      </div>
    </>
  );
}

export default Layout;
