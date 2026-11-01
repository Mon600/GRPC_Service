-- Seed account: admin@example.com / adminpass.
-- Password hash generated with Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().
-- This account has no special role: the users table has no role column.
INSERT INTO users (id, email, name, surname, phone, password_hash)
VALUES (
    'c7300875-9c6c-4974-a5c2-019f3e8c0357',
    'admin@example.com',
    'admin',
    'admin',
    '+79990000001',
    '$argon2id$v=19$m=16384,t=2,p=1$m+onAjhO1n0hMd2emfBleQ$d0M0tOzQF94CH3lj8HF6mTF3LG+dOhIX+o2isH0g+/o'
)
ON CONFLICT (email) DO NOTHING;
