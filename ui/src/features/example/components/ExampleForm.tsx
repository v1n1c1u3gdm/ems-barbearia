import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createExample } from '../api';
import { exampleFormSchema, type ExampleFormValues } from '../schemas';

export function ExampleForm() {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ExampleFormValues>({
    resolver: zodResolver(exampleFormSchema),
    defaultValues: { name: '' },
  });

  const createMutation = useMutation({
    mutationFn: createExample,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['examples'] });
      reset();
    },
  });

  return (
    <form
      onSubmit={handleSubmit((data) => createMutation.mutate(data))}
      className="flex gap-2 flex-wrap items-end"
    >
      <div className="flex-1 min-w-[200px]">
        <label htmlFor="name" className="mb-1 block text-sm font-medium text-zinc-400">
          Nome
        </label>
        <input
          id="name"
          type="text"
          {...register('name')}
          className="block w-full rounded-md border border-zinc-700 bg-zinc-900 px-3 py-2 text-white shadow-sm focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-400">{errors.name.message}</p>
        )}
      </div>
      <button
        type="submit"
        disabled={createMutation.isPending}
        className="rounded-md bg-amber-500 px-4 py-2 text-zinc-950 hover:bg-amber-400 disabled:opacity-50"
      >
        {createMutation.isPending ? 'Salvando...' : 'Criar'}
      </button>
    </form>
  );
}
