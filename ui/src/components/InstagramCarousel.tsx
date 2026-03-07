import { useEffect } from 'react';

const INSTAGRAM_EMBED_SCRIPT = 'https://www.instagram.com/embed.js';

declare global {
  interface Window {
    instgrm?: { Embeds: { process: () => void } };
  }
}

function loadInstagramEmbed(): Promise<void> {
  if (window.instgrm) return Promise.resolve();
  return new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[src="${INSTAGRAM_EMBED_SCRIPT}"]`);
    if (existing) {
      if (window.instgrm) resolve();
      else existing.addEventListener('load', () => resolve());
      return;
    }
    const script = document.createElement('script');
    script.async = true;
    script.src = INSTAGRAM_EMBED_SCRIPT;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Instagram embed failed to load'));
    document.body.appendChild(script);
  });
}

interface InstagramCarouselProps {
  postUrls: readonly string[];
  className?: string;
}

export function InstagramCarousel({ postUrls, className = '' }: InstagramCarouselProps) {
  useEffect(() => {
    if (postUrls.length === 0) return;
    loadInstagramEmbed()
      .then(() => window.instgrm?.Embeds?.process())
      .catch(() => {});
  }, [postUrls]);

  if (postUrls.length === 0) return null;

  return (
    <div className={`w-full overflow-hidden ${className}`}>
      <div
        className="flex gap-4 overflow-x-auto snap-x snap-mandatory snap-center py-4 scroll-smooth px-2 md:px-4 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      >
        {postUrls.map((url) => (
          <div
            key={url}
            className="min-w-[280px] max-w-[340px] flex-shrink-0 snap-center"
          >
            <blockquote
              className="instagram-media overflow-hidden rounded-lg border border-zinc-800 bg-zinc-900"
              data-instgrm-permalink={url}
              data-instgrm-version="14"
            >
              <a href={url} target="_blank" rel="noopener noreferrer">
                Ver no Instagram
              </a>
            </blockquote>
          </div>
        ))}
      </div>
    </div>
  );
}
