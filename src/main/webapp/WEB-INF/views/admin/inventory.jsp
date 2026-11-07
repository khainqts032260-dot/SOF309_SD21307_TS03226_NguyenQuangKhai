<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../common/admin-header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="admin-page-head inventory-page-head">
    <div><span class="eyebrow">WAREHOUSE</span><h1>Quản lý kho hàng</h1><p>Xem tồn kho và nhập thêm hàng. Lịch sử được tách sang trang riêng để không phải cuộn quá dài.</p></div>
    <a class="inventory-history-jump" href="${ctx}/admin/inventory-history"><i class="fa-solid fa-clock-rotate-left"></i> Xem lịch sử nhập hàng</a>
</section>

<c:if test="${not empty sessionScope.inventorySuccess}"><div class="admin-alert success">${sessionScope.inventorySuccess}</div><c:remove var="inventorySuccess" scope="session"/></c:if>
<c:if test="${not empty sessionScope.inventoryError}"><div class="admin-alert error">${sessionScope.inventoryError}</div><c:remove var="inventoryError" scope="session"/></c:if>

<form class="inventory-toolbar" method="get" action="${ctx}/admin/inventory">
    <input type="search" name="q" value="${param.q}" placeholder="Tìm theo tên, mã hoặc danh mục...">
    <select name="stock">
        <option value="">Tất cả tồn kho</option>
        <option value="out" ${param.stock == 'out' ? 'selected' : ''}>Hết hàng</option>
        <option value="low" ${param.stock == 'low' ? 'selected' : ''}>Sắp hết (1–5)</option>
        <option value="available" ${param.stock == 'available' ? 'selected' : ''}>Còn hàng (&gt;5)</option>
    </select>
    <button type="submit"><i class="fa-solid fa-magnifying-glass"></i> Lọc</button>
</form>

<section class="admin-panel inventory-stock-panel inventory-stock-full">
    <div class="admin-panel-title"><div><span>TỒN KHO HIỆN TẠI</span><h2>${inventory.size()} sản phẩm</h2></div></div>
    <div class="admin-table-wrap">
        <table class="admin-table inventory-table">
            <thead><tr><th>Sản phẩm</th><th>Phân loại</th><th>Tồn kho</th><th>Tình trạng</th><th>Nhập hàng</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${inventory}">
                <tr>
                    <c:set var="inventoryFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
                    <td><div class="inventory-product"><c:choose><c:when test="${not empty p.hinhAnh}"><img class="js-fashion-image" loading="lazy" alt="${p.tenSP}" src="${ctx}/${p.hinhAnh}" data-fallback="${inventoryFallback}"></c:when><c:otherwise><img loading="lazy" alt="${p.tenSP}" src="${inventoryFallback}"></c:otherwise></c:choose><div><strong>${p.tenSP}</strong><small>Mã SP #${p.maSP} · ${p.tenDM}</small></div></div></td>
                    <td><span>${empty p.mauSac ? '—' : p.mauSac}</span><small>${empty p.kichThuoc ? '' : p.kichThuoc}</small></td>
                    <td><strong class="inventory-quantity">${p.soLuongTon}</strong></td>
                    <td><span class="stock-badge ${p.soLuongTon == 0 ? 'out' : (p.soLuongTon <= 5 ? 'low' : 'ok')}">${p.tinhTrangKho}</span></td>
                    <td>
                        <form class="inventory-import-form" method="post" action="${ctx}/admin/inventory" data-single-submit>
                            <input type="hidden" name="maSP" value="${p.maSP}">
                            <label><span>Số lượng</span><input type="number" name="soLuongNhap" min="1" max="100000" required placeholder="SL"></label>
                            <label><span>Số biên lai</span><input type="text" name="soBienLai" maxlength="60" placeholder="VD: BL-2026-001"></label>
                            <label><span>Nhà cung cấp</span><input type="text" name="nhaCungCap" maxlength="180" placeholder="Tên nhà cung cấp"></label>
                            <label><span>Xuất xứ</span><input type="text" name="xuatXu" maxlength="180" placeholder="Nơi sản xuất"></label>
                            <label><span>Ghi chú</span><input type="text" name="ghiChu" maxlength="250" placeholder="Ghi chú thêm"></label>
                            <button type="submit" title="Nhập thêm hàng"><i class="fa-solid fa-plus"></i> Nhập và lưu biên lai</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty inventory}"><tr><td colspan="5" class="admin-empty">Không tìm thấy sản phẩm phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>

<jsp:include page="../common/admin-footer.jsp" />
