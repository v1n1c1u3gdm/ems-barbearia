import { useEffect } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';

import { PublicFooter } from './PublicFooter';
import { PublicHeader } from './PublicHeader';

const SECTION_HASHES = ['#inicio', '#servicos', '#contato'];

export function PageLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (
      location.pathname === '/agendar' &&
      location.hash &&
      SECTION_HASHES.includes(location.hash)
    ) {
      navigate(`/${location.hash}`, { replace: true });
    }
  }, [location.pathname, location.hash, navigate]);

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      <PublicHeader />
      <main>
        <Outlet />
      </main>
      <PublicFooter />
    </div>
  );
}
