import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

import { InstagramCarousel } from '@/components/InstagramCarousel';
import { CONTACT } from '@/config/contact';
import { fetchPublicServicos } from '@/features/admin/api';

export function LandingPage() {
  const location = useLocation();

  useEffect(() => {
    const hash = location.hash?.slice(1);
    if (!hash) return;
    const el = document.getElementById(hash);
    if (el) {
      requestAnimationFrame(() => {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    }
  }, [location.pathname, location.hash]);

  const { data: servicos = [], isLoading, isError } = useQuery({
    queryKey: ['public', 'servicos', 'landing'],
    queryFn: () => fetchPublicServicos(),
  });
  return (
    <>
      <section
        id="inicio"
        className="relative flex min-h-[85vh] flex-col items-center justify-center bg-black px-4 py-24 text-center"
      >
        <div
          className="absolute inset-0 bg-center bg-no-repeat opacity-[0.2]"
          style={{
            backgroundImage: 'url(/logo-ems-barbearia.svg)',
            backgroundSize: '70%',
          }}
        />
        <div className="relative z-10 max-w-3xl">
          <h1 className="mb-6 text-4xl font-bold tracking-tight text-white sm:text-5xl md:text-6xl">
            Barbearia para quem dita o próprio ritmo
          </h1>
          <p className="mb-10 text-lg text-zinc-300 sm:text-xl">
            Muito além do corte. Cuidado, estilo e presença em um só lugar.
          </p>
          <a
            href={CONTACT.whatsappUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-block rounded-md bg-amber-500 px-8 py-3 text-lg font-medium text-zinc-950 transition hover:bg-amber-400"
          >
            Agendar
          </a>
          {CONTACT.instagramPostUrls.length > 0 && (
            <div className="relative z-10 mt-16 w-full max-w-4xl">
              <p className="mb-4 text-sm font-medium uppercase tracking-wider text-zinc-500">
                Últimas do Instagram
              </p>
              <InstagramCarousel postUrls={CONTACT.instagramPostUrls} />
            </div>
          )}
        </div>
      </section>

      <section
        id="servicos"
        className="border-t border-zinc-800 bg-zinc-900/50 px-4 py-20"
      >
        <div className="container mx-auto max-w-5xl">
          <h2 className="mb-4 text-center text-3xl font-bold text-white">
            Nossos serviços
          </h2>
          <p className="mb-12 text-center text-zinc-400">
            Serviços de barbearia e cuidado pessoal com qualidade e agilidade.
            Preços variam de acordo com o procedimento e tamanho de cabelo.
          </p>
          {isLoading && (
            <p className="text-center text-zinc-400">Carregando serviços...</p>
          )}
          {isError && (
            <p className="text-center text-zinc-400">
              Não foi possível carregar os serviços. Tente novamente mais tarde.
            </p>
          )}
          {!isLoading && !isError && servicos.length > 0 && (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {servicos.map((s) => (
                <div
                  key={s.id}
                  className="rounded-lg border border-zinc-800 bg-zinc-900/80 p-6 transition hover:border-zinc-700"
                >
                  <h3 className="mb-2 text-lg font-semibold text-white">
                    {s.titulo}
                  </h3>
                  <p className="text-sm text-zinc-400">
                    {s.descricao ?? 'Serviço com qualidade EMS Barbearia.'}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      <section
        id="contato"
        className="border-t border-zinc-800 px-4 py-20"
      >
        <div className="container mx-auto max-w-4xl">
          <h2 className="mb-4 text-center text-3xl font-bold text-white">
            Agende seu horário
          </h2>
          <p className="mb-8 text-center text-zinc-400">
            Fale conosco pelo WhatsApp ou agende pela Trinks.
          </p>
          <div className="mb-10 flex flex-wrap justify-center gap-4">
            <a
              href={CONTACT.whatsappUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-md bg-emerald-600 px-6 py-3 font-medium text-white transition hover:bg-emerald-500"
            >
              Fale conosco
            </a>
            <a
              href={CONTACT.bookingUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-md bg-amber-500 px-6 py-3 font-medium text-zinc-950 transition hover:bg-amber-400"
            >
              Agende conosco
            </a>
          </div>
          <div className="aspect-video w-full overflow-hidden rounded-lg border border-zinc-800">
            <iframe
              title="Localização EMS Barbearia"
              src={CONTACT.mapEmbedUrl}
              width="100%"
              height="100%"
              className="h-full min-h-[300px] w-full border-0"
              allowFullScreen
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
            />
          </div>
        </div>
      </section>
    </>
  );
}
