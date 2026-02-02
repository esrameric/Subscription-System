-- Subscription Service - Seed Data
-- Bu dosya uygulama başlatıldığında otomatik olarak çalışır

-- Eğer tabloda veri yoksa ekle (duplicate key hatası önleme)
INSERT INTO offers (name, description, price, period, status, created_at, updated_at)
SELECT 'Basic Monthly', 'Temel özellikler, aylık abonelik', 29.99, 1, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM offers WHERE name = 'Basic Monthly');

INSERT INTO offers (name, description, price, period, status, created_at, updated_at)
SELECT 'Premium Monthly', 'Tüm özellikler, öncelikli destek, aylık abonelik', 79.99, 1, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM offers WHERE name = 'Premium Monthly');

INSERT INTO offers (name, description, price, period, status, created_at, updated_at)
SELECT 'Basic Annual', 'Temel özellikler, yıllık abonelik (2 ay bedava)', 299.99, 12, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM offers WHERE name = 'Basic Annual');

INSERT INTO offers (name, description, price, period, status, created_at, updated_at)
SELECT 'Premium Annual', 'Tüm özellikler, öncelikli destek, yıllık abonelik (2 ay bedava)', 799.99, 12, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM offers WHERE name = 'Premium Annual');

INSERT INTO offers (name, description, price, period, status, created_at, updated_at)
SELECT 'Enterprise', 'Kurumsal çözümler, özel destek, sınırsız kullanıcı', 1999.99, 12, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM offers WHERE name = 'Enterprise');
