import { test, expect } from '@playwright/test';

const E2E_ADMIN_USER = process.env.E2E_ADMIN_USER ?? '';
const E2E_ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? '';
const hasAdminCredentials = Boolean(E2E_ADMIN_USER && E2E_ADMIN_PASSWORD);

test.describe('Login admin', () => {
  test('credenciais inválidas exibem mensagem de erro', async ({ page }) => {
    await page.goto('/admin/login');
    await page.getByLabel(/Usuário/i).fill('usuario-invalido');
    await page.getByLabel(/Senha/i).fill('senha-errada');
    await page.getByRole('button', { name: 'Entrar' }).click();
    const alert = page.getByRole('alert');
    await expect(alert).toBeVisible();
    await expect(alert).toContainText(/Credenciais inválidas|Erro ao entrar/);
    await expect(page).toHaveURL(/\/admin\/login/);
  });

  test('login com sucesso redireciona para o dashboard', async ({ page }) => {
    test.skip(!hasAdminCredentials, 'E2E_ADMIN_USER e E2E_ADMIN_PASSWORD não configurados');
    await page.goto('/admin/login');
    await page.getByLabel(/Usuário/i).fill(E2E_ADMIN_USER);
    await page.getByLabel(/Senha/i).fill(E2E_ADMIN_PASSWORD);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/admin\/?$/);
    await expect(
      page.getByRole('heading', { name: 'Painel' })
    ).toBeVisible();
  });
});
