-- ============================================================================
-- CELINE CLOSET - DATABASE DUY NHAT / CHAY MOT LAN TU DAU
-- SQL Server 2022+ | Java 17 + JSP/Servlet + JPA/Hibernate
--
-- CACH CHAY TRONG SQL SERVER MANAGEMENT STUDIO:
--   1. Mo file nay bang New Query.
--   2. Co the de database dang chon la master; file tu chuyen database.
--   3. Nhan Execute va doi den khi hien thong bao tao thanh cong.
--
-- LUU Y: File nay XOA database CelineClossetDB cu va tao lai tu dau.
-- Hay sao luu du lieu that truoc khi chay.
-- KHONG can chay bat ky file migration nao khac.
-- ============================================================================

USE master;
-- Kết nối SQL Server bằng tài khoản sa không mật khẩu theo cấu hình local của project.
GO

IF DB_ID(N'CelineClossetDB') IS NOT NULL
BEGIN
    ALTER DATABASE CelineClossetDB
        SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE CelineClossetDB;
END
GO

CREATE DATABASE CelineClossetDB;
GO

ALTER DATABASE CelineClossetDB SET MULTI_USER;
GO

USE CelineClossetDB;
GO

/* ======================== MASTER TABLES ======================== */
CREATE TABLE dbo.TAI_KHOAN (
    maTK INT IDENTITY(1,1) PRIMARY KEY,
    hoTen NVARCHAR(80) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    googleId VARCHAR(100) NULL,
    soDienThoai VARCHAR(15) NULL,
    diaChiMacDinh NVARCHAR(255) NULL,
    hinhDaiDien VARCHAR(500) NULL,
    vaiTro VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    trangThai TINYINT NOT NULL DEFAULT 1,
    diemTichLuy INT NOT NULL DEFAULT 0,
    hangThanhVien VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CK_TAI_KHOAN_VAITRO CHECK (vaiTro IN ('CUSTOMER','STAFF','DELIVERY','ADMIN')),
    CONSTRAINT CK_TAI_KHOAN_EMAIL CHECK (email NOT LIKE '% %' AND email LIKE '%_@_%._%'),
    CONSTRAINT CK_TAI_KHOAN_MATKHAU_HASH CHECK (LEN(matKhau) = 64),
    CONSTRAINT CK_TAI_KHOAN_DIEM CHECK (diemTichLuy >= 0),
    CONSTRAINT CK_TAI_KHOAN_HANG CHECK (hangThanhVien IN ('BRONZE','SILVER','GOLD','PLATINUM','DIAMOND'))
);
GO
CREATE UNIQUE INDEX UX_TAI_KHOAN_GOOGLE_ID ON dbo.TAI_KHOAN(googleId) WHERE googleId IS NOT NULL;
GO

CREATE TABLE dbo.DANH_MUC (
    maDM INT IDENTITY(1,1) PRIMARY KEY,
    tenDM NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(300) NULL,
    trangThai TINYINT NOT NULL DEFAULT 1
);
GO

CREATE TABLE dbo.LOAI_TIN_TUC (
    maLoaiTin INT IDENTITY(1,1) PRIMARY KEY,
    tenLoai NVARCHAR(120) NOT NULL,
    moTa NVARCHAR(400) NULL,
    trangThai TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT CK_LOAI_TIN_TUC_TRANG_THAI CHECK (trangThai IN (0,1,2))
);
GO

CREATE TABLE dbo.TIN_TUC (
    maTin INT IDENTITY(1,1) PRIMARY KEY,
    tieuDe NVARCHAR(220) NOT NULL,
    tomTat NVARCHAR(700) NULL,
    noiDung NVARCHAR(MAX) NULL,
    hinhAnh VARCHAR(500) NULL,
    maLoaiTin INT NULL,
    trangThai TINYINT NOT NULL DEFAULT 0,
    maNguoiTao INT NOT NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_TIN_TUC_NGUOI_TAO FOREIGN KEY (maNguoiTao) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_TIN_TUC_LOAI_TIN FOREIGN KEY (maLoaiTin) REFERENCES dbo.LOAI_TIN_TUC(maLoaiTin),
    CONSTRAINT CK_TIN_TUC_TRANG_THAI CHECK (trangThai IN (0,1))
);
GO

CREATE TABLE dbo.SAN_PHAM (
    maSP INT IDENTITY(1,1) PRIMARY KEY,
    maSKU VARCHAR(30) NULL,
    tenSP NVARCHAR(120) NOT NULL,
    moTa NVARCHAR(1500) NULL,
    donGia DECIMAL(12,2) NOT NULL,
    soLuongTon INT NOT NULL DEFAULT 0,
    trangThai TINYINT NOT NULL DEFAULT 1,
    maDM INT NOT NULL,
    hinhAnh VARCHAR(500) NULL,
    mauSac NVARCHAR(250) NULL,
    kichThuoc VARCHAR(80) NULL,
    chatLieu NVARCHAR(150) NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_SAN_PHAM_DANH_MUC FOREIGN KEY (maDM) REFERENCES dbo.DANH_MUC(maDM),
    CONSTRAINT CK_SAN_PHAM_GIA CHECK (donGia >= 0),
    CONSTRAINT CK_SAN_PHAM_TON CHECK (soLuongTon >= 0)
);
GO
CREATE UNIQUE INDEX UX_SAN_PHAM_SKU ON dbo.SAN_PHAM(maSKU) WHERE maSKU IS NOT NULL;
GO

CREATE TABLE dbo.HINH_ANH_SAN_PHAM (
    maAnh INT IDENTITY(1,1) PRIMARY KEY,
    maSP INT NOT NULL,
    duongDan VARCHAR(500) NOT NULL,
    mauSac NVARCHAR(120) NULL,
    gocAnh NVARCHAR(80) NULL,
    thuTu INT NOT NULL DEFAULT 1,
    CONSTRAINT FK_HINH_ANH_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP) ON DELETE CASCADE
);
GO
CREATE INDEX IX_HINH_ANH_SAN_PHAM_THU_TU ON dbo.HINH_ANH_SAN_PHAM(maSP,thuTu);
GO

CREATE TABLE dbo.SAN_PHAM_YEU_THICH (
    maTK INT NOT NULL,
    maSP INT NOT NULL,
    ngayThem DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT PK_SAN_PHAM_YEU_THICH PRIMARY KEY (maTK, maSP),
    CONSTRAINT FK_YEU_THICH_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_YEU_THICH_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP)
);
GO
CREATE INDEX IX_SAN_PHAM_YEU_THICH_NGAY ON dbo.SAN_PHAM_YEU_THICH(maTK, ngayThem DESC);
GO

CREATE TABLE dbo.NHAP_KHO (
    maNhapKho BIGINT IDENTITY(1,1) PRIMARY KEY,
    maSP INT NOT NULL,
    soLuongNhap INT NOT NULL,
    tonTruoc INT NOT NULL,
    tonSau INT NOT NULL,
    maNhanVien INT NOT NULL,
    ghiChu NVARCHAR(250) NULL,
    soBienLai VARCHAR(60) NULL,
    nhaCungCap NVARCHAR(180) NULL,
    xuatXu NVARCHAR(180) NULL,
    ngayNhap DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_NHAP_KHO_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP),
    CONSTRAINT FK_NHAP_KHO_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_NHAP_KHO_SOLUONG CHECK (soLuongNhap > 0),
    CONSTRAINT CK_NHAP_KHO_TON CHECK (tonTruoc >= 0 AND tonSau >= tonTruoc)
);
GO

CREATE INDEX IX_NHAP_KHO_NGAY_NHAP ON dbo.NHAP_KHO(ngayNhap DESC);
CREATE INDEX IX_NHAP_KHO_SAN_PHAM ON dbo.NHAP_KHO(maSP, ngayNhap DESC);
GO

CREATE TABLE dbo.GIO_HANG (
    maGH INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    trangThai TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT FK_GIO_HANG_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK)
);
GO

CREATE TABLE dbo.CHI_TIET_GIO_HANG (
    maCTGH INT IDENTITY(1,1) PRIMARY KEY,
    maGH INT NOT NULL,
    maSP INT NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(12,2) NOT NULL,
    giamGia DECIMAL(12,2) NOT NULL DEFAULT 0,
    thanhTien DECIMAL(12,2) NOT NULL,
    mauSac NVARCHAR(120) NULL,
    kichThuoc VARCHAR(40) NULL,
    CONSTRAINT FK_CTGH_GIO_HANG FOREIGN KEY (maGH) REFERENCES dbo.GIO_HANG(maGH),
    CONSTRAINT FK_CTGH_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP),
    CONSTRAINT UQ_CTGH UNIQUE(maGH,maSP,mauSac,kichThuoc),
    CONSTRAINT CK_CTGH_SOLUONG CHECK (soLuong > 0)
);
GO

/* ======================== LOYALTY & VOUCHER ======================== */
CREATE TABLE dbo.VOUCHER (
    maVoucher INT IDENTITY(1,1) PRIMARY KEY,
    maCode VARCHAR(40) NOT NULL UNIQUE,
    tenVoucher NVARCHAR(150) NOT NULL,
    loaiGiam VARCHAR(20) NOT NULL,
    giaTri DECIMAL(12,2) NOT NULL,
    giamToiDa DECIMAL(12,2) NULL,
    donToiThieu DECIMAL(12,2) NOT NULL DEFAULT 0,
    diemDoi INT NOT NULL DEFAULT 0,
    ngayBatDau DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngayKetThuc DATETIME2 NULL,
    soLuot INT NULL,
    daDung INT NOT NULL DEFAULT 0,
    trangThai TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT CK_VOUCHER_LOAI CHECK (loaiGiam IN ('PERCENT','FIXED')),
    CONSTRAINT CK_VOUCHER_GIATRI CHECK (giaTri > 0),
    CONSTRAINT CK_VOUCHER_DIEM CHECK (diemDoi >= 0)
);
GO

CREATE TABLE dbo.KHACH_HANG_VOUCHER (
    maKHV INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    maVoucher INT NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    ngayNhan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngaySuDung DATETIME2 NULL,
    maDH INT NULL,
    CONSTRAINT FK_KHV_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_KHV_VOUCHER FOREIGN KEY (maVoucher) REFERENCES dbo.VOUCHER(maVoucher),
    CONSTRAINT CK_KHV_TRANGTHAI CHECK (trangThai IN ('AVAILABLE','USED','EXPIRED'))
);
GO

CREATE TABLE dbo.PHAN_THUONG (
    maPhanThuong INT IDENTITY(1,1) PRIMARY KEY,
    tenPhanThuong NVARCHAR(150) NOT NULL,
    loai VARCHAR(20) NOT NULL,
    diemCan INT NOT NULL,
    maVoucher INT NULL,
    moTa NVARCHAR(500) NULL,
    hinhAnh VARCHAR(500) NULL,
    trangThai TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT FK_PHAN_THUONG_VOUCHER FOREIGN KEY (maVoucher) REFERENCES dbo.VOUCHER(maVoucher),
    CONSTRAINT CK_PHAN_THUONG_LOAI CHECK (loai IN ('VOUCHER','GIFT')),
    CONSTRAINT CK_PHAN_THUONG_DIEM CHECK (diemCan > 0)
);
GO

CREATE TABLE dbo.DOI_THUONG (
    maDoiThuong INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    maPhanThuong INT NOT NULL,
    diemDaDung INT NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ghiChu NVARCHAR(500) NULL,
    ngayDoi DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_DOI_THUONG_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_DOI_THUONG_PHAN_THUONG FOREIGN KEY (maPhanThuong) REFERENCES dbo.PHAN_THUONG(maPhanThuong),
    CONSTRAINT CK_DOI_THUONG_STATUS CHECK (trangThai IN ('PENDING','APPROVED','DELIVERED','CANCELLED'))
);
GO

CREATE TABLE dbo.LICH_SU_DIEM (
    maLSD INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    maDH INT NULL,
    soDiem INT NOT NULL,
    loai VARCHAR(20) NOT NULL,
    noiDung NVARCHAR(255) NOT NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_LSD_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_LSD_LOAI CHECK (loai IN ('EARN','REDEEM','ADJUST'))
);
GO

/* ======================== ORDER & TRACKING ======================== */
CREATE TABLE dbo.DON_HANG (
    maDH INT IDENTITY(1,1) PRIMARY KEY,
    ngayDat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    tongTien DECIMAL(12,2) NOT NULL,
    tienGiam DECIMAL(12,2) NOT NULL DEFAULT 0,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xác nhận',
    phuongThucThanhToan VARCHAR(20) NOT NULL DEFAULT 'COD',
    maTK INT NOT NULL,
    maNhanVien INT NULL,
    maNhanVienXuLy INT NULL,
    ngayXacNhan DATETIME2 NULL,
    ngayHoanThanh DATETIME2 NULL,
    maVoucher INT NULL,
    hoTenNhan NVARCHAR(80) NULL,
    soDienThoaiNhan VARCHAR(15) NULL,
    diaChiNhan NVARCHAR(255) NULL,
    ghiChu NVARCHAR(500) NULL,
    diemCong INT NOT NULL DEFAULT 0,
    daCongDiem BIT NOT NULL DEFAULT 0,
    viDoGiao DECIMAL(10,7) NULL,
    kinhDoGiao DECIMAL(10,7) NULL,
    viDoHienTai DECIMAL(10,7) NULL,
    kinhDoHienTai DECIMAL(10,7) NULL,
    capNhatViTri DATETIME2 NULL,
    lyDoHuy NVARCHAR(500) NULL,
    nguoiHuy VARCHAR(20) NULL,
    ngayHuy DATETIME2 NULL,
    hangDaHoanKho BIT NOT NULL DEFAULT 0,
    loaiSuCo VARCHAR(20) NULL,
    lyDoSuCo NVARCHAR(500) NULL,
    ngaySuCo DATETIME2 NULL,
    CONSTRAINT FK_DON_HANG_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_DON_HANG_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_DON_HANG_NHAN_VIEN_XU_LY FOREIGN KEY (maNhanVienXuLy) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_DON_HANG_VOUCHER FOREIGN KEY (maVoucher) REFERENCES dbo.VOUCHER(maVoucher),
    CONSTRAINT CK_DON_HANG_TONG CHECK (tongTien >= 0),
    CONSTRAINT CK_DON_HANG_GIAM CHECK (tienGiam >= 0)
);
GO
ALTER TABLE dbo.KHACH_HANG_VOUCHER ADD CONSTRAINT FK_KHV_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH);
ALTER TABLE dbo.LICH_SU_DIEM ADD CONSTRAINT FK_LSD_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH);
GO

CREATE TABLE dbo.CHI_TIET_DON_HANG (
    maCTDH INT IDENTITY(1,1) PRIMARY KEY,
    maDH INT NOT NULL,
    maSP INT NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(12,2) NOT NULL,
    thanhTien DECIMAL(12,2) NOT NULL,
    mauSac NVARCHAR(120) NULL,
    kichThuoc VARCHAR(40) NULL,
    CONSTRAINT FK_CTDH_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT FK_CTDH_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP),
    CONSTRAINT CK_CTDH_SOLUONG CHECK (soLuong > 0)
);
GO

CREATE TABLE dbo.THANH_TOAN (
    maTT INT IDENTITY(1,1) PRIMARY KEY,
    maDH INT NOT NULL,
    phuongThuc VARCHAR(20) NOT NULL,
    soTien DECIMAL(12,2) NOT NULL,
    soTienDaNhan DECIMAL(12,2) NOT NULL DEFAULT 0,
    noiDungChuyenKhoan VARCHAR(100) NULL,
    ngayThanhToan DATETIME2 NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    trangThaiDoiSoat VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ghiChuDoiSoat NVARCHAR(500) NULL,
    maGiaoDichNganHang VARCHAR(120) NULL,
    maGiaoDichSePay BIGINT NULL,
    ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_THANH_TOAN_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT UQ_THANH_TOAN_DON_HANG UNIQUE (maDH),
    CONSTRAINT CK_THANH_TOAN_STATUS CHECK (trangThai IN ('PENDING','PAID','FAILED','CANCELLED')),
    CONSTRAINT CK_THANH_TOAN_DOI_SOAT CHECK (trangThaiDoiSoat IN ('NONE','UNDERPAID','OVERPAID','REVIEW')),
    CONSTRAINT CK_THANH_TOAN_DA_NHAN CHECK (soTienDaNhan >= 0)
);
GO
CREATE UNIQUE INDEX UX_THANH_TOAN_SEPAY ON dbo.THANH_TOAN(maGiaoDichSePay) WHERE maGiaoDichSePay IS NOT NULL;
GO

CREATE TABLE dbo.GIAO_DICH_SEPAY (
    maGiaoDichSePay BIGINT PRIMARY KEY,
    maDH INT NULL,
    gateway VARCHAR(80) NULL,
    transactionDate VARCHAR(40) NULL,
    accountNumber VARCHAR(50) NULL,
    subAccount VARCHAR(100) NULL,
    code VARCHAR(100) NULL,
    content NVARCHAR(500) NULL,
    transferType VARCHAR(10) NULL,
    description NVARCHAR(500) NULL,
    transferAmount DECIMAL(18,2) NOT NULL,
    accumulated DECIMAL(18,2) NOT NULL DEFAULT 0,
    referenceCode VARCHAR(120) NULL,
    trangThaiXuLy VARCHAR(40) NOT NULL,
    ghiChu NVARCHAR(500) NULL,
    rawPayload NVARCHAR(MAX) NULL,
    ngayNhan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_GIAO_DICH_SEPAY_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH)
);
GO
CREATE INDEX IX_GIAO_DICH_SEPAY_MADH ON dbo.GIAO_DICH_SEPAY(maDH,ngayNhan DESC);
CREATE INDEX IX_GIAO_DICH_SEPAY_CODE ON dbo.GIAO_DICH_SEPAY(code);
GO

/* ======================== RETURN & REFUND ======================== */
CREATE TABLE dbo.YEU_CAU_TRA_HANG (
    maYCTH INT IDENTITY(1,1) PRIMARY KEY,
    maDH INT NOT NULL UNIQUE,
    maTK INT NOT NULL,
    maNhanVien INT NULL,
    maAdminXuLy INT NULL,
    lyDo NVARCHAR(1000) NOT NULL,
    nganHang NVARCHAR(120) NOT NULL,
    soTaiKhoan VARCHAR(50) NOT NULL,
    chuTaiKhoan NVARCHAR(120) NOT NULL,
    soTienHoan DECIMAL(12,2) NOT NULL,
    trangThai VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    ghiChuXuLy NVARCHAR(1000) NULL,
    ngayYeuCau DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngaySuaNganHang DATETIME2 NULL,
    ngayShipperNhan DATETIME2 NULL,
    ngayDenBuuDien DATETIME2 NULL,
    ngayHoanHang DATETIME2 NULL,
    ngayBatDauHoanTien DATETIME2 NULL,
    ngayHoanTien DATETIME2 NULL,
    ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_YCTH_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT FK_YCTH_KHACH_HANG FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_YCTH_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_YCTH_ADMIN FOREIGN KEY (maAdminXuLy) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_YCTH_TRANG_THAI CHECK (trangThai IN
        ('REQUESTED','SHIPPER_RECEIVED','AT_POST_OFFICE','RETURN_COMPLETED','REFUND_PROCESSING','REFUNDED','REJECTED')),
    CONSTRAINT CK_YCTH_SO_TIEN CHECK (soTienHoan >= 0)
);
GO
CREATE INDEX IX_YCTH_TRANG_THAI ON dbo.YEU_CAU_TRA_HANG(trangThai,ngayCapNhat DESC);
CREATE INDEX IX_YCTH_NHAN_VIEN ON dbo.YEU_CAU_TRA_HANG(maNhanVien,trangThai,ngayCapNhat DESC);
GO

CREATE TABLE dbo.HINH_ANH_TRA_HANG (
    maAnhTra INT IDENTITY(1,1) PRIMARY KEY,
    maYCTH INT NOT NULL,
    duongDan VARCHAR(500) NOT NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_HATH_YEU_CAU FOREIGN KEY (maYCTH)
        REFERENCES dbo.YEU_CAU_TRA_HANG(maYCTH) ON DELETE CASCADE
);
GO
CREATE INDEX IX_HATH_YEU_CAU ON dbo.HINH_ANH_TRA_HANG(maYCTH,maAnhTra);
GO

/* Hồ sơ shipper chứng minh không giao được hàng. */
CREATE TABLE dbo.YEU_CAU_GIAO_THAT_BAI (
    maYCGTB INT IDENTITY(1,1) PRIMARY KEY,
    maDH INT NOT NULL UNIQUE,
    maNhanVien INT NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    lyDo NVARCHAR(500) NULL,
    ghiChuAdmin NVARCHAR(700) NULL,
    maAdminDuyet INT NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngayGuiDuyet DATETIME2 NULL,
    ngayDuyet DATETIME2 NULL,
    CONSTRAINT FK_YCGTB_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT FK_YCGTB_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_YCGTB_ADMIN FOREIGN KEY (maAdminDuyet) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_YCGTB_TRANG_THAI CHECK (trangThai IN ('DRAFT','PENDING','APPROVED','REJECTED'))
);
GO
CREATE INDEX IX_YCGTB_TRANG_THAI ON dbo.YEU_CAU_GIAO_THAT_BAI(trangThai,ngayGuiDuyet DESC);
GO

CREATE TABLE dbo.LAN_GIAO_THAT_BAI (
    maLan INT IDENTITY(1,1) PRIMARY KEY,
    maYCGTB INT NOT NULL,
    ngayGiao DATE NOT NULL,
    soLanGoi INT NOT NULL DEFAULT 1,
    hinhAnh VARCHAR(500) NOT NULL,
    ghiChu NVARCHAR(700) NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_LGTB_YEU_CAU FOREIGN KEY (maYCGTB) REFERENCES dbo.YEU_CAU_GIAO_THAT_BAI(maYCGTB),
    CONSTRAINT CK_LGTB_SO_LAN_GOI CHECK (soLanGoi >= 1),
    CONSTRAINT UQ_LGTB_NGAY UNIQUE (maYCGTB,ngayGiao)
);
GO
CREATE INDEX IX_LGTB_YEU_CAU ON dbo.LAN_GIAO_THAT_BAI(maYCGTB,ngayGiao);
GO

CREATE TABLE dbo.HANH_TRINH_DON_HANG (
    maHT INT IDENTITY(1,1) PRIMARY KEY,
    maDH INT NOT NULL,
    maNhanVien INT NULL,
    viDo DECIMAL(10,7) NOT NULL,
    kinhDo DECIMAL(10,7) NOT NULL,
    ghiChu NVARCHAR(255) NULL,
    ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_HANH_TRINH_DON FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT FK_HANH_TRINH_NV FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK)
);
GO

/* Nhật ký giúp xác định nhân viên đã làm gì và chịu trách nhiệm ở bước nào. */
CREATE TABLE dbo.NHAT_KY_NHAN_VIEN (
    maNK INT IDENTITY(1,1) PRIMARY KEY,
    maNhanVien INT NOT NULL,
    maDH INT NULL,
    hanhDong NVARCHAR(150) NOT NULL,
    noiDung NVARCHAR(700) NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_NKNV_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_NKNV_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH)
);
GO

/* Phiếu hỗ trợ dành cho khách hàng, có thể phân công cho nhân viên xử lý. */
CREATE TABLE dbo.YEU_CAU_HO_TRO (
    maYC INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NULL,
    hoTen NVARCHAR(80) NOT NULL,
    email VARCHAR(200) NOT NULL,
    soDienThoai VARCHAR(15) NULL,
    chuDe NVARCHAR(150) NOT NULL,
    noiDung NVARCHAR(1500) NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'MOI',
    maNhanVien INT NULL,
    phanHoi NVARCHAR(1500) NULL,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ngayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_YCHT_KHACH_HANG FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_YCHT_NHAN_VIEN FOREIGN KEY (maNhanVien) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_YCHT_TRANG_THAI CHECK (trangThai IN ('MOI','DANG_XU_LY','DA_PHAN_HOI','DA_DONG'))
);
GO


CREATE INDEX IX_YCHT_NGAY_CAP_NHAT
ON dbo.YEU_CAU_HO_TRO(ngayCapNhat);
GO


CREATE TABLE dbo.THONG_BAO_TAI_KHOAN (
    maTB INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    tieuDe NVARCHAR(180) NOT NULL,
    noiDung NVARCHAR(1000) NOT NULL,
    duongDan VARCHAR(500) NULL,
    loai VARCHAR(40) NOT NULL DEFAULT 'SYSTEM',
    daDoc BIT NOT NULL DEFAULT 0,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_TBTK_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK)
);
GO
CREATE INDEX IX_TBTK_TAI_KHOAN ON dbo.THONG_BAO_TAI_KHOAN(maTK,daDoc,ngayTao DESC);
GO

CREATE TABLE dbo.TIN_NHAN_HO_TRO (
    maTN INT IDENTITY(1,1) PRIMARY KEY,
    maYC INT NOT NULL,
    maNguoiGui INT NULL,
    vaiTroNguoiGui VARCHAR(20) NOT NULL,
    noiDung NVARCHAR(2000) NOT NULL,
    ngayGui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    daDoc BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_TNHT_YEU_CAU FOREIGN KEY (maYC) REFERENCES dbo.YEU_CAU_HO_TRO(maYC),
    CONSTRAINT FK_TNHT_TAI_KHOAN FOREIGN KEY (maNguoiGui) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT CK_TNHT_VAI_TRO CHECK (vaiTroNguoiGui IN ('CUSTOMER','STAFF','ADMIN','BOT'))
);
GO

/* ======================== FEEDBACK & PASSWORD RESET ======================== */
CREATE TABLE dbo.PHAN_HOI (
    maPH INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NULL,
    maSP INT NULL,
    maDH INT NULL,
    hoTen NVARCHAR(80) NOT NULL,
    email VARCHAR(200) NULL,
    noiDung NVARCHAR(1000) NOT NULL,
    soSao TINYINT NOT NULL DEFAULT 5,
    hinhAnh VARCHAR(500) NULL,
    daMuaHang BIT NOT NULL DEFAULT 0,
    trangThai TINYINT NOT NULL DEFAULT 1,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_PHAN_HOI_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK),
    CONSTRAINT FK_PHAN_HOI_SAN_PHAM FOREIGN KEY (maSP) REFERENCES dbo.SAN_PHAM(maSP),
    CONSTRAINT FK_PHAN_HOI_DON_HANG FOREIGN KEY (maDH) REFERENCES dbo.DON_HANG(maDH),
    CONSTRAINT CK_PHAN_HOI_SOSAO CHECK (soSao BETWEEN 1 AND 5)
);
GO

CREATE UNIQUE INDEX UX_PHAN_HOI_DON_SAN_PHAM_KHACH
ON dbo.PHAN_HOI(maDH,maSP,maTK)
WHERE maDH IS NOT NULL AND maSP IS NOT NULL AND maTK IS NOT NULL AND trangThai=1;
GO

CREATE TABLE dbo.DAT_LAI_MAT_KHAU (
    maToken INT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NOT NULL,
    tokenHash CHAR(64) NOT NULL UNIQUE,
    hetHan DATETIME2 NOT NULL,
    daDung BIT NOT NULL DEFAULT 0,
    ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_RESET_TAI_KHOAN FOREIGN KEY (maTK) REFERENCES dbo.TAI_KHOAN(maTK)
);
GO

/* ======================== DASHBOARD PROCEDURES ======================== */
CREATE OR ALTER PROCEDURE dbo.sp_DashboardTongQuan
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        (SELECT COALESCE(SUM(tongTien),0) FROM dbo.DON_HANG WHERE trangThai <> N'Đã hủy') AS doanhThu,
        (SELECT COUNT(*) FROM dbo.DON_HANG) AS donHang,
        (SELECT COUNT(*) FROM dbo.SAN_PHAM WHERE trangThai = 1) AS sanPham,
        (SELECT COUNT(*) FROM dbo.TAI_KHOAN WHERE vaiTro = 'CUSTOMER') AS khachHang,
        (SELECT COUNT(*) FROM dbo.GIO_HANG WHERE trangThai = 1) AS gioHang,
        (SELECT COUNT(*) FROM dbo.DON_HANG WHERE trangThai IN (N'Chờ xác nhận', N'Đang chuẩn bị')) AS donChoXuLy;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DashboardDoanhThu7Ngay
AS
BEGIN
    SET NOCOUNT ON;
    SELECT CONVERT(VARCHAR(10), d, 103) AS ngay, doanhThu
    FROM (
        SELECT TOP 7 CAST(ngayDat AS DATE) AS d,
               COALESCE(SUM(CASE WHEN trangThai <> N'Đã hủy' THEN tongTien ELSE 0 END),0) AS doanhThu
        FROM dbo.DON_HANG
        GROUP BY CAST(ngayDat AS DATE)
        ORDER BY CAST(ngayDat AS DATE) DESC
    ) x ORDER BY d ASC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DashboardTrangThaiDonHang
AS
BEGIN
    SET NOCOUNT ON;
    SELECT trangThai, COUNT(*) AS soDon,
           CAST(100.0 * COUNT(*) / NULLIF(SUM(COUNT(*)) OVER(),0) AS DECIMAL(6,2)) AS tiLe
    FROM dbo.DON_HANG GROUP BY trangThai ORDER BY soDon DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DashboardTrangThaiThanhToan
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan,
           COUNT(DISTINCT dh.maDH) AS soDon,
           CAST(100.0 * COUNT(DISTINCT dh.maDH) / NULLIF((SELECT COUNT(*) FROM dbo.DON_HANG),0) AS DECIMAL(6,2)) AS tiLe
    FROM dbo.DON_HANG dh LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
    GROUP BY COALESCE(tt.trangThai,'PENDING') ORDER BY soDon DESC;
END
GO

/* ======================== REVENUE PROCEDURES ======================== */
CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoTongQuan @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(DISTINCT dh.maDH) AS totalOrders,
           COALESCE(SUM(CASE WHEN dh.trangThai = N'Đã hủy' THEN 1 ELSE 0 END),0) AS cancelledOrders,
           COALESCE(SUM(CASE WHEN dh.trangThai = N'Báo lỗi' THEN 1 ELSE 0 END),0) AS errorOrders,
           COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PAID' THEN 1 ELSE 0 END),0) AS paidOrders,
           COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PAID' AND dh.trangThai <> N'Đã hủy' THEN dh.tongTien ELSE 0 END),0) AS paidRevenue,
           COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PENDING' AND dh.trangThai <> N'Đã hủy' THEN dh.tongTien ELSE 0 END),0) AS pendingRevenue,
           COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'FAILED' THEN dh.tongTien ELSE 0 END),0) AS failedRevenue,
           COALESCE(AVG(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PAID' AND dh.trangThai <> N'Đã hủy' THEN dh.tongTien END),0) AS avgPaidOrder
    FROM dbo.DON_HANG dh LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
    WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay));
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoDoanhThuTheoNgay @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT CONVERT(VARCHAR(10), ngay, 103) AS ngay, soDon, doanhThu, choThanhToan
    FROM (
        SELECT TOP 31 CAST(dh.ngayDat AS DATE) AS ngay,
               COUNT(DISTINCT dh.maDH) AS soDon,
               COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PAID' AND dh.trangThai <> N'Đã hủy' THEN dh.tongTien ELSE 0 END),0) AS doanhThu,
               COALESCE(SUM(CASE WHEN COALESCE(tt.trangThai,'PENDING') = 'PENDING' AND dh.trangThai <> N'Đã hủy' THEN dh.tongTien ELSE 0 END),0) AS choThanhToan
        FROM dbo.DON_HANG dh LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
        WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
          AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
        GROUP BY CAST(dh.ngayDat AS DATE) ORDER BY CAST(dh.ngayDat AS DATE) DESC
    ) x ORDER BY x.ngay ASC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoDoanhThuTheoDanhMuc @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TOP 10 dm.maDM, dm.tenDM, SUM(ct.soLuong) AS soLuongBan, COALESCE(SUM(ct.thanhTien),0) AS doanhThu
    FROM dbo.CHI_TIET_DON_HANG ct
    JOIN dbo.DON_HANG dh ON dh.maDH = ct.maDH
    JOIN dbo.SAN_PHAM sp ON sp.maSP = ct.maSP
    JOIN dbo.DANH_MUC dm ON dm.maDM = sp.maDM
    LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
    WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
      AND COALESCE(tt.trangThai,'PENDING') = 'PAID' AND dh.trangThai <> N'Đã hủy'
    GROUP BY dm.maDM, dm.tenDM ORDER BY doanhThu DESC, soLuongBan DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_ThongKeSanPhamBanChay @TuNgay DATE = NULL, @DenNgay DATE = NULL, @Top INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    IF @Top IS NULL OR @Top < 1 SET @Top = 10;
    IF @Top > 100 SET @Top = 100;
    SELECT TOP (@Top) sp.maSP, sp.tenSP, dm.maDM, dm.tenDM,
           SUM(ct.soLuong) AS soLuongBan, CAST(SUM(ct.thanhTien) AS DECIMAL(18,2)) AS doanhThu
    FROM dbo.CHI_TIET_DON_HANG ct
    JOIN dbo.DON_HANG dh ON dh.maDH = ct.maDH
    JOIN dbo.SAN_PHAM sp ON sp.maSP = ct.maSP
    JOIN dbo.DANH_MUC dm ON dm.maDM = sp.maDM
    WHERE dh.trangThai <> N'Đã hủy'
      AND (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
      AND EXISTS (SELECT 1 FROM dbo.THANH_TOAN tt WHERE tt.maDH = dh.maDH AND tt.trangThai = 'PAID')
    GROUP BY sp.maSP, sp.tenSP, dm.maDM, dm.tenDM
    ORDER BY soLuongBan DESC, doanhThu DESC, sp.maSP ASC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoTrangThaiThanhToan @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan,
           COUNT(DISTINCT dh.maDH) AS soDon, COALESCE(SUM(dh.tongTien),0) AS tongTien
    FROM dbo.DON_HANG dh LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
    WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
    GROUP BY COALESCE(tt.trangThai,'PENDING') ORDER BY soDon DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoTrangThaiDonHang @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT dh.trangThai, COUNT(DISTINCT dh.maDH) AS soDon, COALESCE(SUM(dh.tongTien),0) AS tongTien
    FROM dbo.DON_HANG dh
    WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
    GROUP BY dh.trangThai ORDER BY soDon DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_BaoCaoDonHangGanDay @TuNgay DATE = NULL, @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TOP 20 dh.maDH, dh.ngayDat, dh.hoTenNhan, dh.soDienThoaiNhan, dh.tongTien,
           dh.trangThai, dh.phuongThucThanhToan, tk.email,
           nv.maTK AS maNhanVien, nv.hoTen AS tenNhanVien,
           COALESCE(tt.trangThai,'PENDING') AS trangThaiThanhToan, tt.ngayThanhToan
    FROM dbo.DON_HANG dh
    JOIN dbo.TAI_KHOAN tk ON tk.maTK = dh.maTK
    LEFT JOIN dbo.TAI_KHOAN nv ON nv.maTK = dh.maNhanVien
    LEFT JOIN dbo.THANH_TOAN tt ON tt.maDH = dh.maDH
    WHERE (@TuNgay IS NULL OR dh.ngayDat >= @TuNgay)
      AND (@DenNgay IS NULL OR dh.ngayDat < DATEADD(DAY,1,@DenNgay))
    ORDER BY dh.ngayDat DESC, dh.maDH DESC;
END
GO

/* ======================== SAMPLE DATA ======================== */
-- Mật khẩu mẫu: Admin@123, Staff@123, Delivery@123, Customer@123
-- STAFF: staff@celineclosset.vn, staff0@celineclosset.vn
-- DELIVERY: staff2@celineclosset.vn, staff3@celineclosset.vn
INSERT INTO dbo.TAI_KHOAN (hoTen,email,matKhau,soDienThoai,vaiTro,trangThai,diemTichLuy,hangThanhVien,diaChiMacDinh) VALUES
(N'Quản trị Celine','admin@celineclosset.vn','e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7','0900000001','ADMIN',1,0,'BRONZE',NULL),
(N'Nhân viên 02 - Quỳnh','staff0@celineclosset.vn','dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9','0900000002','STAFF',1,0,'BRONZE',NULL),
(N'Nhân viên 03 - Minh','staff@celineclosset.vn','dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9','0900000003','STAFF',1,0,'BRONZE',NULL),
(N'Nhân viên giao hàng 04 – Lan','staff2@celineclosset.vn','6d1618208f00b6ab984a068413013f6f5994b417c104e915876d52e3a4b85900','0900000004','DELIVERY',1,0,'BRONZE',NULL),
(N'Nhân viên giao hàng 05 – Huy','staff3@celineclosset.vn','6d1618208f00b6ab984a068413013f6f5994b417c104e915876d52e3a4b85900','0900000005','DELIVERY',1,0,'BRONZE',NULL),
(N'Lê Minh Anh','customer@demo.vn','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000006','CUSTOMER',1,1280,'GOLD',N'25 Nguyễn Văn Lượng, Phường Gò Vấp, TP.HCM'),
(N'Phạm Thùy Dương','thuyduong@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000007','CUSTOMER',1,430,'SILVER',N'72 Nguyễn Văn Linh, Hải Châu, Đà Nẵng'),
(N'Võ Gia Hân','giahan@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000008','CUSTOMER',1,120,'BRONZE',N'18 Hòa Bình, Ninh Kiều, Cần Thơ'),
(N'Nguyễn Hải Yến','haiyen@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000009','CUSTOMER',1,280,'SILVER',N'85 Võ Văn Ngân, Phường Thủ Đức, TP.HCM'),
(N'Trần Quốc Bảo','quocbao@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000010','CUSTOMER',1,90,'BRONZE',N'35 Tràng Tiền, Hoàn Kiếm, Hà Nội'),
(N'Đặng Khánh Linh','khanhlinh@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000011','CUSTOMER',1,540,'SILVER',N'12 Hùng Vương, Phú Nhuận, Huế'),
(N'Bùi Hoàng Nam','hoangnam@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000012','CUSTOMER',1,60,'BRONZE',N'40 Trần Phú, Nha Trang, Khánh Hòa'),
(N'Lâm Tú Uyên','tuyen@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000013','CUSTOMER',1,760,'GOLD',N'15 Trần Quốc Toản, Đà Lạt, Lâm Đồng'),
(N'Phan Đức Anh','ducanh@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000014','CUSTOMER',1,210,'SILVER',N'48 Lạch Tray, Ngô Quyền, Hải Phòng'),
(N'Đỗ Ngọc Mai','ngocmai@example.com','98ec654a8df28f8f0f8f02220483d46916b85017de0b74d8ec755c28cb8539a8','0900000015','CUSTOMER',1,320,'SILVER',N'66 Võ Thị Sáu, Biên Hòa, Đồng Nai');
GO

INSERT INTO dbo.DANH_MUC (tenDM,moTa,trangThai) VALUES
(N'Chân váy',N'Chân váy midi và chân váy công sở thanh lịch.',1),
(N'Áo vest & blazer',N'Blazer và áo gile vest cho phong cách công sở hiện đại.',1),
(N'Áo kiểu & sơ mi',N'Áo sơ mi, blouse và áo mặc trong dễ phối.',1),
(N'Set phối',N'Các set phối sẵn thanh lịch, tiện dụng.',1),
(N'Phụ kiện',N'Khăn, thắt lưng và phụ kiện hoàn thiện trang phục.',1),
(N'Giày',N'Giày cao gót và slingback nữ thanh lịch.',1),
(N'Quần jeans',N'Quần jeans phom hiện đại, dễ phối.',1),
(N'Đầm công sở',N'Đầm midi và đầm sơ mi dành cho văn phòng.',1),
(N'Quần tây',N'Quần tây cạp cao, phom suông hoặc ống rộng.',1),
(N'Túi xách',N'Túi tote và túi xách công sở.',1);
GO

INSERT INTO dbo.LOAI_TIN_TUC(tenLoai,moTa,trangThai) VALUES
(N'Bộ sưu tập',N'Thông tin về bộ sưu tập và lookbook mới.',1),
(N'Khuyến mãi',N'Chương trình ưu đãi, voucher và sự kiện bán hàng.',1),
(N'Cửa hàng',N'Thông báo vận hành, chính sách và cập nhật từ Celine Closet.',1),
(N'Phong cách',N'Gợi ý phối đồ và xu hướng công sở.',1);
GO

INSERT INTO dbo.TIN_TUC(tieuDe,tomTat,noiDung,hinhAnh,maLoaiTin,trangThai,maNguoiTao) VALUES
(N'Lookbook tháng 1 – Khởi đầu thanh lịch',N'Bộ phối blazer, sơ mi và quần tây mở đầu năm mới.',N'Khám phá những thiết kế công sở thanh lịch trong Lookbook tháng 1 của Celine Closet.','assets/images/lookbook/lookbook-01.png',1,1,1),
(N'Ưu đãi giữa mùa dành cho thành viên',N'Nhận voucher và ưu đãi trên các sản phẩm được chọn.',N'Đăng nhập tài khoản thành viên để kiểm tra voucher và tích điểm cho mỗi đơn hàng.','assets/images/fashion/hero-03.jpg',2,1,1),
(N'Celine Closet cập nhật danh mục sản phẩm mới',N'20 sản phẩm công sở và phụ kiện đã được cập nhật đầy đủ hình ảnh.',N'Toàn bộ ảnh sản phẩm, màu sắc, size và mô tả đã được chuẩn hóa theo bộ ảnh mới của cửa hàng.','assets/images/fashion/hero-04.jpg',3,1,1),
(N'5 cách phối blazer thanh lịch cho tuần làm việc',N'Từ quần tây, chân váy đến jeans: 5 công thức phối blazer dễ áp dụng mỗi ngày.',N'Blazer là món đồ cốt lõi của tủ đồ công sở. Hãy thay đổi lớp áo trong, quần hoặc chân váy theo từng ngày để có nhiều cách phối mà vẫn giữ phong cách chỉn chu.','assets/images/fashion/hero-02.jpg',4,1,1),
(N'Hướng dẫn chọn size trang phục công sở',N'Ba số đo cần biết trước khi chọn áo, quần hoặc đầm.',N'Hãy đo vòng ngực, vòng eo và vòng mông khi cơ thể ở trạng thái tự nhiên. Với blazer chú ý phần vai; với quần tây ưu tiên vòng eo và mông; với đầm cần kiểm tra đồng thời ngực và eo.','assets/images/fashion/story.jpg',4,1,1),
(N'Mid-season sale – ưu đãi đến 50%',N'Ưu đãi giữa mùa cho các thiết kế được chọn và voucher thành viên.',N'Chương trình áp dụng cho một số sản phẩm được chọn. Thành viên có thể dùng voucher phù hợp ở bước thanh toán và vẫn được tích điểm khi đơn hoàn thành.','assets/images/fashion/hero-03.jpg',2,1,1),
(N'Chính sách đổi sản phẩm trong 7 ngày',N'Tóm tắt điều kiện gửi yêu cầu đổi hoặc trả hàng.',N'Khách có thể gửi yêu cầu trong thời hạn hiển thị trên lịch sử đơn hàng. Quy trình trả hàng và hoàn tiền được cập nhật theo từng bước để khách dễ theo dõi.','assets/images/fashion/card-01.jpg',3,1,1);
GO

SET IDENTITY_INSERT dbo.SAN_PHAM ON;
INSERT INTO dbo.SAN_PHAM (maSP,maSKU,tenSP,moTa,donGia,soLuongTon,trangThai,maDM,hinhAnh,mauSac,kichThuoc,chatLieu) VALUES
(1,'CV-LIN-001',N'Chân Váy Bút Chì Midi Xẻ Trước – Linea',N'Phom bút chì dài qua gối, cạp cao và đường xẻ giữa phía trước tạo vẻ gọn gàng, thanh lịch cho trang phục công sở.',699000,38,1,1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-tra-sua_mat-truoc.webp',N'Nâu trà sữa, Đỏ rượu, Đen, Nâu cacao, Trắng kem, Xám','S,M,L,XL',N'Tuytsi co giãn'),
(2,'AG-ELA-002',N'Áo Gile Vest Hai Hàng Khuy – Elara',N'Thiết kế gile vest ôm nhẹ phần eo, cổ chữ V và hai hàng khuy, phù hợp phối cùng quần tây hoặc chân váy.',749000,32,1,2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_trang-kem_mat-truoc.webp',N'Trắng kem, Nâu cacao, Đen','S,M,L',N'Tuytsi pha viscose'),
(3,'AHD-LIV-003',N'Áo Hai Dây Camisole Ôm Dáng – Livia',N'Áo hai dây tối giản, phom ôm nhẹ và đường cổ mềm, thích hợp mặc riêng hoặc làm lớp trong blazer, cardigan.',389000,46,1,3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_trang_mat-truoc.webp',N'Trắng, Nâu taupe, Xám, Đen','S,M,L',N'Satin mờ co giãn nhẹ'),
(4,'SET-OXF-004',N'Set Sơ Mi Kẻ Phối Khăn Len Choàng Vai – Oxford',N'Set sơ mi kẻ sọc dáng thoải mái kết hợp khăn len choàng vai, tạo phong cách preppy trẻ trung nhưng vẫn lịch sự.',899000,28,1,4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_kem-xanh-reu_mat-truoc.webp',N'Kem – xanh rêu, Trắng – navy, Kem – nâu cacao, Trắng – xanh sage','S,M,L',N'Cotton pha mềm'),
(5,'TL-AXI-005',N'Thắt Lưng Bản Nhỏ Khóa Chữ Nhật – Axis',N'Thắt lưng bản nhỏ với khóa kim loại chữ nhật, phù hợp tạo điểm nhấn gọn gàng cho quần tây, jeans hoặc váy.',329000,54,1,5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_den_mat-truoc.webp',N'Đen, Trắng, Nâu chocolate','Freesize',N'Da PU cao cấp'),
(6,'GSB-CEL-006',N'Giày Slingback Mũi Nhọn Gót Kitten – Celeste',N'Giày slingback mũi nhọn với gót kitten thanh mảnh, mang lại vẻ nữ tính và dễ di chuyển trong môi trường công sở.',829000,26,1,6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_mat-truoc.webp',N'Trắng kem, Đỏ rượu, Nâu chocolate','35,36,37,38,39,40',N'Da tổng hợp mềm'),
(7,'QJL-BEL-007',N'Quần Jeans Cạp Cao Ống Loe – Bellis',N'Thiết kế cạp cao ôm phần hông và loe dần từ gối, giúp đôi chân trông dài và tạo nét retro hiện đại.',789000,34,1,7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xanh-nhat_mat-truoc.webp',N'Xanh nhạt, Xanh indigo, Xám wash','S,M,L,XL',N'Denim co giãn'),
(8,'QJR-MON-008',N'Quần Jeans Cạp Cao Ống Rộng – Monroe',N'Quần jeans cạp cao với ống rộng thẳng, tạo cảm giác thoải mái và dễ phối cùng áo ôm, sơ mi hoặc blazer.',789000,35,1,7,'assets/images/catalog/QJR-MON-008/QJR-MON-008_nau-cacao_mat-truoc.webp',N'Nâu cacao, Trắng kem, Đen','S,M,L,XL',N'Denim cotton'),
(9,'DSM-SER-009',N'Đầm Sơ Mi Midi Chiết Eo – Serena',N'Đầm sơ mi midi cổ Đức, hàng nút trước và chiết eo nhẹ, phù hợp đi làm, gặp gỡ hoặc các dịp cần vẻ ngoài chỉn chu.',1090000,22,1,8,'assets/images/catalog/DSM-SER-009/DSM-SER-009_xanh-mint_mat-truoc.webp',N'Xanh mint','S,M,L,XL',N'Cotton pha viscose'),
(10,'BLZ-MAD-010',N'Blazer Hai Hàng Khuy Phom Suông – Madison',N'Blazer hai hàng khuy với ve chữ K và phom suông vừa phải, tạo vẻ chuyên nghiệp khi phối cùng quần tây hoặc đầm.',1290000,24,1,2,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_be_mat-truoc.webp',N'Be, Xám than','S,M,L,XL',N'Tuytsi pha cao cấp'),
(11,'SMN-ROS-011',N'Áo Sơ Mi Cổ Nơ Tay Dài – Rosalie',N'Áo sơ mi cổ nơ với tay dài phồng nhẹ và cổ tay bo gọn, phù hợp phối quần tây hoặc chân váy cho phong cách nữ tính.',629000,40,1,3,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_trang-kem_mat-truoc.webp',N'Trắng kem, Hồng phấn','S,M,L,XL',N'Lụa satin mềm'),
(12,'QTS-SIE-012',N'Quần Tây Cạp Cao Ống Rộng – Siena',N'Quần tây cạp cao có ly trước và ống rộng thẳng, giúp tổng thể trang phục trông thanh thoát, hiện đại và chuyên nghiệp.',849000,31,1,9,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_den_mat-truoc.webp',N'Đen, Xám, Trắng kem, Be','S,M,L,XL',N'Tuytsi pha rũ'),
(13,'GCG-NOI-013',N'Giày Cao Gót Mũi Nhọn Khóa Kim Loại – Noir',N'Giày cao gót mũi nhọn với gót thấp và điểm nhấn khóa kim loại, thích hợp cho trang phục công sở hoặc sự kiện.',899000,20,1,6,'assets/images/catalog/GCG-NOI-013/GCG-NOI-013_den_mat-truoc.webp',N'Đen, Trắng, Xanh navy','35,36,37,38,39,40',N'Da tổng hợp'),
(14,'SM-AVE-014',N'Áo Sơ Mi Trơn Cổ Đức – Aveline',N'Áo sơ mi trơn cổ Đức với dáng suông nhẹ và bề mặt mềm rủ, dễ kết hợp cùng quần tây, jeans hoặc chân váy.',529000,58,1,3,'assets/images/catalog/SM-AVE-014/SM-AVE-014_den_mat-truoc.webp',N'Đen, Hồng nhạt, Nâu trà sữa, Trắng, Vàng bơ, Xanh da trời','S,M,L,XL',N'Lụa satin pha'),
(15,'TTC-VER-015',N'Túi Tote Công Sở Phom Cánh Dơi – Verona',N'Túi tote phom cánh dơi rộng rãi, quai đôi và bề mặt vân da, phù hợp mang đi làm hoặc sử dụng hằng ngày.',1190000,18,1,10,'assets/images/catalog/TTC-VER-015/TTC-VER-015_den_mat-truoc.webp',N'Đen, Nâu trà sữa','Freesize',N'Da PU vân'),
(16,'TXH-AUR-016',N'Túi Xách Công Sở Phom Hộp – Aurelia',N'Túi xách tay phom hộp cứng cáp với quai đôi và chi tiết khóa kim loại, phù hợp phong cách công sở chỉn chu.',1390000,16,1,10,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_nau-taupe_mat-truoc.webp',N'Nâu taupe, Trắng, Xanh navy','Freesize',N'Da PU phom cứng'),
(17,'DSM-CAM-017',N'Đầm Sơ Mi Midi Dáng Xòe Phối Đai – Camille',N'Đầm sơ mi midi cổ Đức, tay ngắn, phom xòe nhẹ với túi nắp và đai eo mảnh, phù hợp đi làm hoặc gặp gỡ.',1190000,21,1,8,'assets/images/catalog/DSM-CAM-017/DSM-CAM-017_xam-ghi_mat-truoc.webp',N'Xám ghi','S,M,L,XL',N'Cotton pha viscose'),
(18,'DMV-NOE-018',N'Đầm Midi Cổ Xẻ Chữ V Phối Đai – Noelle',N'Đầm midi cổ xẻ chữ V, tay ngắn, chiết eo gọn với chân váy xếp ly và hai túi nắp, tạo vẻ thanh lịch hiện đại.',1290000,19,1,8,'assets/images/catalog/DMV-NOE-018/DMV-NOE-018_den_mat-truoc.webp',N'Đen','S,M,L,XL',N'Tuytsi pha mềm'),
(19,'CVM-MAR-019',N'Chân Váy Midi Đắp Chéo Phối Nút – Margot',N'Chân váy midi cạp cao dáng thẳng, thiết kế đắp chéo với hàng nút lệch và túi nắp, phù hợp phong cách công sở chỉn chu.',749000,29,1,1,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_xam-than_mat-truoc.webp',N'Xám than, Đen, Trắng kem','S,M,L,XL',N'Tuytsi co giãn nhẹ'),
(20,'KLV-ETO-020',N'Khăn Lụa Vuông Họa Tiết Viền Màu – Étoile',N'Khăn lụa vuông họa tiết với viền màu tương phản, có thể thắt cổ, buộc tóc hoặc tạo điểm nhấn cho túi xách và blazer.',449000,44,1,5,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_kem-nga-nau-chocolate_trai-phang.webp',N'Kem ngà – nâu chocolate, Navy – cam – hồng phấn, Xanh mint – trắng, Trắng kem – xanh navy','Freesize',N'Lụa satin');
SET IDENTITY_INSERT dbo.SAN_PHAM OFF;
GO

INSERT INTO dbo.HINH_ANH_SAN_PHAM(maSP,duongDan,mauSac,gocAnh,thuTu) VALUES
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-tra-sua_mat-truoc.webp',N'Nâu trà sữa',N'Mặt trước',1),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-tra-sua_mat-sau.webp',N'Nâu trà sữa',N'Mặt sau',2),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-tra-sua_goc-nghieng.webp',N'Nâu trà sữa',N'Góc nghiêng',3),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-tra-sua_anh-nguoi-mau.webp',N'Nâu trà sữa',N'Ảnh người mẫu',4),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_do-ruou_mat-truoc.webp',N'Đỏ rượu',N'Mặt trước',5),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_den_mat-truoc.webp',N'Đen',N'Mặt trước',6),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_nau-cacao_mat-truoc.webp',N'Nâu cacao',N'Mặt trước',7),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',8),
(1,'assets/images/catalog/CV-LIN-001/CV-LIN-001_xam_mat-truoc.webp',N'Xám',N'Mặt trước',9),
(2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',10),
(2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',11),
(2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',12),
(2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_nau-cacao_mat-truoc.webp',N'Nâu cacao',N'Mặt trước',13),
(2,'assets/images/catalog/AG-ELA-002/AG-ELA-002_den_mat-truoc.webp',N'Đen',N'Mặt trước',14),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',15),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_trang_mat-sau.webp',N'Trắng',N'Mặt sau',16),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_trang_anh-nguoi-mau.webp',N'Trắng',N'Ảnh người mẫu',17),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_nau-taupe_mat-truoc.webp',N'Nâu taupe',N'Mặt trước',18),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_xam_mat-truoc.webp',N'Xám',N'Mặt trước',19),
(3,'assets/images/catalog/AHD-LIV-003/AHD-LIV-003_den_mat-truoc.webp',N'Đen',N'Mặt trước',20),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_kem-xanh-reu_mat-truoc.webp',N'Kem – xanh rêu',N'Mặt trước',21),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_kem-xanh-reu_mat-sau.webp',N'Kem – xanh rêu',N'Mặt sau',22),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_kem-xanh-reu_anh-nguoi-mau.webp',N'Kem – xanh rêu',N'Ảnh người mẫu',23),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_trang-navy_mat-truoc.webp',N'Trắng – navy',N'Mặt trước',24),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_kem-nau-cacao_mat-truoc.webp',N'Kem – nâu cacao',N'Mặt trước',25),
(4,'assets/images/catalog/SET-OXF-004/SET-OXF-004_trang-xanh-sage_mat-truoc.webp',N'Trắng – xanh sage',N'Mặt trước',26),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_den_mat-truoc.webp',N'Đen',N'Mặt trước',27),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_den_mat-sau.webp',N'Đen',N'Mặt sau',28),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_den_goc-cuon.webp',N'Đen',N'Góc cuộn',29),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',30),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_trang_mat-sau.webp',N'Trắng',N'Mặt sau',31),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_trang_goc-cuon.webp',N'Trắng',N'Góc cuộn',32),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_nau-chocolate_mat-truoc.webp',N'Nâu chocolate',N'Mặt trước',33),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_nau-chocolate_mat-sau.webp',N'Nâu chocolate',N'Mặt sau',34),
(5,'assets/images/catalog/TL-AXI-005/TL-AXI-005_nau-chocolate_goc-cuon.webp',N'Nâu chocolate',N'Góc cuộn',35),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',36),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_goc-cheo.webp',N'Trắng kem',N'Góc chéo',37),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_goc-sau.webp',N'Trắng kem',N'Góc sau',38),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_goc-ngang.webp',N'Trắng kem',N'Góc ngang',39),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',40),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_do-ruou_goc-cheo.webp',N'Đỏ rượu',N'Góc chéo',41),
(6,'assets/images/catalog/GSB-CEL-006/GSB-CEL-006_nau-chocolate_goc-cheo.webp',N'Nâu chocolate',N'Góc chéo',42),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xanh-nhat_mat-truoc.webp',N'Xanh nhạt',N'Mặt trước',43),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xanh-nhat_mat-sau.webp',N'Xanh nhạt',N'Mặt sau',44),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xanh-indigo_mat-truoc.webp',N'Xanh indigo',N'Mặt trước',45),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xanh-indigo_mat-sau.webp',N'Xanh indigo',N'Mặt sau',46),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xam-wash_mat-truoc.webp',N'Xám wash',N'Mặt trước',47),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xam-wash_mat-sau.webp',N'Xám wash',N'Mặt sau',48),
(7,'assets/images/catalog/QJL-BEL-007/QJL-BEL-007_xam-wash_anh-nguoi-mau.webp',N'Xám wash',N'Ảnh người mẫu',49),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_nau-cacao_mat-truoc.webp',N'Nâu cacao',N'Mặt trước',50),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_nau-cacao_mat-sau.webp',N'Nâu cacao',N'Mặt sau',51),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',52),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',53),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',54),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_den_mat-truoc.webp',N'Đen',N'Mặt trước',55),
(8,'assets/images/catalog/QJR-MON-008/QJR-MON-008_den_mat-sau.webp',N'Đen',N'Mặt sau',56),
(9,'assets/images/catalog/DSM-SER-009/DSM-SER-009_xanh-mint_mat-truoc.webp',N'Xanh mint',N'Mặt trước',57),
(9,'assets/images/catalog/DSM-SER-009/DSM-SER-009_xanh-mint_mat-sau.webp',N'Xanh mint',N'Mặt sau',58),
(9,'assets/images/catalog/DSM-SER-009/DSM-SER-009_xanh-mint_goc-nghieng.webp',N'Xanh mint',N'Góc nghiêng',59),
(9,'assets/images/catalog/DSM-SER-009/DSM-SER-009_xanh-mint_anh-nguoi-mau.webp',N'Xanh mint',N'Ảnh người mẫu',60),
(10,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_be_mat-truoc.webp',N'Be',N'Mặt trước',61),
(10,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_be_mat-sau.webp',N'Be',N'Mặt sau',62),
(10,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_be_goc-nghieng.webp',N'Be',N'Góc nghiêng',63),
(10,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_be_anh-nguoi-mau.webp',N'Be',N'Ảnh người mẫu',64),
(10,'assets/images/catalog/BLZ-MAD-010/BLZ-MAD-010_xam-than_mat-truoc.webp',N'Xám than',N'Mặt trước',65),
(11,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',66),
(11,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',67),
(11,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_trang-kem_goc-nghieng.webp',N'Trắng kem',N'Góc nghiêng',68),
(11,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',69),
(11,'assets/images/catalog/SMN-ROS-011/SMN-ROS-011_hong-phan_mat-truoc.webp',N'Hồng phấn',N'Mặt trước',70),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_den_mat-truoc.webp',N'Đen',N'Mặt trước',71),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_den_mat-sau.webp',N'Đen',N'Mặt sau',72),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_den_goc-nghieng.webp',N'Đen',N'Góc nghiêng',73),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',74),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_xam_mat-truoc.webp',N'Xám',N'Mặt trước',75),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_xam_mat-sau.webp',N'Xám',N'Mặt sau',76),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',77),
(12,'assets/images/catalog/QTS-SIE-012/QTS-SIE-012_be_mat-truoc.webp',N'Be',N'Mặt trước',78),
(13,'assets/images/catalog/GCG-NOI-013/GCG-NOI-013_den_mat-truoc.webp',N'Đen',N'Mặt trước',79),
(13,'assets/images/catalog/GCG-NOI-013/GCG-NOI-013_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',80),
(13,'assets/images/catalog/GCG-NOI-013/GCG-NOI-013_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',81),
(13,'assets/images/catalog/GCG-NOI-013/GCG-NOI-013_xanh-navy_mat-truoc.webp',N'Xanh navy',N'Mặt trước',82),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_den_mat-truoc.webp',N'Đen',N'Mặt trước',83),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_hong-nhat_mat-truoc.webp',N'Hồng nhạt',N'Mặt trước',84),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_nau-tra-sua_mat-truoc.webp',N'Nâu trà sữa',N'Mặt trước',85),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',86),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_trang_mat-sau.webp',N'Trắng',N'Mặt sau',87),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_vang-bo_mat-truoc.webp',N'Vàng bơ',N'Mặt trước',88),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_xanh-da-troi_mat-truoc.webp',N'Xanh da trời',N'Mặt trước',89),
(14,'assets/images/catalog/SM-AVE-014/SM-AVE-014_xanh-da-troi_anh-nguoi-mau.webp',N'Xanh da trời',N'Ảnh người mẫu',90),
(15,'assets/images/catalog/TTC-VER-015/TTC-VER-015_den_mat-truoc.webp',N'Đen',N'Mặt trước',91),
(15,'assets/images/catalog/TTC-VER-015/TTC-VER-015_den_mat-sau.webp',N'Đen',N'Mặt sau',92),
(15,'assets/images/catalog/TTC-VER-015/TTC-VER-015_den_goc-ngang.webp',N'Đen',N'Góc ngang',93),
(15,'assets/images/catalog/TTC-VER-015/TTC-VER-015_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',94),
(15,'assets/images/catalog/TTC-VER-015/TTC-VER-015_nau-tra-sua_mat-truoc.webp',N'Nâu trà sữa',N'Mặt trước',95),
(16,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_nau-taupe_mat-truoc.webp',N'Nâu taupe',N'Mặt trước',96),
(16,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_nau-taupe_mat-sau.webp',N'Nâu taupe',N'Mặt sau',97),
(16,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_nau-taupe_goc-ngang.webp',N'Nâu taupe',N'Góc ngang',98),
(16,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',99),
(16,'assets/images/catalog/TXH-AUR-016/TXH-AUR-016_xanh-navy_mat-truoc.webp',N'Xanh navy',N'Mặt trước',100),
(17,'assets/images/catalog/DSM-CAM-017/DSM-CAM-017_xam-ghi_mat-truoc.webp',N'Xám ghi',N'Mặt trước',101),
(17,'assets/images/catalog/DSM-CAM-017/DSM-CAM-017_xam-ghi_mat-sau.webp',N'Xám ghi',N'Mặt sau',102),
(17,'assets/images/catalog/DSM-CAM-017/DSM-CAM-017_xam-ghi_goc-nghieng.webp',N'Xám ghi',N'Góc nghiêng',103),
(17,'assets/images/catalog/DSM-CAM-017/DSM-CAM-017_xam-ghi_anh-nguoi-mau.webp',N'Xám ghi',N'Ảnh người mẫu',104),
(18,'assets/images/catalog/DMV-NOE-018/DMV-NOE-018_den_mat-truoc.webp',N'Đen',N'Mặt trước',105),
(18,'assets/images/catalog/DMV-NOE-018/DMV-NOE-018_den_mat-sau.webp',N'Đen',N'Mặt sau',106),
(18,'assets/images/catalog/DMV-NOE-018/DMV-NOE-018_den_goc-nghieng.webp',N'Đen',N'Góc nghiêng',107),
(18,'assets/images/catalog/DMV-NOE-018/DMV-NOE-018_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',108),
(19,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_xam-than_mat-truoc.webp',N'Xám than',N'Mặt trước',109),
(19,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_xam-than_mat-sau.webp',N'Xám than',N'Mặt sau',110),
(19,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_xam-than_anh-nguoi-mau.webp',N'Xám than',N'Ảnh người mẫu',111),
(19,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_den_mat-truoc.webp',N'Đen',N'Mặt trước',112),
(19,'assets/images/catalog/CVM-MAR-019/CVM-MAR-019_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',113),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_kem-nga-nau-chocolate_trai-phang.webp',N'Kem ngà – nâu chocolate',N'Trải phẳng',114),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_kem-nga-nau-chocolate_tao-kieu.webp',N'Kem ngà – nâu chocolate',N'Tạo kiểu',115),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_navy-cam-hong-phan_tao-kieu.webp',N'Navy – cam – hồng phấn',N'Tạo kiểu',116),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_navy-cam-hong-phan_anh-nguoi-mau.webp',N'Navy – cam – hồng phấn',N'Ảnh người mẫu',117),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_xanh-mint-trang_tao-kieu.webp',N'Xanh mint – trắng',N'Tạo kiểu',118),
(20,'assets/images/catalog/KLV-ETO-020/KLV-ETO-020_trang-kem-xanh-navy_goc-gap.webp',N'Trắng kem – xanh navy',N'Góc gấp',119);
GO

-- Màu sắc giữ đúng theo ảnh thật; không sinh hoặc ghi đè màu ngẫu nhiên.
-- Tạo hai đánh giá demo cho mọi sản phẩm. Một số đánh giá có ảnh, một số chỉ có nội dung.
DECLARE @reviewProduct INT=1;
WHILE @reviewProduct <= (SELECT MAX(maSP) FROM dbo.SAN_PHAM)
BEGIN
 IF NOT EXISTS (SELECT 1 FROM dbo.PHAN_HOI WHERE maSP=@reviewProduct)
 BEGIN
  INSERT INTO dbo.PHAN_HOI(maSP,hoTen,email,noiDung,soSao,hinhAnh,daMuaHang,trangThai,ngayTao) VALUES
  (@reviewProduct,N'Ngọc Anh',CONCAT('ngocanh',@reviewProduct,'@demo.vn'),N'Sản phẩm đúng hình, phom gọn và màu thực tế trang nhã. Chất liệu phù hợp mặc đi làm.',5,CASE WHEN @reviewProduct%3=0 THEN (SELECT hinhAnh FROM dbo.SAN_PHAM WHERE maSP=@reviewProduct) ELSE NULL END,1,1,DATEADD(DAY,-(@reviewProduct%20),SYSDATETIME())),
  (@reviewProduct,N'Thảo My',CONCAT('thaomy',@reviewProduct,'@demo.vn'),N'Shop đóng gói cẩn thận, tư vấn size nhanh. Đường may đẹp và mặc khá thoải mái.',4,NULL,1,1,DATEADD(DAY,-((@reviewProduct%20)+2),SYSDATETIME()));
 END
 SET @reviewProduct=@reviewProduct+1;
END;
GO

INSERT INTO dbo.VOUCHER(maCode,tenVoucher,loaiGiam,giaTri,giamToiDa,donToiThieu,diemDoi,ngayBatDau,ngayKetThuc,soLuot,trangThai) VALUES
('CELINE10',N'Giảm 10% tối đa 100.000đ','PERCENT',10,100000,500000,0,'2026-01-01','2026-12-31',500,1),
('FREESHIP',N'Hỗ trợ phí vận chuyển 30.000đ','FIXED',30000,NULL,300000,0,'2026-01-01','2026-12-31',500,1),
('WELCOME50',N'Ưu đãi thành viên mới 50.000đ','FIXED',50000,NULL,399000,0,'2026-01-01','2026-12-31',500,1),
('SILVER80',N'Voucher hạng Silver 80.000đ','FIXED',80000,NULL,600000,500,'2026-01-01','2026-12-31',300,1),
('GOLD15',N'Voucher hạng Gold giảm 15%','PERCENT',15,180000,800000,900,'2026-01-01','2026-12-31',300,1);
GO

INSERT INTO dbo.PHAN_THUONG(tenPhanThuong,loai,diemCan,maVoucher,moTa,hinhAnh,trangThai) VALUES
(N'Voucher Silver 80K','VOUCHER',500,4,N'Đổi 500 điểm để nhận voucher giảm 80.000đ.','assets/images/catalog/TTC-VER-015/TTC-VER-015_den_mat-truoc.webp',1),
(N'Voucher Gold 15%','VOUCHER',900,5,N'Đổi 900 điểm để nhận voucher giảm 15%, tối đa 180.000đ.','assets/images/catalog/KLV-ETO-020/KLV-ETO-020_navy-cam-hong-phan_tao-kieu.webp',1),
(N'Túi vải C&C Limited','GIFT',1200,NULL,N'Quà tặng túi vải phiên bản giới hạn, giao cùng đơn tiếp theo hoặc nhận tại cửa hàng.','assets/images/catalog/TL-AXI-005/TL-AXI-005_den_mat-truoc.webp',1);
GO

INSERT INTO dbo.KHACH_HANG_VOUCHER(maTK,maVoucher,trangThai) VALUES (6,1,'AVAILABLE'),(6,2,'AVAILABLE'),(7,3,'AVAILABLE');
GO

INSERT INTO dbo.GIO_HANG (maTK,trangThai) VALUES (6,1);
INSERT INTO dbo.CHI_TIET_GIO_HANG(maGH,maSP,soLuong,donGia,giamGia,thanhTien) VALUES
(1,1,1,749000,0,749000),(1,2,2,1090000,0,2180000);
GO

-- Hai đơn gần nhất, một đơn đang giao có tọa độ để khách và nhân viên xem bản đồ.
INSERT INTO dbo.DON_HANG(ngayDat,tongTien,tienGiam,trangThai,phuongThucThanhToan,maTK,maNhanVien,maNhanVienXuLy,maVoucher,hoTenNhan,soDienThoaiNhan,diaChiNhan,ghiChu,diemCong,daCongDiem,viDoGiao,kinhDoGiao,viDoHienTai,kinhDoHienTai,capNhatViTri) VALUES
('2026-07-19T09:15:00',1328000,50000,N'Đang chuẩn bị','BANK',6,NULL,3,3,N'Lê Minh Anh','0900000006',N'25 Nguyễn Văn Lượng, Phường Gò Vấp, TP.HCM',N'Giao giờ hành chính',132,0,10.8386000,106.6712000,NULL,NULL,NULL),
('2026-07-21T14:30:00',1190000,0,N'Đang giao','COD',9,4,2,NULL,N'Nguyễn Hải Yến','0900000009',N'85 Võ Văn Ngân, Phường Thủ Đức, TP.HCM',N'Gọi trước khi giao',119,0,10.8507000,106.7719000,10.8302000,106.6847000,'2026-07-22T08:10:00');
GO

INSERT INTO dbo.CHI_TIET_DON_HANG(maDH,maSP,soLuong,donGia,thanhTien) VALUES
(1,1,1,749000,749000),(1,3,1,629000,629000),(2,8,1,1190000,1190000);
INSERT INTO dbo.THANH_TOAN(maDH,phuongThuc,soTien,noiDungChuyenKhoan,ngayThanhToan,trangThai) VALUES
(1,'BANK',1328000,'DH00001','2026-07-19T09:20:00','PAID'),(2,'COD',1190000,'DH00002',NULL,'PENDING');
UPDATE dbo.THANH_TOAN SET soTienDaNhan=soTien, trangThaiDoiSoat='NONE' WHERE trangThai='PAID';
INSERT INTO dbo.HANH_TRINH_DON_HANG(maDH,maNhanVien,viDo,kinhDo,ghiChu,ngayCapNhat) VALUES
(2,4,10.8187500,106.5963500,N'Bắt đầu giao hàng tại cửa hàng','2026-07-22T07:45:00'),
(2,4,10.8249000,106.6425000,N'Đang trên đường đến khách hàng','2026-07-22T07:58:00'),
(2,4,10.8302000,106.6847000,N'Vị trí shipper','2026-07-22T08:10:00');
GO

INSERT INTO dbo.NHAT_KY_NHAN_VIEN(maNhanVien,maDH,hanhDong,noiDung,ngayTao) VALUES
(3,1,N'Nhận phân công',N'Phụ trách kiểm tra và chuẩn bị đơn #1.','2026-07-19T09:25:00'),
(4,2,N'Nhận phân công',N'Phụ trách giao đơn #2 cho Nguyễn Hải Yến.','2026-07-21T14:35:00'),
(4,2,N'Cập nhật vị trí',N'Đã bắt đầu giao hàng và gửi vị trí GPS.','2026-07-22T07:45:00'),
(4,2,N'Cập nhật vị trí',N'Xe đang trên đường đến điểm giao.','2026-07-22T07:58:00');
GO

INSERT INTO dbo.YEU_CAU_HO_TRO(maTK,hoTen,email,soDienThoai,chuDe,noiDung,trangThai,maNhanVien,phanHoi,ngayTao,ngayCapNhat) VALUES
(9,N'Nguyễn Hải Yến','haiyen@example.com','0900000009',N'Kiểm tra thời gian giao đơn #2',N'Tôi muốn biết đơn hàng dự kiến giao vào buổi nào.','DANG_XU_LY',2,NULL,'2026-07-22T07:30:00','2026-07-22T07:35:00'),
(7,N'Phạm Thùy Dương','thuyduong@example.com','0900000007',N'Tư vấn đổi size',N'Tôi cần đổi sản phẩm từ size S sang size M.','DA_PHAN_HOI',3,N'Cửa hàng đã ghi nhận và hướng dẫn đổi size trong 7 ngày.','2026-07-20T15:00:00','2026-07-20T15:20:00');
GO

-- Dữ liệu mẫu từ tháng 01/2026 đến 07/2026 để biểu đồ doanh thu có dữ liệu liên tục.
DECLARE @i INT = 1;
WHILE @i <= 42
BEGIN
    DECLARE @month INT = ((@i - 1) % 7) + 1;
    DECLARE @day INT = 2 + ((@i * 3) % 20);
    DECLARE @customer INT = 6 + ((@i - 1) % 10);
    DECLARE @processingStaff INT = CASE WHEN @i % 2 = 0 THEN 2 ELSE 3 END;
    DECLARE @deliveryStaff INT = CASE WHEN @i % 2 = 0 THEN 4 ELSE 5 END;
    DECLARE @product INT = 1 + ((@i - 1) % 20);
    DECLARE @price DECIMAL(12,2) = (SELECT donGia FROM dbo.SAN_PHAM WHERE maSP=@product);
    DECLARE @qty INT = CASE WHEN @i % 5 = 0 THEN 2 ELSE 1 END;
    DECLARE @amount DECIMAL(12,2) = @price * @qty;
    DECLARE @status NVARCHAR(30) = CASE
        WHEN @i % 13 = 0 THEN N'Chờ xác nhận'
        WHEN @i % 11 = 0 THEN N'Đã hủy'
        WHEN @i % 7 = 0 THEN N'Đang giao'
        WHEN @i % 5 = 0 THEN N'Đang chuẩn bị'
        ELSE N'Hoàn thành' END;
    DECLARE @orderDate DATETIME2 = DATETIME2FROMPARTS(2026,@month,@day,9 + (@i%8),(@i*7)%60,0,0,0);
    DECLARE @deliveryLat DECIMAL(10,7) = CASE @customer
        WHEN 6 THEN 10.8386000 WHEN 7 THEN 16.0471000 WHEN 8 THEN 10.0342000 WHEN 9 THEN 10.8507000
        WHEN 10 THEN 21.0266000 WHEN 11 THEN 16.4637000 WHEN 12 THEN 12.2451000 WHEN 13 THEN 11.9404000
        WHEN 14 THEN 20.8449000 WHEN 15 THEN 10.9575000 END;
    DECLARE @deliveryLng DECIMAL(10,7) = CASE @customer
        WHEN 6 THEN 106.6712000 WHEN 7 THEN 108.2062000 WHEN 8 THEN 105.7887000 WHEN 9 THEN 106.7719000
        WHEN 10 THEN 105.8533000 WHEN 11 THEN 107.5909000 WHEN 12 THEN 109.1943000 WHEN 13 THEN 108.4583000
        WHEN 14 THEN 106.6881000 WHEN 15 THEN 106.8426000 END;

    INSERT INTO dbo.DON_HANG(ngayDat,tongTien,trangThai,phuongThucThanhToan,maTK,maNhanVien,maNhanVienXuLy,
                             hoTenNhan,soDienThoaiNhan,diaChiNhan,ghiChu,diemCong,daCongDiem,
                             viDoGiao,kinhDoGiao,viDoHienTai,kinhDoHienTai,capNhatViTri)
    SELECT @orderDate,@amount,@status,CASE WHEN @i%2=0 THEN 'BANK' ELSE 'COD' END,@customer,
           CASE WHEN @status IN (N'Đang giao',N'Hoàn thành') THEN @deliveryStaff ELSE NULL END,@processingStaff,
           hoTen,soDienThoai,diaChiMacDinh,N'Dữ liệu mẫu tháng '+CAST(@month AS NVARCHAR(2)),
           FLOOR(@amount/10000),CASE WHEN @status=N'Hoàn thành' THEN 1 ELSE 0 END,
           @deliveryLat,@deliveryLng,
           CASE WHEN @status=N'Đang giao' THEN CASE @customer
               WHEN 6 THEN 10.8265000  -- Bình Thạnh, TP.HCM
               WHEN 7 THEN 13.7829000  -- Quy Nhơn, trên trục đường ra Đà Nẵng
               WHEN 8 THEN 10.3600000  -- Mỹ Tho, trên trục đi Cần Thơ
               WHEN 9 THEN 10.8350000  -- TP Thủ Đức
               WHEN 10 THEN 16.4637000 -- Huế, trên trục Bắc Nam đi Hà Nội
               WHEN 11 THEN 13.7829000 -- Quy Nhơn, trên trục đi Huế
               WHEN 12 THEN 11.9404000 -- Đà Lạt, trục đi Nha Trang
               WHEN 13 THEN 11.5486000 -- Bảo Lộc, trục đi Đà Lạt
               WHEN 14 THEN 18.6796000 -- Vinh, trục Bắc Nam đi Hải Phòng
               WHEN 15 THEN 10.8650000 -- TP Thủ Đức, trục đi Biên Hòa
               ELSE 10.8187500 END ELSE NULL END,
           CASE WHEN @status=N'Đang giao' THEN CASE @customer
               WHEN 6 THEN 106.7035000
               WHEN 7 THEN 109.2197000
               WHEN 8 THEN 106.3600000
               WHEN 9 THEN 106.7550000
               WHEN 10 THEN 107.5909000
               WHEN 11 THEN 109.2197000
               WHEN 12 THEN 108.4583000
               WHEN 13 THEN 107.8077000
               WHEN 14 THEN 105.6813000
               WHEN 15 THEN 106.7600000
               ELSE 106.5963500 END ELSE NULL END,
           CASE WHEN @status=N'Đang giao' THEN DATEADD(MINUTE,20,@orderDate) ELSE NULL END
    FROM dbo.TAI_KHOAN WHERE maTK=@customer;

    DECLARE @orderId INT = SCOPE_IDENTITY();
    INSERT INTO dbo.CHI_TIET_DON_HANG(maDH,maSP,soLuong,donGia,thanhTien)
    VALUES(@orderId,@product,@qty,@price,@amount);
    INSERT INTO dbo.THANH_TOAN(maDH,phuongThuc,soTien,noiDungChuyenKhoan,ngayThanhToan,trangThai)
    VALUES(@orderId,CASE WHEN @i%2=0 THEN 'BANK' ELSE 'COD' END,@amount,'DH'+CASE WHEN LEN(CAST(@orderId AS VARCHAR(20)))>=5 THEN CAST(@orderId AS VARCHAR(20)) ELSE RIGHT(REPLICATE('0',5)+CAST(@orderId AS VARCHAR(20)),5) END,
           CASE WHEN @status IN (N'Hoàn thành',N'Đang giao',N'Đang chuẩn bị') THEN DATEADD(MINUTE,5,@orderDate) ELSE NULL END,
           CASE WHEN @status=N'Đã hủy' THEN 'CANCELLED'
                WHEN @status=N'Chờ xác nhận' THEN 'PENDING'
                ELSE 'PAID' END);
    SET @i += 1;
END
GO
UPDATE dbo.THANH_TOAN SET soTienDaNhan=soTien, trangThaiDoiSoat='NONE' WHERE trangThai='PAID' AND soTienDaNhan=0;
GO

INSERT INTO dbo.PHAN_HOI(maTK,maSP,hoTen,email,noiDung,soSao,hinhAnh,daMuaHang,trangThai,ngayTao) VALUES
(6,1,N'Lê Minh Anh','customer@demo.vn',N'Vải mềm, màu dịu và form lên rất gọn. Tôi đã mặc đi làm cả ngày vẫn thoải mái.',5,'assets/images/fashion/card-01.jpg',1,1,'2026-07-20T09:00:00'),
(7,3,N'Thùy Dương','thuyduong@example.com',N'Chân váy đứng phom, đường may sạch. Shop tư vấn size rất nhanh.',5,'assets/images/fashion/card-03.jpg',1,1,'2026-07-18T16:30:00'),
(8,5,N'Hoài Thương','hoaithuong@example.com',N'Màu ngoài đẹp hơn hình, đóng gói cẩn thận. Sẽ mua lại.',4,'assets/images/fashion/card-05.jpg',1,1,'2026-07-17T10:20:00'),
(NULL,NULL,N'Minh Anh','minhanh@example.com',N'Giao diện dễ xem, sản phẩm được trình bày rõ và thanh lịch.',5,NULL,0,1,'2026-07-16T14:10:00');
GO

-- Cập nhật điểm/hạng từ dữ liệu mẫu hoàn thành.
UPDATE tk
SET diemTichLuy = diemTichLuy + x.diem,
    hangThanhVien = CASE
        WHEN diemTichLuy + x.diem >= 3000 THEN 'DIAMOND'
        WHEN diemTichLuy + x.diem >= 1000 THEN 'GOLD'
        WHEN diemTichLuy + x.diem >= 300 THEN 'SILVER'
        ELSE 'BRONZE' END
FROM dbo.TAI_KHOAN tk
JOIN (
    SELECT maTK, SUM(diemCong) AS diem FROM dbo.DON_HANG
    WHERE trangThai=N'Hoàn thành' AND daCongDiem=1 GROUP BY maTK
) x ON x.maTK=tk.maTK;
GO


/* ======================== SAN PHAM MO TU BO ANH CUA CUA HANG ======================== */
SET IDENTITY_INSERT dbo.SAN_PHAM ON;
INSERT INTO dbo.SAN_PHAM (maSP,maSKU,tenSP,moTa,donGia,soLuongTon,trangThai,maDM,hinhAnh,mauSac,kichThuoc,chatLieu) VALUES
(21,'TSH-ARI-021',N'Áo Thun Ôm Cổ Tròn Tay Ngắn – Aria',N'Áo thun tay ngắn phom ôm vừa, cổ tròn tối giản và chất vải co giãn mềm, phù hợp mặc riêng hoặc phối trong blazer.',429000,60,1,3,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_do-burgundy_mat-truoc.webp',N'Đỏ burgundy, Trắng, Đen, Xám, Xanh navy','S,M,L,XL',N'Cotton gân co giãn'),
(22,'CDG-CLA-022',N'Cardigan Dệt Kim Cổ Tròn Dáng Lửng – Clara',N'Cardigan dệt kim cổ tròn, dáng lửng gọn gàng với hàng cúc nhỏ, dễ phối cùng quần tây, jeans hoặc chân váy.',649000,42,1,3,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_den_mat-truoc.webp',N'Đen, Be, Xám','S,M,L',N'Len dệt kim mịn'),
(23,'CDG-CEL-023',N'Cardigan Dệt Kim Vân Cáp Mỏng – Celia',N'Cardigan dệt kim vân cáp mảnh, phom ôm nhẹ và hàng cúc ngọc trai, tạo điểm nhấn nữ tính cho trang phục công sở.',679000,38,1,3,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_xanh-baby_mat-truoc.webp',N'Xanh baby, Hồng phấn, Vàng bơ','S,M,L',N'Len cotton dệt vân'),
(24,'GMA-COL-024',N'Giày Mary Jane Mũi Phối Màu – Colette',N'Giày Mary Jane mũi tròn phối màu tương phản, quai ngang thanh mảnh và gót thấp, phù hợp đi làm cả ngày.',899000,24,1,6,'assets/images/catalog/GMA-COL-024/GMA-COL-024_trang-kem-den_mat-truoc.webp',N'Trắng kem – đen','35,36,37,38,39,40',N'Da tổng hợp mềm'),
(25,'GBB-ELI-025',N'Giày Búp Bê Mary Jane Quai Mảnh – Elise',N'Giày búp bê Mary Jane đế bệt với hai quai mảnh, bề mặt xanh baby nhẹ nhàng và phom mũi tròn dễ mang.',799000,26,1,6,'assets/images/catalog/GBB-ELI-025/GBB-ELI-025_xanh-baby_mat-truoc.webp',N'Xanh baby','35,36,37,38,39,40',N'Vải dệt phủ mềm'),
(26,'BCG-VES-026',N'Boots Cổ Ngắn Gót Vuông – Vesper',N'Boots cổ ngắn mũi vuông nhẹ, gót vuông chắc chắn và bề mặt da đen tối giản, phù hợp phong cách công sở hiện đại.',1290000,18,1,6,'assets/images/catalog/BCG-VES-026/BCG-VES-026_den_mat-truoc.webp',N'Đen','35,36,37,38,39,40',N'Da tổng hợp cao cấp'),
(27,'BLZ-AME-027',N'Blazer Chiết Eo Hai Hàng Khuy – Amélie',N'Blazer hai hàng khuy với đường chiết eo rõ nét và phần vạt xòe nhẹ, mang lại vẻ chuyên nghiệp nhưng nữ tính.',1390000,20,1,2,'assets/images/catalog/BLZ-AME-027/BLZ-AME-027_trang-kem_mat-truoc.webp',N'Trắng kem','S,M,L,XL',N'Tuytsi cao cấp'),
(28,'BLZ-LUC-028',N'Blazer Tay Ngắn Dáng Lửng – Lucie',N'Blazer tay ngắn dáng lửng, ve cổ gọn và một khuy tối giản, dễ phối cùng quần tây hoặc chân váy cạp cao.',1090000,27,1,2,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_trang-kem_mat-truoc.webp',N'Trắng kem, Nâu chocolate','S,M,L',N'Tuytsi pha linen'),
(29,'GLD-SOL-029',N'Áo Gile Dài Không Tay – Solène',N'Áo gile dáng dài không tay với ve cổ bản vừa, đường cắt thẳng và túi nắp, tạo lớp phối thanh lịch cho mùa chuyển tiếp.',1190000,22,1,2,'assets/images/catalog/GLD-SOL-029/GLD-SOL-029_den_mat-truoc.webp',N'Đen','S,M,L',N'Tuytsi pha mềm'),
(30,'QTR-ADE-030',N'Quần Tây Xếp Ly Ống Rộng – Adèle',N'Quần tây cạp cao xếp ly, ống rộng mềm rủ và phom dài thanh thoát, phù hợp phối với sơ mi hoặc áo ôm.',849000,34,1,9,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xanh-navy_mat-truoc.webp',N'Xanh navy, Xám','S,M,L,XL',N'Tuytsi sọc mảnh'),
(31,'SMT-BLA-031',N'Áo Sơ Mi Tay Ngắn Cổ Tròn – Blanche',N'Áo sơ mi tay ngắn màu trắng với cổ bo tròn mềm, hàng cúc nhỏ và phom suông vừa, phù hợp môi trường công sở.',529000,45,1,3,'assets/images/catalog/SMT-BLA-031/SMT-BLA-031_trang_mat-truoc.webp',N'Trắng','S,M,L,XL',N'Cotton poplin'),
(32,'CVT-LAU-032',N'Cà Vạt Công Sở Bản Vừa – Laurent',N'Cà vạt bản vừa với bảng màu trung tính và họa tiết kẻ tinh tế, phù hợp trang phục công sở hoặc sự kiện trang trọng.',299000,70,1,5,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_be_mat-truoc.webp',N'Be, Nâu chocolate, Đỏ rượu – navy, Nâu sọc, Be kẻ caro, Nâu kẻ caro','Freesize',N'Polyester lụa'),
(33,'VDG-NOA-033',N'Ví Da Gập Mini Nắp Chữ V – Noa',N'Ví gập mini với nắp chữ V, bề mặt da hạt và ngăn nhỏ gọn, tiện mang theo thẻ, tiền mặt và giấy tờ cá nhân.',459000,40,1,5,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_trang-kem_mat-truoc.webp',N'Trắng kem, Xanh baby, Đen','Freesize',N'Da PU hạt'),
(34,'QTR-MOC-034',N'Quần Tây Cạp Cao Ống Rộng – Mocha',N'Quần tây cạp cao màu nâu chocolate, xếp ly nhẹ và ống rộng thẳng, đi kèm thắt lưng mảnh tạo tổng thể chỉn chu.',879000,30,1,9,'assets/images/catalog/QTR-MOC-034/QTR-MOC-034_nau-chocolate_mat-truoc.webp',N'Nâu chocolate','S,M,L,XL',N'Tuytsi pha rũ');
SET IDENTITY_INSERT dbo.SAN_PHAM OFF;
GO

INSERT INTO dbo.HINH_ANH_SAN_PHAM(maSP,duongDan,mauSac,gocAnh,thuTu) VALUES
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_do-burgundy_mat-truoc.webp',N'Đỏ burgundy',N'Mặt trước',120),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_do-burgundy_mat-sau.webp',N'Đỏ burgundy',N'Mặt sau',121),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_do-burgundy_anh-nguoi-mau.webp',N'Đỏ burgundy',N'Ảnh người mẫu',122),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',123),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_den_mat-truoc.webp',N'Đen',N'Mặt trước',124),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_xam_mat-truoc.webp',N'Xám',N'Mặt trước',125),
(21,'assets/images/catalog/TSH-ARI-021/TSH-ARI-021_xanh-navy_mat-truoc.webp',N'Xanh navy',N'Mặt trước',126),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_den_mat-truoc.webp',N'Đen',N'Mặt trước',127),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_den_mat-sau.webp',N'Đen',N'Mặt sau',128),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_den_goc-nghieng.webp',N'Đen',N'Góc nghiêng',129),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',130),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_be_mat-truoc.webp',N'Be',N'Mặt trước',131),
(22,'assets/images/catalog/CDG-CLA-022/CDG-CLA-022_xam_mat-truoc.webp',N'Xám',N'Mặt trước',132),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_xanh-baby_mat-truoc.webp',N'Xanh baby',N'Mặt trước',133),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_xanh-baby_mat-sau.webp',N'Xanh baby',N'Mặt sau',134),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_xanh-baby_goc-nghieng.webp',N'Xanh baby',N'Góc nghiêng',135),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_xanh-baby_anh-nguoi-mau.webp',N'Xanh baby',N'Ảnh người mẫu',136),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_hong-phan_mat-truoc.webp',N'Hồng phấn',N'Mặt trước',137),
(23,'assets/images/catalog/CDG-CEL-023/CDG-CEL-023_vang-bo_mat-truoc.webp',N'Vàng bơ',N'Mặt trước',138),
(24,'assets/images/catalog/GMA-COL-024/GMA-COL-024_trang-kem-den_mat-truoc.webp',N'Trắng kem – đen',N'Mặt trước',139),
(24,'assets/images/catalog/GMA-COL-024/GMA-COL-024_trang-kem-den_mat-sau.webp',N'Trắng kem – đen',N'Mặt sau',140),
(24,'assets/images/catalog/GMA-COL-024/GMA-COL-024_trang-kem-den_goc-ngang.webp',N'Trắng kem – đen',N'Góc ngang',141),
(24,'assets/images/catalog/GMA-COL-024/GMA-COL-024_trang-kem-den_goc-cheo.webp',N'Trắng kem – đen',N'Góc chéo',142),
(25,'assets/images/catalog/GBB-ELI-025/GBB-ELI-025_xanh-baby_mat-truoc.webp',N'Xanh baby',N'Mặt trước',143),
(25,'assets/images/catalog/GBB-ELI-025/GBB-ELI-025_xanh-baby_mat-sau.webp',N'Xanh baby',N'Mặt sau',144),
(25,'assets/images/catalog/GBB-ELI-025/GBB-ELI-025_xanh-baby_goc-ngang.webp',N'Xanh baby',N'Góc ngang',145),
(25,'assets/images/catalog/GBB-ELI-025/GBB-ELI-025_xanh-baby_goc-cheo.webp',N'Xanh baby',N'Góc chéo',146),
(26,'assets/images/catalog/BCG-VES-026/BCG-VES-026_den_mat-truoc.webp',N'Đen',N'Mặt trước',147),
(26,'assets/images/catalog/BCG-VES-026/BCG-VES-026_den_goc-cheo.webp',N'Đen',N'Góc chéo',148),
(26,'assets/images/catalog/BCG-VES-026/BCG-VES-026_den_goc-ngang.webp',N'Đen',N'Góc ngang',149),
(26,'assets/images/catalog/BCG-VES-026/BCG-VES-026_den_mat-sau.webp',N'Đen',N'Mặt sau',150),
(27,'assets/images/catalog/BLZ-AME-027/BLZ-AME-027_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',151),
(27,'assets/images/catalog/BLZ-AME-027/BLZ-AME-027_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',152),
(27,'assets/images/catalog/BLZ-AME-027/BLZ-AME-027_trang-kem_goc-nghieng.webp',N'Trắng kem',N'Góc nghiêng',153),
(27,'assets/images/catalog/BLZ-AME-027/BLZ-AME-027_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',154),
(28,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',155),
(28,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',156),
(28,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_trang-kem_goc-nghieng.webp',N'Trắng kem',N'Góc nghiêng',157),
(28,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_trang-kem_anh-nguoi-mau.webp',N'Trắng kem',N'Ảnh người mẫu',158),
(28,'assets/images/catalog/BLZ-LUC-028/BLZ-LUC-028_nau-chocolate_mat-truoc.webp',N'Nâu chocolate',N'Mặt trước',159),
(29,'assets/images/catalog/GLD-SOL-029/GLD-SOL-029_den_mat-truoc.webp',N'Đen',N'Mặt trước',160),
(29,'assets/images/catalog/GLD-SOL-029/GLD-SOL-029_den_mat-sau.webp',N'Đen',N'Mặt sau',161),
(29,'assets/images/catalog/GLD-SOL-029/GLD-SOL-029_den_goc-nghieng.webp',N'Đen',N'Góc nghiêng',162),
(29,'assets/images/catalog/GLD-SOL-029/GLD-SOL-029_den_anh-nguoi-mau.webp',N'Đen',N'Ảnh người mẫu',163),
(30,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xanh-navy_mat-truoc.webp',N'Xanh navy',N'Mặt trước',164),
(30,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xanh-navy_mat-sau.webp',N'Xanh navy',N'Mặt sau',165),
(30,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xanh-navy_goc-nghieng.webp',N'Xanh navy',N'Góc nghiêng',166),
(30,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xanh-navy_anh-nguoi-mau.webp',N'Xanh navy',N'Ảnh người mẫu',167),
(30,'assets/images/catalog/QTR-ADE-030/QTR-ADE-030_xam_mat-truoc.webp',N'Xám',N'Mặt trước',168),
(31,'assets/images/catalog/SMT-BLA-031/SMT-BLA-031_trang_mat-truoc.webp',N'Trắng',N'Mặt trước',169),
(31,'assets/images/catalog/SMT-BLA-031/SMT-BLA-031_trang_mat-sau.webp',N'Trắng',N'Mặt sau',170),
(31,'assets/images/catalog/SMT-BLA-031/SMT-BLA-031_trang_chi-tiet-co.webp',N'Trắng',N'Chi tiết cổ',171),
(31,'assets/images/catalog/SMT-BLA-031/SMT-BLA-031_trang_anh-nguoi-mau.webp',N'Trắng',N'Ảnh người mẫu',172),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_be_mat-truoc.webp',N'Be',N'Mặt trước',173),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_nau-chocolate_mat-truoc.webp',N'Nâu chocolate',N'Mặt trước',174),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_do-ruou-navy_mat-truoc.webp',N'Đỏ rượu – navy',N'Mặt trước',175),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_nau-soc_goc-cuon.webp',N'Nâu sọc',N'Góc cuộn',176),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_be-ke-caro_mat-truoc.webp',N'Be kẻ caro',N'Mặt trước',177),
(32,'assets/images/catalog/CVT-LAU-032/CVT-LAU-032_nau-ke-caro_mat-sau.webp',N'Nâu kẻ caro',N'Mặt sau',178),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_trang-kem_mat-truoc.webp',N'Trắng kem',N'Mặt trước',179),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_trang-kem_mat-sau.webp',N'Trắng kem',N'Mặt sau',180),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_xanh-baby_mat-truoc.webp',N'Xanh baby',N'Mặt trước',181),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_xanh-baby_goc-nghieng.webp',N'Xanh baby',N'Góc nghiêng',182),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_den_mat-truoc.webp',N'Đen',N'Mặt trước',183),
(33,'assets/images/catalog/VDG-NOA-033/VDG-NOA-033_den_goc-nghieng.webp',N'Đen',N'Góc nghiêng',184),
(34,'assets/images/catalog/QTR-MOC-034/QTR-MOC-034_nau-chocolate_mat-truoc.webp',N'Nâu chocolate',N'Mặt trước',185),
(34,'assets/images/catalog/QTR-MOC-034/QTR-MOC-034_nau-chocolate_mat-sau.webp',N'Nâu chocolate',N'Mặt sau',186),
(34,'assets/images/catalog/QTR-MOC-034/QTR-MOC-034_nau-chocolate_goc-nghieng.webp',N'Nâu chocolate',N'Góc nghiêng',187),
(34,'assets/images/catalog/QTR-MOC-034/QTR-MOC-034_nau-chocolate_anh-nguoi-mau.webp',N'Nâu chocolate',N'Ảnh người mẫu',188);
GO

/* ======================== SAN PHAM TEST THANH TOAN 10.000D ======================== */
SET IDENTITY_INSERT dbo.SAN_PHAM ON;
INSERT INTO dbo.SAN_PHAM (maSP,maSKU,tenSP,moTa,donGia,soLuongTon,trangThai,maDM,hinhAnh,mauSac,kichThuoc,chatLieu) VALUES
(35,'TEST-BANK-010',N'[TEST] Sản phẩm thanh toán ngân hàng 10.000đ',N'Sản phẩm chỉ dùng để kiểm thử QR TPBank và webhook SePay. Không dùng làm hàng bán thực tế.',10000,999,1,5,'assets/images/fashion/card-01.jpg',N'Test',N'Freesize',N'Sản phẩm kiểm thử');
SET IDENTITY_INSERT dbo.SAN_PHAM OFF;
GO
INSERT INTO dbo.HINH_ANH_SAN_PHAM(maSP,duongDan,mauSac,gocAnh,thuTu) VALUES
(35,'assets/images/fashion/card-01.jpg',N'Test',N'Ảnh kiểm thử',189);
GO

/* Ghi lại mốc hoàn thành để tính chính xác thời hạn trả hàng 7 ngày. */
UPDATE dbo.DON_HANG
SET ngayHoanThanh=COALESCE(ngayHoanThanh,DATEADD(HOUR,1,ngayDat))
WHERE trangThai=N'Hoàn thành';
GO

/* ======================== KIEM TRA SAU KHI TAO ======================== */
PRINT N'DA TAO DATABASE CelineClossetDB THANH CONG - KHONG CAN CHAY MIGRATION.';
SELECT
    DB_NAME() AS databaseHienTai,
    (SELECT COUNT(*) FROM dbo.TAI_KHOAN) AS soTaiKhoan,
    (SELECT COUNT(*) FROM dbo.SAN_PHAM) AS soSanPham,
    (SELECT COUNT(*) FROM dbo.DON_HANG) AS soDonHang,
    (SELECT COUNT(*) FROM dbo.VOUCHER) AS soVoucher,
    (SELECT COUNT(*) FROM dbo.PHAN_HOI) AS soPhanHoi,
    (SELECT COUNT(*) FROM dbo.LOAI_TIN_TUC) AS soLoaiTin,
    (SELECT COUNT(*) FROM dbo.HINH_ANH_SAN_PHAM) AS soAnhSanPham;
GO




/* Đơn COD đã hoàn thành luôn được xem là đã thu tiền thành công. */
UPDATE tt
SET tt.trangThai='PAID',tt.soTienDaNhan=tt.soTien,tt.trangThaiDoiSoat='NONE',
    tt.ghiChuDoiSoat=N'Tự động xác nhận tiền mặt khi giao thành công',
    tt.ngayThanhToan=COALESCE(tt.ngayThanhToan,dh.ngayHoanThanh,SYSDATETIME()),
    tt.ngayCapNhat=SYSDATETIME()
FROM dbo.THANH_TOAN tt
JOIN dbo.DON_HANG dh ON dh.maDH=tt.maDH
WHERE dh.trangThai=N'Hoàn thành' AND dh.phuongThucThanhToan='COD';
GO

/* Chuẩn hóa mã chuyển khoản thành DH + tối thiểu 5 chữ số: DH00001, DH00045... */
UPDATE dbo.THANH_TOAN
SET noiDungChuyenKhoan='DH'+CASE
    WHEN LEN(CAST(maDH AS VARCHAR(20)))>=5 THEN CAST(maDH AS VARCHAR(20))
    ELSE RIGHT(REPLICATE('0',5)+CAST(maDH AS VARCHAR(20)),5)
END
WHERE phuongThuc='BANK';
GO
