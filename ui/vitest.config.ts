import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.test.{ts,tsx}',
        '**/*.spec.{ts,tsx}',
        'src/main.tsx',
        'src/App.tsx',
        'src/routes/index.tsx',
        'src/config/**',
        'src/lib/**',
        'src/features/**',
        'src/pages/AdminDashboardPage.tsx',
        'src/pages/AdminLoginPage.tsx',
        'src/pages/admin/**',
        'src/pages/HomePage.tsx',
        'src/components/layout/Header.tsx',
        'src/components/InstagramCarousel.tsx',
        'dist/**',
        '**/*.config.*',
        '**/vitest.config.*',
        '**/*.d.ts',
      ],
      thresholds: {
        lines: 80,
        functions: 80,
        branches: 80,
        statements: 80,
      },
    },
  },
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
});
