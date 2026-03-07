import { Link } from 'react-router-dom';
import { CONTACT } from '@/config/contact';

export function PublicFooter() {
  return (
    <footer className="border-t border-zinc-800 bg-zinc-950">
      <div className="container mx-auto flex flex-col items-center justify-between gap-4 px-4 py-8 sm:flex-row">
        <img
          src="/ems-barbearia.svg"
          alt="EMS Barbearia"
          className="h-8 w-auto opacity-90"
          width="120"
          height="30"
        />
        <div className="flex items-center gap-6 text-sm text-zinc-400">
          <a
            href={CONTACT.mapUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="transition hover:text-white"
          >
            {CONTACT.address}
          </a>
          <Link to="/admin" className="transition hover:text-white">
            Admin
          </Link>
        </div>
      </div>
      <div className="border-t border-zinc-800 px-4 py-3 text-center text-xs text-zinc-500">
        © {new Date().getFullYear()} EMS Barbearia
      </div>
    </footer>
  );
}
