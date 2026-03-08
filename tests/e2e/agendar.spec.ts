import { test, expect } from '@playwright/test';

test.describe('Página Agendar', () => {
  test('acesso direto a /agendar exibe gate de autenticação', async ({ page }) => {
    await page.goto('/agendar');
    await expect(
      page.getByRole('heading', { name: 'Agendar horário' })
    ).toBeVisible();
    await expect(
      page.getByRole('heading', { name: 'Entrar ou cadastrar' })
    ).toBeVisible();
    await expect(
      page.getByRole('button', { name: /Criar conta com email/i })
    ).toBeVisible();
  });
});
