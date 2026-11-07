package vn.celineclosset.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "SAN_PHAM")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maSP")
    private int maSP;

    @Column(name = "maSKU", length = 30)
    private String maSKU;

    @Column(name = "tenSP", nullable = false, length = 120)
    private String tenSP;

    @Column(name = "moTa", length = 1000)
    private String moTa;

    @Column(name = "donGia", nullable = false, precision = 10, scale = 2)
    private BigDecimal donGia;

    @Column(name = "soLuongTon", nullable = false)
    private int soLuongTon;

    @Column(name = "trangThai", nullable = false)
    private int trangThai;

    @Column(name = "maDM", nullable = false)
    private int maDM;

    @Column(name = "hinhAnh", length = 500)
    private String hinhAnh;

    @Column(name = "mauSac", length = 250)
    private String mauSac;

    @Column(name = "kichThuoc", length = 80)
    private String kichThuoc;

    @Column(name = "chatLieu", length = 150)
    private String chatLieu;

    public int getMaSP() { return maSP; }
    public void setMaSP(int maSP) { this.maSP = maSP; }
    public String getMaSKU() { return maSKU; }
    public void setMaSKU(String maSKU) { this.maSKU = maSKU; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }
    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
    public int getMaDM() { return maDM; }
    public void setMaDM(int maDM) { this.maDM = maDM; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
    public String getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(String kichThuoc) { this.kichThuoc = kichThuoc; }
    public String getChatLieu() { return chatLieu; }
    public void setChatLieu(String chatLieu) { this.chatLieu = chatLieu; }
}
