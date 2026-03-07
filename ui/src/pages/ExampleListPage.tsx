import { useQuery } from '@tanstack/react-query';
import { fetchExamples } from '@/features/example/api';
import { ExampleForm } from '@/features/example/components/ExampleForm';

export function ExampleListPage() {
  const { data: items, isLoading, error } = useQuery({
    queryKey: ['examples'],
    queryFn: () => fetchExamples(),
  });

  if (isLoading) return <p className="text-zinc-400">Carregando...</p>;
  if (error) return <p className="text-red-400">Erro: {(error as Error).message}</p>;

  return (
    <div className="container mx-auto space-y-8 px-4 py-8">
      <h1 className="text-2xl font-bold text-white">Examples</h1>

      <section>
        <h2 className="mb-2 text-lg font-medium text-zinc-300">Criar</h2>
        <ExampleForm />
      </section>

      <section>
        <h2 className="mb-2 text-lg font-medium text-zinc-300">Lista</h2>
        {items?.length === 0 ? (
          <p className="text-zinc-500">Nenhum item.</p>
        ) : (
          <ul className="divide-y divide-zinc-800 rounded-lg border border-zinc-800 bg-zinc-900/80">
            {items?.map((item) => (
              <li key={item.id} className="flex items-center justify-between px-4 py-3">
                <span className="font-medium text-white">{item.name}</span>
                <span className="text-sm text-zinc-500">
                  {new Date(item.createdAt).toLocaleString()}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
