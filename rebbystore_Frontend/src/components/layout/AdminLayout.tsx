import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { AdminSidebar } from '../admin/AdminSidebar';
import { ToastContainer } from '../ui/ToastContainer';

export function AdminLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <div className="min-h-screen flex bg-rs-bg">
      <AdminSidebar mobileOpen={mobileOpen} onMobileClose={() => setMobileOpen(false)} />
      <div className="flex-1 min-w-0">
        <Outlet context={{ onMenuClick: () => setMobileOpen(true) }} />
      </div>
      <ToastContainer />
    </div>
  );
}
