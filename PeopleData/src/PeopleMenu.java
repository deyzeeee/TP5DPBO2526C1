import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PeopleMenu extends JFrame {
    public static void main(String[] args) {
        // buat object window
        PeopleMenu menu = new PeopleMenu();

        // atur ukuran window
        menu.setSize(700, 600);

        // letakkan window di tengah layar
        menu.setLocationRelativeTo(null);

        // isi window
        menu.setContentPane(menu.mainPanel);

        // beri jarak di sekeliling isi window supaya lebih rapi dan ke tengah
        ((JComponent) menu.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // ubah warna background
        menu.getContentPane().setBackground(Color.WHITE);

        // tampilkan window
        menu.setVisible(true);

        // agar program ikut berhenti saat window diclose
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang dipilih di tabel (-1 = tidak ada yang dipilih)
    private int selectedIndex = -1;

    // objek database untuk koneksi ke mysql
    private Database database;

    // batas tinggi badan yang dianggap lazim (di luar batas ini user diminta konfirmasi)
    private static final int TINGGI_MIN_LAZIM = 30;
    private static final int TINGGI_MAX_LAZIM = 300;

    private JPanel mainPanel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunLahirField;
    private JComboBox kategoriComboBox;
    private JSpinner tinggiSpinner;
    private JLabel tinggiValueLabel;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JScrollPane tableScrollPane;
    private JTable peopleTable;
    private JLabel titleLabel;

    // constructor
    public PeopleMenu() {
        // inisialisasi koneksi ke database
        database = new Database();

        // atur tampilan tabel
        peopleTable.setFillsViewportHeight(true);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        refreshTable();

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] kategoriData = { "???", "Mahasiswa", "Dosen", "Tenaga Kependidikan", "Alumni" };
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // atur spinner tinggi badan (nilai awal 170, minimal 1, tanpa batas atas, kelipatan 1)
        tinggiSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(170), Integer.valueOf(1), null, Integer.valueOf(1)));
        tinggiValueLabel.setText("cm");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });

        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // tampilkan konfirmasi sebelum menghapus data
                int konfirmasi = JOptionPane.showConfirmDialog(
                        null,
                        "Yakin ingin menghapus data ini?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (konfirmasi == JOptionPane.YES_OPTION) {
                    deleteData();
                }
            }
        });

        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // saat salah satu baris tabel ditekan
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = peopleTable.getSelectedRow();

                // jika klik di area kosong tabel, abaikan
                if (selectedIndex == -1) {
                    return;
                }

                // ambil value dari model tabel (kolom 0 adalah nomor urut)
                String curId = peopleTable.getModel().getValueAt(selectedIndex, 1).toString();
                String curNama = peopleTable.getModel().getValueAt(selectedIndex, 2).toString();
                String curTahun = peopleTable.getModel().getValueAt(selectedIndex, 3).toString();
                String curKategori = peopleTable.getModel().getValueAt(selectedIndex, 4).toString();
                String curTinggi = peopleTable.getModel().getValueAt(selectedIndex, 5).toString();

                // ubah isi textfield, combo box, dan spinner
                idField.setText(curId);
                namaField.setText(curNama);
                tahunLahirField.setText(curTahun);
                kategoriComboBox.setSelectedItem(curKategori);
                tinggiSpinner.setValue(Integer.parseInt(curTinggi));

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    // isi ulang tabel dengan data terbaru lalu rapikan lebar kolomnya
    public void refreshTable() {
        peopleTable.setModel(setTable());
        aturLebarKolom();
    }

    // atur lebar tiap kolom tabel
    public void aturLebarKolom() {
        peopleTable.getColumnModel().getColumn(0).setPreferredWidth(40);   // No
        peopleTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // ID
        peopleTable.getColumnModel().getColumn(2).setPreferredWidth(160);  // Nama
        peopleTable.getColumnModel().getColumn(3).setPreferredWidth(90);   // Tahun Lahir
        peopleTable.getColumnModel().getColumn(4).setPreferredWidth(150);  // Kategori
        peopleTable.getColumnModel().getColumn(5).setPreferredWidth(90);   // Tinggi
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] cols = { "No", "ID", "Nama", "Tahun Lahir", "Kategori", "Tinggi (cm)" };

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel tmp = new DefaultTableModel(null, cols);

        try {
            // ambil semua data dari tabel person
            ResultSet resultSet = database.selectQuery("SELECT * FROM person");

            // isi tabel dengan hasil query, kolom No diisi nomor urut
            int i = 1;
            while (resultSet.next()) {
                Object[] row = {
                        i,
                        resultSet.getString("id"),
                        resultSet.getString("nama"),
                        resultSet.getString("thn_lahir"),
                        resultSet.getString("kategori"),
                        resultSet.getString("tinggi")
                };
                tmp.addRow(row);
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return tmp;
    }

    public void insertData() {
        // cek form kosong / kategori belum dipilih
        if (!isInputValid()) {
            return;
        }

        try {
            // ambil value dari textfield, combobox, dan spinner
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            int tahunLahir = Integer.parseInt(tahunLahirField.getText().trim());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            int tinggi = (int) tinggiSpinner.getValue();

            // cek rentang tahun lahir
            if (tahunLahir < 1900 || tahunLahir > Year.now().getValue()) {
                showError("Tahun lahir harus antara 1900 sampai " + Year.now().getValue() + "!");
                return;
            }

            // cek ID duplikat di database
            if (isIdDipakai(id, null)) {
                showError("ID sudah dipakai, gunakan ID lain!");
                return;
            }

            // beri peringatan jika tinggi badan tidak lazim
            if (!konfirmasiTinggi(tinggi)) {
                return;
            }

            // tambahkan data ke database
            String sqlQuery = "INSERT INTO person (id, nama, thn_lahir, kategori, tinggi) VALUES ('"
                    + escape(id) + "', '" + escape(nama) + "', '" + tahunLahir + "', '"
                    + kategori + "', '" + tinggi + "')";
            database.insertUpdateDeleteQuery(sqlQuery);

            // update tabel
            refreshTable();

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Insert berhasil");
            JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
        } catch (NumberFormatException ex) {
            showError("Tahun lahir harus berupa angka!");
        }
    }

    public void updateData() {
        // cek form kosong / kategori belum dipilih
        if (!isInputValid()) {
            return;
        }

        try {
            // ambil data dari form
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            int tahunLahir = Integer.parseInt(tahunLahirField.getText().trim());
            String kategori = kategoriComboBox.getSelectedItem().toString();
            int tinggi = (int) tinggiSpinner.getValue();

            // id lama diambil dari tabel, karena id di form bisa saja sudah diubah
            String idLama = peopleTable.getModel().getValueAt(selectedIndex, 1).toString();

            // cek rentang tahun lahir
            if (tahunLahir < 1900 || tahunLahir > Year.now().getValue()) {
                showError("Tahun lahir harus antara 1900 sampai " + Year.now().getValue() + "!");
                return;
            }

            // cek ID duplikat (abaikan data yang sedang diubah)
            if (isIdDipakai(id, idLama)) {
                showError("ID sudah dipakai, gunakan ID lain!");
                return;
            }

            // beri peringatan jika tinggi badan tidak lazim
            if (!konfirmasiTinggi(tinggi)) {
                return;
            }

            // ubah data person di database
            String sqlQuery = "UPDATE person SET id = '" + escape(id) + "', nama = '" + escape(nama)
                    + "', thn_lahir = '" + tahunLahir + "', kategori = '" + kategori
                    + "', tinggi = '" + tinggi + "' WHERE id = '" + escape(idLama) + "'";
            database.insertUpdateDeleteQuery(sqlQuery);

            // update tabel
            refreshTable();

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update berhasil");
            JOptionPane.showMessageDialog(null, "Data berhasil diubah");
        } catch (NumberFormatException ex) {
            showError("Tahun lahir harus berupa angka!");
        }
    }

    public void deleteData() {
        // ambil id dari baris tabel yang dipilih
        String id = peopleTable.getModel().getValueAt(selectedIndex, 1).toString();

        // hapus data dari database
        String sqlQuery = "DELETE FROM person WHERE id = '" + escape(id) + "'";
        database.insertUpdateDeleteQuery(sqlQuery);

        // update tabel
        refreshTable();

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Delete berhasil");
        JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
    }

    public void clearForm() {
        // kosongkan semua textfield, combo box, dan spinner
        idField.setText("");
        namaField.setText("");
        tahunLahirField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        tinggiSpinner.setValue(170);

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // hilangkan highlight baris di tabel
        peopleTable.clearSelection();

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }

    // cek apakah form sudah terisi dengan benar
    private boolean isInputValid() {
        if (idField.getText().trim().isEmpty()
                || namaField.getText().trim().isEmpty()
                || tahunLahirField.getText().trim().isEmpty()) {
            showError("ID, Nama, dan Tahun Lahir tidak boleh kosong!");
            return false;
        }

        if (kategoriComboBox.getSelectedIndex() == 0) {
            showError("Pilih kategori terlebih dahulu!");
            return false;
        }

        return true;
    }

    // cek apakah ID sudah dipakai data lain di database (idAbaikan diisi null kalau tidak ada yang diabaikan)
    private boolean isIdDipakai(String id, String idAbaikan) {
        try {
            String sqlQuery = "SELECT id FROM person WHERE id = '" + escape(id) + "'";

            // saat update, id milik data yang sedang diubah tidak ikut dihitung
            if (idAbaikan != null) {
                sqlQuery += " AND id <> '" + escape(idAbaikan) + "'";
            }

            // kalau ada baris hasil, berarti id sudah dipakai
            ResultSet resultSet = database.selectQuery(sqlQuery);
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // minta konfirmasi jika tinggi badan tidak lazim, return true jika user tetap ingin menyimpan
    private boolean konfirmasiTinggi(int tinggi) {
        if (tinggi >= TINGGI_MIN_LAZIM && tinggi <= TINGGI_MAX_LAZIM) {
            return true;
        }

        int lanjut = JOptionPane.showConfirmDialog(
                null,
                "Tinggi " + tinggi + " cm tidak lazim. Tetap simpan?",
                "Konfirmasi Tinggi Badan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        return lanjut == JOptionPane.YES_OPTION;
    }

    // amankan teks sebelum masuk query supaya tanda petik (misal O'Neil) tidak bikin error
    private String escape(String teks) {
        return teks.replace("\\", "\\\\").replace("'", "''");
    }

    // tampilkan pesan error
    private void showError(String pesan) {
        JOptionPane.showMessageDialog(null, pesan, "Error", JOptionPane.ERROR_MESSAGE);
    }
}