import { Outlet } from 'react-router-dom';
import { PublicFooter } from './PublicFooter';
import { PublicHeader } from './PublicHeader';

export function PageLayout() {
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
