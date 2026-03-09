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

  test('link Agendar no header leva a /agendar e exibe título', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('navigation').getByRole('link', { name: 'Agendar' }).click();
    await expect(page).toHaveURL(/\/agendar$/);
    await expect(
      page.getByRole('heading', { name: 'Agendar horário' })
    ).toBeVisible();
  });
});

// Para rodar: suba o app (docker compose ou cd ui && npm run dev) e execute no diretório tests:
//   npm run test -- --grep "Menu a partir"
// Contra Docker na porta 80: BASE_URL=http://localhost npm run test -- --grep "Menu a partir"
// Se o router estiver quebrado, estes testes falham: pathname continua /agendar ou a seção da landing não aparece.
test.describe('Menu a partir de /agendar (router deve navegar para / com hash)', () => {
  test('em /agendar, clicar em Contato navega para /#contato e exibe seção da landing', async ({
    page,
  }) => {
    await page.goto('/agendar');
    await expect(page).toHaveURL(/\/agendar/);
    await page.getByRole('navigation').getByRole('link', { name: 'Contato' }).click();
    const url = new URL(page.url());
    expect(url.pathname).toBe('/');
    expect(url.hash).toBe('#contato');
    await expect(page.getByRole('heading', { name: 'Agende seu horário' })).toBeVisible();
    await expect(page.locator('#contato')).toBeInViewport();
  });

  test('em /agendar, clicar em Serviços navega para /#servicos e exibe seção da landing', async ({
    page,
  }) => {
    await page.goto('/agendar');
    await expect(page).toHaveURL(/\/agendar/);
    await page.getByRole('navigation').getByRole('link', { name: 'Serviços' }).click();
    const url = new URL(page.url());
    expect(url.pathname).toBe('/');
    expect(url.hash).toBe('#servicos');
    await expect(page.getByRole('heading', { name: 'Nossos serviços' })).toBeVisible();
    await expect(page.locator('#servicos')).toBeInViewport();
  });

  test('em /agendar, clicar em Início navega para /#inicio e exibe seção da landing', async ({
    page,
  }) => {
    await page.goto('/agendar');
    await expect(page).toHaveURL(/\/agendar/);
    await page
      .getByRole('navigation')
      .locator('a[href*="inicio"]')
      .filter({ hasText: 'Início' })
      .click();
    const url = new URL(page.url());
    expect(url.pathname).toBe('/');
    expect(url.hash).toBe('#inicio');
    await expect(
      page.getByRole('heading', { name: /barbearia para quem dita o próprio ritmo/i })
    ).toBeVisible();
    await expect(page.locator('#inicio')).toBeInViewport();
  });
});

test.describe('Rotas diretas', () => {
  test('acesso a /admin redireciona para login', async ({ page }) => {
    await page.goto('/admin');
    await expect(page).toHaveURL(/\/admin\/login/);
    await expect(page.getByRole('heading', { name: 'Acesso restrito' })).toBeVisible();
  });
});

test.describe('Footer', () => {
  test('home exibe footer com copyright', async ({ page }) => {
    await page.goto('/');
    const footer = page.locator('footer');
    await expect(footer).toBeVisible();
    await expect(footer.getByText(/©\s*\d{4}/)).toBeVisible();
  });
});

test.describe('Proteção admin', () => {
  test('acesso direto a /admin/servicos sem login redireciona para login', async ({
    page,
  }) => {
    await page.goto('/admin/servicos');
    await expect(page).toHaveURL(/\/admin\/login/);
  });

  test('acesso direto a /admin/clientes sem login redireciona para login', async ({
    page,
  }) => {
    await page.goto('/admin/clientes');
    await expect(page).toHaveURL(/\/admin\/login/);
  });
});
