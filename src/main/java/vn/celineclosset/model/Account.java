package vn.celineclosset.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TAI_KHOAN")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maTK")
    private int maTK;

    @Column(name = "hoTen", nullable = false, length = 50)
    private String hoTen;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "matKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "soDienThoai", length = 10)
    private String soDienThoai;

    @Column(name = "diaChiMacDinh", length = 255)
    private String diaChiMacDinh;

    @Column(name = "hinhDaiDien", length = 255)
    private String hinhDaiDien;

    @Column(name = "vaiTro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "trangThai", nullable = false)
    private int trangThai;

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChiMacDinh() { return diaChiMacDinh; }
    public void setDiaChiMacDinh(String diaChiMacDinh) { this.diaChiMacDinh = diaChiMacDinh; }
    public String getHinhDaiDien() { return hinhDaiDien; }
    public void setHinhDaiDien(String hinhDaiDien) { this.hinhDaiDien = hinhDaiDien; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
}
