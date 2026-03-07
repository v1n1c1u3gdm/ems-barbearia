import { z } from 'zod';

export const exampleFormSchema = z.object({
  name: z.string().min(1, 'Nome é obrigatório').max(255, 'Máximo 255 caracteres'),
});

export type ExampleFormValues = z.infer<typeof exampleFormSchema>;
