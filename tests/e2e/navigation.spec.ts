import { test, expect } from '@playwright/test';

test.describe('Navegação pública', () => {
  test('carrega a home e exibe o título principal', async ({ page }) => {
    await page.goto('/');
    await expect(
      page.getByRole('heading', { name: /barbearia para quem dita o próprio ritmo/i })
    ).toBeVisible();
  });

  test('link do logo leva à home', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: /EMS Barbearia - Início/i }).click();
    await expect(page).toHaveURL(/\/$/);
  });

  test('nav Início rola para a seção início', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'Início' }).first().click();
    await expect(page.locator('#inicio')).toBeInViewport();
  });

  test('nav Serviços rola para a seção e exibe título', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'Serviços' }).click();
    await expect(page.locator('#servicos')).toBeInViewport();
    await expect(page.getByRole('heading', { name: 'Nossos serviços' })).toBeVisible();
  });

  test('nav Contato rola para a seção e exibe título', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('link', { name: 'Contato' }).click();
    await expect(page.locator('#contato')).toBeInViewport();
    await expect(page.getByRole('heading', { name: 'Agende seu horário' })).toBeVisible();
  });

  test('home não exibe link para Admin', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: 'Admin' })).not.toBeVisible();
  });
});

test.describe('Rotas diretas', () => {
  test('acesso a /examples exibe a página de examples', async ({ page }) => {
    await page.goto('/examples');
    await expect(
      page.getByRole('heading', { name: 'Examples' }).or(page.getByText('Carregando...'))
    ).toBeVisible({ timeout: 10000 });
  });

  test('acesso a /admin redireciona para login', async ({ page }) => {
    await page.goto('/admin');
    await expect(page).toHaveURL(/\/admin\/login/);
    await expect(page.getByRole('heading', { name: 'Acesso restrito' })).toBeVisible();
  });
});
