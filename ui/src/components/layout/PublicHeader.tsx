import { Link } from 'react-router-dom';
import { CONTACT } from '@/config/contact';

export function PublicHeader() {
  return (
    <header className="sticky top-0 z-50 border-b border-zinc-800 bg-zinc-950/95 backdrop-blur">
      <nav className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2" aria-label="EMS Barbearia - Início">
          <img
            src="/ems-barbearia.svg"
            alt="EMS Barbearia"
            className="h-9 w-auto"
            width="140"
            height="36"
          />
        </Link>
        <div className="flex items-center gap-6">
          <a
            href="#inicio"
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Início
          </a>
          <a
            href="#servicos"
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Serviços
          </a>
          <a
            href="#contato"
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Contato
          </a>
          <a
            href={CONTACT.whatsappUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="rounded-md bg-amber-500 px-4 py-2 text-sm font-medium text-zinc-950 transition hover:bg-amber-400"
          >
            Agendar
          </a>
        </div>
      </nav>
    </header>
  );
}
