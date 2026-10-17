-- buat database kalau belum ada
CREATE DATABASE IF NOT EXISTS db_people;
USE db_people;

-- tabel person, kolomnya disesuaikan dengan TP4 (id, nama, tahun lahir, kategori, tinggi)
CREATE TABLE IF NOT EXISTS person (
    id VARCHAR(20) NOT NULL,
    nama VARCHAR(100) NOT NULL,
    thn_lahir INT NOT NULL,
    kategori VARCHAR(50) NOT NULL,
    tinggi INT NOT NULL,
    PRIMARY KEY (id)
);

-- data awal, sama dengan data populateList() di TP4
INSERT INTO person (id, nama, thn_lahir, kategori, tinggi) VALUES
('CVT001', 'Ida Bagus Arjuna Mahadana', 2006, 'Mahasiswa', 168),
('CVT002', 'Farell Arrefa Zhafir', 2006, 'Mahasiswa', 165),
('CVT003', 'Novi Nur Azizah', 2006, 'Mahasiswa', 154),
('CVT004', 'Hendra Wijaya', 1980, 'Dosen', 175),
('CVT005', 'Maya Lestari', 1975, 'Dosen', 163),
('CVT006', 'Agus Setiawan', 1988, 'Tenaga Kependidikan', 168),
('CVT007', 'Ghaitsa Meutia Ranggali', 2004, 'Alumni', 160),
('CVT008', 'Fawwaz Khaitsar Nuriman', 2004, 'Alumni', 170),
('CVT009', 'Kharisa Adinda Queenadia', 2007, 'Mahasiswa', 160),
('CVT010', 'Bagas Nugroho', 1990, 'Tenaga Kependidikan', 170);
