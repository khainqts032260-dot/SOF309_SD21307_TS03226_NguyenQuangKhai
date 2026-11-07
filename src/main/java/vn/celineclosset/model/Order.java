package vn.celineclosset.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DON_HANG")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maDH")
    private int maDH;

    @Column(name = "ngayDat", insertable = false, updatable = false)
    private LocalDateTime ngayDat;

    @Column(name = "ngayHoanThanh")
    private LocalDateTime ngayHoanThanh;

    @Column(name = "tongTien", nullable = false, precision = 10, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "trangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "phuongThucThanhToan", nullable = false, length = 20)
    private String phuongThucThanhToan;

    @Column(name = "maTK", nullable = false)
    private int maTK;

    @Column(name = "hoTenNhan", length = 50)
    private String hoTenNhan;

    @Column(name = "soDienThoaiNhan", length = 10)
    private String soDienThoaiNhan;

    @Column(name = "diaChiNhan", length = 255)
    private String diaChiNhan;

    @Column(name = "ghiChu", length = 255)
    private String ghiChu;

    public int getMaDH() { return maDH; }
    public void setMaDH(int maDH) { this.maDH = maDH; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
    public LocalDateTime getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(LocalDateTime ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getHoTenNhan() { return hoTenNhan; }
    public void setHoTenNhan(String hoTenNhan) { this.hoTenNhan = hoTenNhan; }
    public String getSoDienThoaiNhan() { return soDienThoaiNhan; }
    public void setSoDienThoaiNhan(String soDienThoaiNhan) { this.soDienThoaiNhan = soDienThoaiNhan; }
    public String getDiaChiNhan() { return diaChiNhan; }
    public void setDiaChiNhan(String diaChiNhan) { this.diaChiNhan = diaChiNhan; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
