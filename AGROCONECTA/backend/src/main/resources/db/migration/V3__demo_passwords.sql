-- Establece una contraseña conocida para las cuentas de demostración.
-- Contraseña de todas las cuentas demo: Agroconecta123
UPDATE users
SET password_hash = '$2a$12$5SGn0v4r.nR.wWH98DHx4uOcWwlwTkSBYmyRAov1byeSUGJr6VD1m'
WHERE email IN (
    'admin@agroconecta.co',
    'maria@agroconecta.co',
    'carlos@agroconecta.co',
    'jose@agroconecta.co'
);
